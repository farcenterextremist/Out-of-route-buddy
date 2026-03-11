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

/**
 * Room database for the OutOfRouteBuddy app.
 * Provides persistent storage for trip data and statistics.
 */
@Database(
    entities = [TripEntity::class],
    version = 4,
    exportSchema = true,
)
@TypeConverters(DateConverter::class)
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .addCallback(DatabaseHealthCheck.IntegrityCheckCallback()) // ✅ NEW (#4)
                        // DB1 (PROJECT_AUDIT): fallbackToDestructiveMigration kept until migration test coverage exists.
                        // Prevents app crash on migration failure; may cause data loss. Add migration test when schema v2 exists (T4 in COVERAGE_SCORE_AND_CRITICAL_AREAS).
                        .fallbackToDestructiveMigration()
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
    }
} 
