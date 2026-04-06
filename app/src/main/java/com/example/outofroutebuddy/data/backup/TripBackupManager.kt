package com.example.outofroutebuddy.data.backup

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.outofroutebuddy.data.dao.TripDao
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.domain.models.DataTier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Three-layer trip data failsafe:
 *
 * Layer 1 — Android Auto Backup (cloud via Google Drive, configured in backup_rules.xml)
 * Layer 2 — JSON export to device Downloads/OutOfRouteBuddy_Backups/ (survives uninstall)
 * Layer 3 — Raw .db file copy to Downloads/OutOfRouteBuddy_Backups/ (secondary local backup)
 *
 * Auto-triggers on app startup and can be called manually from the UI.
 * Deduplicates on restore so repeated restores are safe.
 */
@Singleton
class TripBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripDao: TripDao,
) {
    companion object {
        private const val TAG = "TripBackupManager"
        private const val BACKUP_DIR = "OutOfRouteBuddy_Backups"
        private const val JSON_FILENAME = "oorb_trip_backup.json"
        private const val DB_FILENAME = "oorb_database_backup.db"
        private const val BACKUP_VERSION = 1
        private const val DB_SCHEMA_VERSION = 7
        private const val MIN_BACKUP_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
        private const val PREFS_NAME = "trip_backup_prefs"
        private const val KEY_LAST_BACKUP_MS = "last_backup_timestamp_ms"
        private const val KEY_LAST_BACKUP_COUNT = "last_backup_trip_count"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ──────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────

    /**
     * Auto-backup: call on app startup and after trip inserts.
     * Skips if no new data or backed up recently.
     */
    suspend fun autoBackupIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentCount = tripDao.countTripsSuspend()
            if (currentCount == 0) return@withContext false

            val now = System.currentTimeMillis()
            val lastBackupMs = prefs.getLong(KEY_LAST_BACKUP_MS, 0L)
            val lastCount = prefs.getInt(KEY_LAST_BACKUP_COUNT, -1)

            val elapsed = now - lastBackupMs
            val countChanged = currentCount != lastCount
            if (elapsed < MIN_BACKUP_INTERVAL_MS && !countChanged) {
                Log.d(TAG, "Auto-backup skipped: ${elapsed / 1000}s ago, count unchanged ($currentCount)")
                return@withContext false
            }

            performFullBackup()
        } catch (e: Exception) {
            Log.e(TAG, "Auto-backup failed", e)
            false
        }
    }

    /**
     * Force a full backup (JSON + DB copy to Downloads). Returns true on success.
     */
    suspend fun performFullBackup(): Boolean = withContext(Dispatchers.IO) {
        try {
            val trips = tripDao.getAllTrips().first()
            if (trips.isEmpty()) {
                Log.d(TAG, "No trips to back up")
                return@withContext false
            }
            Log.i(TAG, "Starting full backup: ${trips.size} trips")

            val jsonOk = writeJsonToDownloads(trips)
            val dbOk = copyDatabaseToDownloads()

            if (jsonOk || dbOk) {
                prefs.edit()
                    .putLong(KEY_LAST_BACKUP_MS, System.currentTimeMillis())
                    .putInt(KEY_LAST_BACKUP_COUNT, trips.size)
                    .apply()
            }
            Log.i(TAG, "Backup complete: json=$jsonOk  db=$dbOk  trips=${trips.size}")
            jsonOk || dbOk
        } catch (e: Exception) {
            Log.e(TAG, "performFullBackup failed", e)
            false
        }
    }

    /**
     * Restore trips from the JSON backup in Downloads.
     * Skips duplicates (matches on date + loadedMiles + actualMiles + oorPercentage).
     * Returns the count of newly inserted trips, or -1 on failure.
     */
    suspend fun restoreFromBackup(): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val json = readJsonFromDownloads()
            if (json == null) {
                Log.w(TAG, "No backup file found")
                return@withContext RestoreResult(-1, "No backup file found in Downloads/$BACKUP_DIR/")
            }

            val root = JSONObject(json)
            val tripsArray = root.getJSONArray("trips")
            val backupCount = tripsArray.length()

            val existing = tripDao.getAllTrips().first()
            val existingFingerprints = existing.map { fingerprint(it) }.toSet()

            var restored = 0
            var skipped = 0
            for (i in 0 until backupCount) {
                val entity = jsonToTripEntity(tripsArray.getJSONObject(i))
                if (fingerprint(entity) in existingFingerprints) {
                    skipped++
                    continue
                }
                // Insert with id=0 so Room auto-generates a new primary key
                tripDao.insertTrip(entity.copy(id = 0))
                restored++
            }

            Log.i(TAG, "Restore complete: $restored inserted, $skipped duplicates skipped (backup had $backupCount)")
            RestoreResult(restored, "Restored $restored trips ($skipped already existed)")
        } catch (e: Exception) {
            Log.e(TAG, "restoreFromBackup failed", e)
            RestoreResult(-1, "Restore failed: ${e.message}")
        }
    }

    /** Info about the most recent backup (for UI display). */
    fun getLastBackupInfo(): BackupInfo {
        val ms = prefs.getLong(KEY_LAST_BACKUP_MS, 0L)
        val count = prefs.getInt(KEY_LAST_BACKUP_COUNT, 0)
        return BackupInfo(
            lastBackupTimestamp = if (ms > 0) Date(ms) else null,
            tripCount = count,
        )
    }

    // ──────────────────────────────────────────────
    //  Dedup fingerprint
    // ──────────────────────────────────────────────

    private fun fingerprint(e: TripEntity): String =
        "${e.date.time}_${e.loadedMiles}_${e.actualMiles}_${e.oorPercentage}_${e.createdAt.time}"

    // ──────────────────────────────────────────────
    //  JSON serialization
    // ──────────────────────────────────────────────

    private fun tripsToJson(trips: List<TripEntity>): String {
        val root = JSONObject()
        root.put("backupVersion", BACKUP_VERSION)
        root.put("backupTimestamp", System.currentTimeMillis())
        root.put("backupDate", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("tripCount", trips.size)
        root.put("dbVersion", DB_SCHEMA_VERSION)

        val arr = JSONArray()
        trips.forEach { arr.put(entityToJson(it)) }
        root.put("trips", arr)
        return root.toString(2)
    }

    private fun entityToJson(e: TripEntity): JSONObject = JSONObject().apply {
        put("id", e.id)
        put("date", e.date.time)
        put("loadedMiles", e.loadedMiles)
        put("bounceMiles", e.bounceMiles)
        put("actualMiles", e.actualMiles)
        put("oorMiles", e.oorMiles)
        put("oorPercentage", e.oorPercentage)
        put("createdAt", e.createdAt.time)
        put("avgGpsAccuracy", e.avgGpsAccuracy)
        put("minGpsAccuracy", e.minGpsAccuracy)
        put("maxGpsAccuracy", e.maxGpsAccuracy)
        put("totalGpsPoints", e.totalGpsPoints)
        put("validGpsPoints", e.validGpsPoints)
        put("rejectedGpsPoints", e.rejectedGpsPoints)
        put("tripDurationMinutes", e.tripDurationMinutes)
        put("avgSpeedMph", e.avgSpeedMph)
        put("maxSpeedMph", e.maxSpeedMph)
        put("locationJumpsDetected", e.locationJumpsDetected)
        put("accuracyWarnings", e.accuracyWarnings)
        put("speedAnomalies", e.speedAnomalies)
        put("tripStartTime", e.tripStartTime?.time ?: JSONObject.NULL)
        put("tripEndTime", e.tripEndTime?.time ?: JSONObject.NULL)
        put("tripTimeZoneId", e.tripTimeZoneId ?: JSONObject.NULL)
        put("wasInterrupted", e.wasInterrupted)
        put("interruptionCount", e.interruptionCount)
        put("lastLocationLat", e.lastLocationLat)
        put("lastLocationLng", e.lastLocationLng)
        put("lastLocationTime", e.lastLocationTime?.time ?: JSONObject.NULL)
        put("interstatePercent", e.interstatePercent)
        put("interstateMinutes", e.interstateMinutes)
        put("backRoadsPercent", e.backRoadsPercent)
        put("backRoadsMinutes", e.backRoadsMinutes)
        put("truckStopsVisited", e.truckStopsVisited)
        put("pickupAddress", e.pickupAddress)
        put("dropoffAddress", e.dropoffAddress)
        put("dataTier", e.dataTier.name)
        put("tripStartLat", e.tripStartLat ?: JSONObject.NULL)
        put("tripStartLng", e.tripStartLng ?: JSONObject.NULL)
        put("tripEndLat", e.tripEndLat ?: JSONObject.NULL)
        put("tripEndLng", e.tripEndLng ?: JSONObject.NULL)
        put("stopEventsCount", e.stopEventsCount)
        put("significantTurnsCount", e.significantTurnsCount)
        put("elevationMinMeters", e.elevationMinMeters ?: JSONObject.NULL)
        put("elevationMaxMeters", e.elevationMaxMeters ?: JSONObject.NULL)
        put("distinctTimeZoneCount", e.distinctTimeZoneCount)
    }

    private fun jsonToTripEntity(obj: JSONObject): TripEntity = TripEntity(
        id = obj.optLong("id", 0),
        date = Date(obj.getLong("date")),
        loadedMiles = obj.getDouble("loadedMiles"),
        bounceMiles = obj.getDouble("bounceMiles"),
        actualMiles = obj.getDouble("actualMiles"),
        oorMiles = obj.getDouble("oorMiles"),
        oorPercentage = obj.getDouble("oorPercentage"),
        createdAt = Date(obj.optLong("createdAt", System.currentTimeMillis())),
        avgGpsAccuracy = obj.optDouble("avgGpsAccuracy", 0.0),
        minGpsAccuracy = obj.optDouble("minGpsAccuracy", 0.0),
        maxGpsAccuracy = obj.optDouble("maxGpsAccuracy", 0.0),
        totalGpsPoints = obj.optInt("totalGpsPoints", 0),
        validGpsPoints = obj.optInt("validGpsPoints", 0),
        rejectedGpsPoints = obj.optInt("rejectedGpsPoints", 0),
        tripDurationMinutes = obj.optInt("tripDurationMinutes", 0),
        avgSpeedMph = obj.optDouble("avgSpeedMph", 0.0),
        maxSpeedMph = obj.optDouble("maxSpeedMph", 0.0),
        locationJumpsDetected = obj.optInt("locationJumpsDetected", 0),
        accuracyWarnings = obj.optInt("accuracyWarnings", 0),
        speedAnomalies = obj.optInt("speedAnomalies", 0),
        tripStartTime = obj.nullableLong("tripStartTime")?.let { Date(it) },
        tripEndTime = obj.nullableLong("tripEndTime")?.let { Date(it) },
        tripTimeZoneId = obj.nullableString("tripTimeZoneId"),
        wasInterrupted = obj.optBoolean("wasInterrupted", false),
        interruptionCount = obj.optInt("interruptionCount", 0),
        lastLocationLat = obj.optDouble("lastLocationLat", 0.0),
        lastLocationLng = obj.optDouble("lastLocationLng", 0.0),
        lastLocationTime = obj.nullableLong("lastLocationTime")?.let { Date(it) },
        interstatePercent = obj.optDouble("interstatePercent", 0.0),
        interstateMinutes = obj.optInt("interstateMinutes", 0),
        backRoadsPercent = obj.optDouble("backRoadsPercent", 0.0),
        backRoadsMinutes = obj.optInt("backRoadsMinutes", 0),
        truckStopsVisited = obj.optInt("truckStopsVisited", 0),
        pickupAddress = obj.optString("pickupAddress", ""),
        dropoffAddress = obj.optString("dropoffAddress", ""),
        dataTier = runCatching { DataTier.valueOf(obj.optString("dataTier", "GOLD")) }
            .getOrDefault(DataTier.GOLD),
        tripStartLat = obj.nullableDouble("tripStartLat"),
        tripStartLng = obj.nullableDouble("tripStartLng"),
        tripEndLat = obj.nullableDouble("tripEndLat"),
        tripEndLng = obj.nullableDouble("tripEndLng"),
        stopEventsCount = obj.optInt("stopEventsCount", 0),
        significantTurnsCount = obj.optInt("significantTurnsCount", 0),
        elevationMinMeters = obj.nullableDouble("elevationMinMeters"),
        elevationMaxMeters = obj.nullableDouble("elevationMaxMeters"),
        distinctTimeZoneCount = obj.optInt("distinctTimeZoneCount", 0),
    )

    private fun JSONObject.nullableDouble(key: String): Double? =
        if (has(key) && !isNull(key)) getDouble(key) else null

    private fun JSONObject.nullableLong(key: String): Long? =
        if (has(key) && !isNull(key)) getLong(key) else null

    private fun JSONObject.nullableString(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null

    // ──────────────────────────────────────────────
    //  File I/O — Downloads folder (survives uninstall)
    // ──────────────────────────────────────────────

    private fun writeJsonToDownloads(trips: List<TripEntity>): Boolean = try {
        val bytes = tripsToJson(trips).toByteArray(Charsets.UTF_8)
        writeToDownloads(JSON_FILENAME, "application/json", bytes)
        true
    } catch (e: Exception) {
        Log.e(TAG, "JSON backup write failed", e)
        false
    }

    private fun copyDatabaseToDownloads(): Boolean {
        return try {
            val dbFile = context.getDatabasePath("outofroutebuddy_database")
            if (!dbFile.exists()) {
                Log.w(TAG, "DB file not found: ${dbFile.absolutePath}")
                return false
            }
            writeToDownloads(DB_FILENAME, "application/octet-stream", dbFile.readBytes())
            true
        } catch (e: Exception) {
            Log.e(TAG, "DB backup write failed", e)
            false
        }
    }

    private fun writeToDownloads(filename: String, mimeType: String, data: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(filename, mimeType, data)
        } else {
            writeToPublicDir(filename, data)
        }
    }

    /** API 29+ — scoped storage via MediaStore.Downloads */
    private fun writeViaMediaStore(filename: String, mimeType: String, data: ByteArray) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val resolver = context.contentResolver
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_DIR"

        // Remove previous version to avoid stale duplicates
        val sel = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
        resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, sel, arrayOf(filename, "$relativePath/"))

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("MediaStore insert returned null URI")
        resolver.openOutputStream(uri)?.use { it.write(data) }
            ?: throw IllegalStateException("Could not open OutputStream for $uri")
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }

    /** API < 29 — legacy direct file write */
    @Suppress("DEPRECATION")
    private fun writeToPublicDir(filename: String, data: ByteArray) {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            BACKUP_DIR,
        )
        dir.mkdirs()
        File(dir, filename).writeBytes(data)
    }

    // ──────────────────────────────────────────────
    //  Read backup for restore
    // ──────────────────────────────────────────────

    private fun readJsonFromDownloads(): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            readViaMediaStore()
        } else {
            readFromPublicDir()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Read backup failed", e)
        null
    }

    private fun readViaMediaStore(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val resolver = context.contentResolver
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_DIR/"
        val sel = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"

        resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID),
            sel,
            arrayOf(JSON_FILENAME, relativePath),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                val uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                return resolver.openInputStream(uri)?.bufferedReader()?.readText()
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun readFromPublicDir(): String? {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "$BACKUP_DIR/$JSON_FILENAME",
        )
        return if (file.exists()) file.readText() else null
    }

    // ──────────────────────────────────────────────
    //  Data classes
    // ──────────────────────────────────────────────

    data class BackupInfo(
        val lastBackupTimestamp: Date?,
        val tripCount: Int,
    )

    data class RestoreResult(
        val restoredCount: Int,
        val message: String,
    )
}
