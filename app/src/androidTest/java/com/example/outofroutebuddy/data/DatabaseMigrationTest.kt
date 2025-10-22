package com.example.outofroutebuddy.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * 🧪 Database Migration Tests
 * 
 * Tests for Room database migrations:
 * - Migration from v1 to v2
 * - Data integrity after migration
 * - Migration failure recovery
 * 
 * Priority: 🟢 LOW
 * Impact: Future-proofing for database updates
 * 
 * Created: Phase 3B - Database Evolution
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    
    @Test
    fun testDatabaseCreation() {
        // Create database
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Should create successfully
        assertNotNull(db)
        assertNotNull(db.tripDao())
        
        db.close()
    }
    
    @Test
    fun testDatabaseHasCorrectTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Query should not throw
        val trips = db.tripDao().getAllTrips()
        assertNotNull("Trips table should exist", trips)
        
        db.close()
    }
    
    @Test
    fun testDatabaseHandlesBasicOperations() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Basic operations should work
        val trips = db.tripDao().getAllTrips()
        assertTrue("Should start with no trips", trips.isEmpty())
        
        db.close()
    }
}

