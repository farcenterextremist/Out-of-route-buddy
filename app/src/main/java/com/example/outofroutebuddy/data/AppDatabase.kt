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
    version = 2,
    exportSchema = false,
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
                        .addMigrations(MIGRATION_1_2)
                        .addCallback(DatabaseHealthCheck.IntegrityCheckCallback()) // ✅ NEW (#4)
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 =
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
    }
} 
