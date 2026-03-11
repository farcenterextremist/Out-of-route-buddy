package com.example.outofroutebuddy.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * T4: Room migration test. Creates DB at version 1, runs MIGRATION_1_2, opens at version 2 and asserts.
 * Requires schema JSON files in app/schemas (see sourceSets.androidTest.assets in build.gradle.kts).
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val schemaDir = "com.example.outofroutebuddy.data.AppDatabase"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        schemaDir,
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_preservesDataAndAddsColumns() {
        // Create v1 DB and insert a row
        helper.createDatabase("test-db", 1).apply {
            execSQL(
                "INSERT INTO trips (id, date, loadedMiles, bounceMiles, actualMiles, oorMiles, oorPercentage, createdAt) " +
                    "VALUES (1, 1000, 10.0, 2.0, 12.0, 0.0, 0.0, 1000)"
            )
            close()
        }

        // Run migration and open at v2
        val db = helper.runMigrationsAndValidate("test-db", 2, true, AppDatabase.MIGRATION_1_2)

        // Assert v2 columns exist by querying
        val c = db.query("SELECT id, date, loadedMiles, actualMiles, avgGpsAccuracy, tripStartTime FROM trips WHERE id = 1")
        assertEquals(1, c.count)
        c.moveToFirst()
        assertEquals(1L, c.getLong(0))
        assertEquals(1000L, c.getLong(1))
        assertEquals(12.0, c.getDouble(3), 0.01)
        c.close()
        db.close()
    }
}
