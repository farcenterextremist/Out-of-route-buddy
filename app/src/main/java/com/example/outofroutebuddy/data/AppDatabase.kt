package com.example.outofroutebuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.outofroutebuddy.data.dao.TripDao
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.data.util.DateConverter
import com.example.outofroutebuddy.data.util.DataTierConverter

/**
 * Room database for the OutOfRouteBuddy app.
 * Provides persistent storage for trip data and statistics.
 */
@Database(
    entities = [TripEntity::class],
    version = 7,
    exportSchema = true,
)
@TypeConverters(DateConverter::class, DataTierConverter::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Data Access Object for trip operations
     */
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the database instance (singleton pattern)
         * ✅ NEW (#4): Added integrity check callback
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "outofroutebuddy_database",
                    )
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                        )
                        .addCallback(DatabaseHealthCheck.IntegrityCheckCallback())
                        // REMOVED: fallbackToDestructiveMigration() — this silently wipes all
                        // trip data when a migration fails. Better to crash and fix the migration
                        // than to silently delete GOLD-tier user data.
                        .build()
                INSTANCE = instance
                instance
            }
        }

        /** T4: internal so migration tests can run MIGRATION_1_2 */
        internal val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Add new GPS metadata columns with default values
                    db.execSQL("ALTER TABLE trips ADD COLUMN avgGpsAccuracy REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN minGpsAccuracy REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN maxGpsAccuracy REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN totalGpsPoints INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN validGpsPoints INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN rejectedGpsPoints INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripDurationMinutes INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN avgSpeedMph REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN maxSpeedMph REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN locationJumpsDetected INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN accuracyWarnings INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN speedAnomalies INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripStartTime INTEGER")
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripEndTime INTEGER")
                    db.execSQL("ALTER TABLE trips ADD COLUMN wasInterrupted INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN interruptionCount INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN lastLocationLat REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN lastLocationLng REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN lastLocationTime INTEGER")
                }
            }

        /** Migration 2→3: add trip timezone for display when user is in a different zone. */
        internal val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripTimeZoneId TEXT")
                }
            }

        /** Migration 3→4: add extended GPS metadata (interstate/back roads, truck stops). */
        internal val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE trips ADD COLUMN interstatePercent REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN interstateMinutes INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN backRoadsPercent REAL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN backRoadsMinutes INTEGER DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN truckStopsVisited INTEGER DEFAULT 0")
                }
            }

        /** Migration 4→5: add data tier (SILVER / PLATINUM / GOLD). Existing rows default to GOLD (human data). */
        internal val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Rebuild the table so legacy columns added with SQLite defaults/nullability
                    // match the exact schema Room expects going forward.
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `trips_new` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `date` INTEGER NOT NULL,
                            `loadedMiles` REAL NOT NULL,
                            `bounceMiles` REAL NOT NULL,
                            `actualMiles` REAL NOT NULL,
                            `oorMiles` REAL NOT NULL,
                            `oorPercentage` REAL NOT NULL,
                            `createdAt` INTEGER NOT NULL,
                            `avgGpsAccuracy` REAL NOT NULL,
                            `minGpsAccuracy` REAL NOT NULL,
                            `maxGpsAccuracy` REAL NOT NULL,
                            `totalGpsPoints` INTEGER NOT NULL,
                            `validGpsPoints` INTEGER NOT NULL,
                            `rejectedGpsPoints` INTEGER NOT NULL,
                            `tripDurationMinutes` INTEGER NOT NULL,
                            `avgSpeedMph` REAL NOT NULL,
                            `maxSpeedMph` REAL NOT NULL,
                            `locationJumpsDetected` INTEGER NOT NULL,
                            `accuracyWarnings` INTEGER NOT NULL,
                            `speedAnomalies` INTEGER NOT NULL,
                            `tripStartTime` INTEGER,
                            `tripEndTime` INTEGER,
                            `tripTimeZoneId` TEXT,
                            `wasInterrupted` INTEGER NOT NULL,
                            `interruptionCount` INTEGER NOT NULL,
                            `lastLocationLat` REAL NOT NULL,
                            `lastLocationLng` REAL NOT NULL,
                            `lastLocationTime` INTEGER,
                            `interstatePercent` REAL NOT NULL,
                            `interstateMinutes` INTEGER NOT NULL,
                            `backRoadsPercent` REAL NOT NULL,
                            `backRoadsMinutes` INTEGER NOT NULL,
                            `truckStopsVisited` INTEGER NOT NULL,
                            `dataTier` TEXT NOT NULL
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        INSERT INTO `trips_new` (
                            `id`, `date`, `loadedMiles`, `bounceMiles`, `actualMiles`, `oorMiles`, `oorPercentage`, `createdAt`,
                            `avgGpsAccuracy`, `minGpsAccuracy`, `maxGpsAccuracy`, `totalGpsPoints`, `validGpsPoints`, `rejectedGpsPoints`,
                            `tripDurationMinutes`, `avgSpeedMph`, `maxSpeedMph`, `locationJumpsDetected`, `accuracyWarnings`, `speedAnomalies`,
                            `tripStartTime`, `tripEndTime`, `tripTimeZoneId`, `wasInterrupted`, `interruptionCount`,
                            `lastLocationLat`, `lastLocationLng`, `lastLocationTime`,
                            `interstatePercent`, `interstateMinutes`, `backRoadsPercent`, `backRoadsMinutes`, `truckStopsVisited`, `dataTier`
                        )
                        SELECT
                            `id`, `date`, `loadedMiles`, `bounceMiles`, `actualMiles`, `oorMiles`, `oorPercentage`, `createdAt`,
                            `avgGpsAccuracy`, `minGpsAccuracy`, `maxGpsAccuracy`, `totalGpsPoints`, `validGpsPoints`, `rejectedGpsPoints`,
                            `tripDurationMinutes`, `avgSpeedMph`, `maxSpeedMph`, `locationJumpsDetected`, `accuracyWarnings`, `speedAnomalies`,
                            `tripStartTime`, `tripEndTime`, `tripTimeZoneId`, `wasInterrupted`, `interruptionCount`,
                            `lastLocationLat`, `lastLocationLng`, `lastLocationTime`,
                            COALESCE(`interstatePercent`, 0.0),
                            COALESCE(`interstateMinutes`, 0),
                            COALESCE(`backRoadsPercent`, 0.0),
                            COALESCE(`backRoadsMinutes`, 0),
                            COALESCE(`truckStopsVisited`, 0),
                            'GOLD'
                        FROM `trips`
                        """.trimIndent()
                    )

                    db.execSQL("DROP TABLE `trips`")
                    db.execSQL("ALTER TABLE `trips_new` RENAME TO `trips`")
                }
            }

        /** Migration 5→6: add pickup/dropoff address columns. */
        internal val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE trips ADD COLUMN pickupAddress TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE trips ADD COLUMN dropoffAddress TEXT NOT NULL DEFAULT ''")
                }
            }

        /** Migration 6→7: trip start/end coordinates, stop/turn heuristics, elevation & TZ stats. */
        internal val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripStartLat REAL")
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripStartLng REAL")
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripEndLat REAL")
                    db.execSQL("ALTER TABLE trips ADD COLUMN tripEndLng REAL")
                    db.execSQL("ALTER TABLE trips ADD COLUMN stopEventsCount INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN significantTurnsCount INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE trips ADD COLUMN elevationMinMeters REAL")
                    db.execSQL("ALTER TABLE trips ADD COLUMN elevationMaxMeters REAL")
                    db.execSQL("ALTER TABLE trips ADD COLUMN distinctTimeZoneCount INTEGER NOT NULL DEFAULT 0")
                }
            }
    }
} 
