package com.example.outofroutebuddy.data.dao

import androidx.room.Room
import com.example.outofroutebuddy.data.AppDatabase
import com.example.outofroutebuddy.data.entities.TripEntity
import com.example.outofroutebuddy.data.util.DateConverter
import com.google.common.truth.Truth.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
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
    fun insert_and_getTripById_works() = runBlocking {
        val id = dao.insertTrip(sampleTrip(dateOffsetDays = 0))
        val fetched = dao.getTripById(id)
        assertThat(fetched).isNotNull()
        assertThat(fetched?.id).isEqualTo(id)
    }

    @Test
    fun getTripsForDateRange_returnsSortedAsc() {
        // DAO contract: getTripsForDateRange uses ORDER BY date ASC, id ASC (oldest first)
        val t1 = sampleTrip(dateOffsetDays = -3)
        val t2 = sampleTrip(dateOffsetDays = -1)
        val t3 = sampleTrip(dateOffsetDays = 0)
        dao.insertTrips(listOf(t1, t2, t3))

        val start = daysFromNow(-3)
        val end = daysFromNow(0)
        val result = dao.getTripsForDateRange(start, end)
        assertThat(result.size).isAtLeast(1)
        if (result.size >= 2) {
            assertThat(result[0].date.time).isAtMost(result[1].date.time)
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

    @Test
    fun getTripsForDateRange_excludesTripsOutsideRange() {
        dao.insertTrips(listOf(
            sampleTrip(dateOffsetDays = -2),
            sampleTrip(dateOffsetDays = 0),
            sampleTrip(dateOffsetDays = 2)
        ))
        val start = startOfDay(daysFromNow(0))
        val end = endOfDay(daysFromNow(0))
        val result = dao.getTripsForDateRange(start, end)
        assertThat(result).hasSize(1)
        // Trip for day 0 is stored with full timestamp; assert it falls within the queried day.
        // Uses Truth LongSubject isAtLeast/isAtMost (not isInClosedInterval).
        assertThat(result[0].date.time).isAtLeast(start.time)
        assertThat(result[0].date.time).isAtMost(end.time)
    }

    @Test
    fun getTripsForDateRange_includesBoundaryDates() {
        val rangeStart = startOfDay(daysFromNow(0))
        val rangeEnd = endOfDay(daysFromNow(1))
        val tripAtStart = TripEntity(
            id = 0,
            date = rangeStart,
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 125.0,
            oorMiles = 5.0,
            oorPercentage = 4.17,
        )
        val tripAtEnd = TripEntity(
            id = 0,
            date = rangeEnd,
            loadedMiles = 100.0,
            bounceMiles = 20.0,
            actualMiles = 125.0,
            oorMiles = 5.0,
            oorPercentage = 4.17,
        )
        dao.insertTrip(tripAtStart)
        dao.insertTrip(tripAtEnd)
        val result = dao.getTripsForDateRange(rangeStart, rangeEnd)
        assertThat(result).hasSize(2)
    }

    @Test
    fun getTripsOverlappingDay_includesTripSpanningMidnightOnBothDays() {
        val jan10_2350 = dateAt(2025, Calendar.JANUARY, 10, 23, 50, 0, 0)
        val jan11_0010 = dateAt(2025, Calendar.JANUARY, 11, 0, 10, 0, 0)
        dao.insertTrip(
            sampleTrip(0).copy(
                date = jan10_2350,
                tripStartTime = jan10_2350,
                tripEndTime = jan11_0010,
            ),
        )

        val day10Start = dateAt(2025, Calendar.JANUARY, 10, 0, 0, 0, 0)
        val day11Start = dateAt(2025, Calendar.JANUARY, 11, 0, 0, 0, 0)
        val day12Start = dateAt(2025, Calendar.JANUARY, 12, 0, 0, 0, 0)

        val jan10Trips = dao.getTripsOverlappingDay(day10Start, day11Start)
        val jan11Trips = dao.getTripsOverlappingDay(day11Start, day12Start)

        assertThat(jan10Trips).hasSize(1)
        assertThat(jan11Trips).hasSize(1)
    }

    @Test
    fun getTripsOverlappingDay_excludesTripStartingExactlyAtRangeEnd() {
        val day11Start = dateAt(2025, Calendar.JANUARY, 11, 0, 0, 0, 0)
        val day11End = dateAt(2025, Calendar.JANUARY, 12, 0, 0, 0, 0)
        dao.insertTrip(
            sampleTrip(0).copy(
                date = day11End,
                tripStartTime = day11End,
                tripEndTime = dateAt(2025, Calendar.JANUARY, 12, 0, 30, 0, 0),
            ),
        )

        val result = dao.getTripsOverlappingDay(day11Start, day11End)
        assertThat(result).isEmpty()
    }

    @Test
    fun getTripsOverlappingRange_usesHalfOpenEndBoundary() {
        val rangeStart = dateAt(2025, Calendar.FEBRUARY, 1, 0, 0, 0, 0)
        val rangeEnd = dateAt(2025, Calendar.MARCH, 1, 0, 0, 0, 0)
        val insideStart = dateAt(2025, Calendar.FEBRUARY, 28, 23, 45, 0, 0)
        val insideEnd = dateAt(2025, Calendar.MARCH, 1, 0, 5, 0, 0)
        val atBoundaryStart = rangeEnd

        dao.insertTrip(
            sampleTrip(0).copy(
                date = insideStart,
                tripStartTime = insideStart,
                tripEndTime = insideEnd,
            ),
        )
        dao.insertTrip(
            sampleTrip(0).copy(
                date = atBoundaryStart,
                tripStartTime = atBoundaryStart,
                tripEndTime = dateAt(2025, Calendar.MARCH, 1, 0, 30, 0, 0),
            ),
        )

        val result = dao.getTripsOverlappingRange(rangeStart, rangeEnd)
        assertThat(result).hasSize(1)
        assertThat(result.first().tripStartTime).isEqualTo(insideStart)
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

    private fun dateAt(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hour: Int,
        minute: Int,
        second: Int,
        millis: Int,
    ): Date {
        return Calendar.getInstance().apply {
            set(year, month, dayOfMonth, hour, minute, second)
            set(Calendar.MILLISECOND, millis)
        }.time
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


