package com.example.outofroutebuddy.data.dao

import androidx.room.Room
import com.example.outofroutebuddy.data.AppDatabase
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.data.util.DateConverter
import com.google.common.truth.Truth.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TripDaoInMemoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TripDao

    @Before
    fun setup() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.tripDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_getTripById_works() {
        val id = dao.insertTrip(sampleTrip(dateOffsetDays = 0))
        val fetched = dao.getTripById(id)
        assertThat(fetched).isNotNull()
        assertThat(fetched?.id).isEqualTo(id)
    }

    @Test
    fun getTripsForDateRange_returnsSortedDesc() {
        // Insert trips across days
        val t1 = sampleTrip(dateOffsetDays = -3)
        val t2 = sampleTrip(dateOffsetDays = -1)
        val t3 = sampleTrip(dateOffsetDays = 0)
        dao.insertTrips(listOf(t1, t2, t3))

        val start = daysFromNow(-3)
        val end = daysFromNow(0)
        val result = dao.getTripsForDateRange(start, end)
        assertThat(result.size).isAtLeast(1)
        if (result.size >= 2) {
            // Desc by date: latest first
            assertThat(result[0].date.time).isAtLeast(result[1].date.time)
        }
    }

    @Test
    fun getTripCountForDate_filtersByDay() {
        dao.insertTrips(listOf(
            sampleTrip(dateOffsetDays = 0),
            sampleTrip(dateOffsetDays = 0),
            sampleTrip(dateOffsetDays = -1)
        ))

        val startToday = startOfDay(daysFromNow(0))
        val endToday = endOfDay(daysFromNow(0))
        val countToday = dao.getTripCountForDate(startToday, endToday)
        assertThat(countToday).isEqualTo(2)
    }

    @Test
    fun deleteAllTrips_clearsTable() {
        dao.insertTrips(listOf(sampleTrip(0), sampleTrip(-2)))
        assertThat(dao.getTripCount()).isGreaterThan(0)
        dao.deleteAllTrips()
        assertThat(dao.getTripCount()).isEqualTo(0)
    }

    private fun sampleTrip(dateOffsetDays: Int): TripEntity {
        val date = Date(System.currentTimeMillis() + dateOffsetDays * 24L * 60L * 60L * 1000L)
        val dispatched = 100.0 + 20.0
        val oorPct = if (dispatched > 0) (5.0 / dispatched) * 100.0 else 0.0
        return TripEntity(
            id = 0,
            date = date,
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 125.0,
            oorMiles = 5.0,
            oorPercentage = oorPct,
            avgGpsAccuracy = 10.0,
            minGpsAccuracy = 5.0,
            maxGpsAccuracy = 20.0,
            totalGpsPoints = 10,
            validGpsPoints = 9,
            rejectedGpsPoints = 1,
            tripDurationMinutes = 60,
            avgSpeedMph = 30.0,
            maxSpeedMph = 60.0,
            locationJumpsDetected = 0,
            accuracyWarnings = 0,
            speedAnomalies = 0,
            tripStartTime = null,
            tripEndTime = null,
            wasInterrupted = false,
            interruptionCount = 0,
            lastLocationLat = 0.0,
            lastLocationLng = 0.0,
            lastLocationTime = null
        )
    }

    private fun daysFromNow(days: Int): Date {
        return Date(System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L)
    }

    private fun startOfDay(date: Date): Date {
        val cal = java.util.Calendar.getInstance().apply { time = date }
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun endOfDay(date: Date): Date {
        val cal = java.util.Calendar.getInstance().apply { time = date }
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        return cal.time
    }
}


