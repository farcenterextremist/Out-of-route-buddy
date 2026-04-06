package com.example.outofroutebuddy.presentation.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.domain.calendar.CalendarDayOorTierCalculator
import com.example.outofroutebuddy.domain.models.CalendarDayOorTier
import com.example.outofroutebuddy.domain.models.GpsMetadata
import com.example.outofroutebuddy.domain.models.Trip
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Expandable Stat Card adapter for TripHistoryByDateDialog.
 * When [viewingDate] is set, the card title shows "Trip 1", "Trip 2", etc., and
 * midnight-spanning trips show "Partial Trip Mar 2–Mar 3" (date range) instead of "Started X, ended Y".
 * When the trip was recorded in a different timezone than the user's current one, the timezone
 * abbreviation (e.g. CST) is shown; not shown when user is already in that timezone.
 */
class TripHistoryStatCardAdapter(
    private val onDeleteClick: (Trip) -> Unit,
    private val onTripClick: ((Trip) -> Unit)? = null,
    var viewingDate: Date? = null,
) : ListAdapter<Trip, TripHistoryStatCardAdapter.StatCardViewHolder>(TripDiffCallback()) {

    private val expandedTripIds = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_history_stat_card, parent, false)
        return StatCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatCardViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            viewingDate,
            tripIndex = if (viewingDate != null) position + 1 else null,
            expandedTripIds.contains(getItem(position).id),
            onTripClick = onTripClick
        ) { tripId ->
            if (expandedTripIds.contains(tripId)) {
                expandedTripIds.remove(tripId)
            } else {
                expandedTripIds.add(tripId)
            }
            notifyItemChanged(position)
        }
    }

    inner class StatCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripDate: TextView = itemView.findViewById(R.id.trip_date)
        private val tripTimeRange: TextView = itemView.findViewById(R.id.trip_time_range)
        private val tripMiles: TextView = itemView.findViewById(R.id.trip_miles)
        private val tripOor: TextView = itemView.findViewById(R.id.trip_oor)
        private val tripMilesSliceCaption: TextView = itemView.findViewById(R.id.trip_miles_slice_caption)
        private val expandButton: ImageButton = itemView.findViewById(R.id.expand_button)
        private val viewDetailsButton: View = itemView.findViewById(R.id.view_details_button)
        private val deleteButton: View = itemView.findViewById(R.id.delete_button)
        private val metadataSection: LinearLayout = itemView.findViewById(R.id.metadata_section)
        private val tripLoadedMiles: TextView = itemView.findViewById(R.id.trip_loaded_miles)
        private val tripBounceMiles: TextView = itemView.findViewById(R.id.trip_bounce_miles)
        private val tripDispatchedMiles: TextView = itemView.findViewById(R.id.trip_dispatched_miles)
        private val tripDuration: TextView = itemView.findViewById(R.id.trip_duration)
        private val tripStartLocationBlock: LinearLayout = itemView.findViewById(R.id.trip_start_location_block)
        private val tripStartCoords: TextView = itemView.findViewById(R.id.trip_start_coords)
        private val tripEndLocationBlock: LinearLayout = itemView.findViewById(R.id.trip_end_location_block)
        private val tripEndCoords: TextView = itemView.findViewById(R.id.trip_end_coords)
        private val tripGpsMetadata: TextView = itemView.findViewById(R.id.trip_gps_metadata)
        private val tripGpsInsights: TextView = itemView.findViewById(R.id.trip_gps_insights)
        private val tripInterstate: TextView = itemView.findViewById(R.id.trip_interstate)
        private val tripBackRoads: TextView = itemView.findViewById(R.id.trip_back_roads)
        private val tripTruckStops: TextView = itemView.findViewById(R.id.trip_truck_stops)

        fun bind(trip: Trip, viewingDate: Date?, tripIndex: Int?, isExpanded: Boolean, onTripClick: ((Trip) -> Unit)?, onExpandToggle: (String) -> Unit) {
            val card = itemView as MaterialCardView
            val ctx = itemView.context
            val strokePx = (2 * itemView.resources.displayMetrics.density).toInt().coerceAtLeast(1)
            val primaryText = ContextCompat.getColor(ctx, R.color.text_primary_adaptive)
            if (viewingDate != null) {
                // Calendar "trip by date" dialog: OOR tier always takes priority.
                // Green (≤10%), Orange (10-14%), Deep Red (>14%).
                val tier = CalendarDayOorTierCalculator.oorTierForTrip(trip)
                card.strokeWidth = strokePx
                when (tier) {
                    CalendarDayOorTier.GREEN -> {
                        card.strokeColor = ContextCompat.getColor(ctx, R.color.history_trip_oor_good_card_stroke)
                        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.history_trip_oor_good_card_fill))
                        tripOor.setTextColor(ContextCompat.getColor(ctx, R.color.history_trip_oor_good_oor_text))
                    }
                    CalendarDayOorTier.YELLOW_OUTLINE -> {
                        card.strokeColor = ContextCompat.getColor(ctx, R.color.history_trip_oor_warn_card_stroke)
                        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.history_trip_oor_warn_card_fill))
                        tripOor.setTextColor(ContextCompat.getColor(ctx, R.color.history_trip_oor_warn_oor_text))
                    }
                    CalendarDayOorTier.RED -> {
                        card.strokeColor = ContextCompat.getColor(ctx, R.color.history_trip_oor_bad_card_stroke)
                        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.history_trip_oor_bad_card_fill))
                        tripOor.setTextColor(ContextCompat.getColor(ctx, R.color.history_trip_oor_bad_oor_text))
                    }
                }
                card.contentDescription = null
            } else if (TripStatCardReviewFlags.shouldFlagForReview(trip)) {
                card.strokeWidth = strokePx
                card.strokeColor = ContextCompat.getColor(ctx, R.color.trip_review_flag_stroke)
                card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.trip_review_flag_card_fill))
                card.contentDescription =
                    ctx.getString(R.string.trip_stat_card_flagged_content_description)
                tripOor.setTextColor(primaryText)
            } else {
                card.strokeWidth = 0
                card.strokeColor = ContextCompat.getColor(ctx, android.R.color.transparent)
                card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.card_background_adaptive))
                card.contentDescription = null
                tripOor.setTextColor(primaryText)
            }

            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
            val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
            val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)

            // Use trip's timezone for display when available so times show as they were in that zone
            val tripZone = trip.timeZoneId?.let { java.util.TimeZone.getTimeZone(it) }
            if (tripZone != null) {
                dateFormat.timeZone = tripZone
                timeFormat.timeZone = tripZone
                shortDateFormat.timeZone = tripZone
            }

            val start = trip.startTime
            val end = trip.endTime

            // When viewing by date, show "Trip 1", "Trip 2", etc.; otherwise show date
            tripDate.text = if (tripIndex != null) {
                itemView.context.getString(R.string.trip_card_title, tripIndex)
            } else {
                dateFormat.format(end ?: start ?: Date())
            }

            // Subtitle: for spanning trips when viewing by date show "Partial Trip Mar 2–Mar 3"; else time range or "Started X, ended Y"
            // Only append timezone (e.g. "CST") when trip was in a different zone than user's current one
            if (start != null && end != null) {
                val startCal = Calendar.getInstance(tripZone ?: java.util.TimeZone.getDefault()).apply { time = start }
                val endCal = Calendar.getInstance(tripZone ?: java.util.TimeZone.getDefault()).apply { time = end }
                val spansMultipleDays = startCal.get(Calendar.DAY_OF_YEAR) != endCal.get(Calendar.DAY_OF_YEAR) ||
                    startCal.get(Calendar.YEAR) != endCal.get(Calendar.YEAR)
                tripTimeRange.visibility = View.VISIBLE
                val baseText = when {
                    viewingDate != null && spansMultipleDays -> itemView.context.getString(
                        R.string.partial_trip_range,
                        shortDateFormat.format(start),
                        shortDateFormat.format(end)
                    )
                    spansMultipleDays -> itemView.context.getString(
                        R.string.midnight_spanning_format,
                        dateFormat.format(start),
                        dateFormat.format(end)
                    )
                    else -> "${timeFormat.format(start)} - ${timeFormat.format(end)}"
                }
                val showTimezone = trip.timeZoneId != null &&
                    trip.timeZoneId != java.util.TimeZone.getDefault().id
                tripTimeRange.text = if (showTimezone) {
                    val abbr = (tripZone ?: java.util.TimeZone.getDefault())
                        .getDisplayName(false, java.util.TimeZone.SHORT, Locale.US)
                    "$baseText $abbr"
                } else {
                    baseText
                }
            } else {
                tripTimeRange.visibility = View.GONE
            }

            tripMiles.text = String.format(Locale.US, "%.1f mi", trip.actualMiles)
            val oorText = formatOORDisplay(trip.oorMiles, trip.oorPercentage)
            tripOor.text = oorText
            tripOor.contentDescription = itemView.context.getString(R.string.stat_card_oor_description, oorText)

            val showDaySliceCaption = viewingDate != null && trip.isProportionalDaySlice
            if (showDaySliceCaption) {
                tripMilesSliceCaption.visibility = View.VISIBLE
                tripMilesSliceCaption.text = ctx.getString(R.string.trip_stat_slice_miles_caption)
                val milesCd = "${tripMiles.text}. ${tripMilesSliceCaption.text}"
                tripMiles.contentDescription = milesCd
                tripOor.contentDescription =
                    ctx.getString(R.string.stat_card_oor_description, "$oorText. ${tripMilesSliceCaption.text}")
            } else {
                tripMilesSliceCaption.visibility = View.GONE
                tripMiles.contentDescription = tripMiles.text
            }

            metadataSection.visibility = if (isExpanded) View.VISIBLE else View.GONE
            expandButton.setImageResource(
                if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            )

            itemView.setOnClickListener { onExpandToggle(trip.id) }
            expandButton.setOnClickListener { onExpandToggle(trip.id) }

            viewDetailsButton.visibility = if (onTripClick != null) View.VISIBLE else View.GONE
            viewDetailsButton.setOnClickListener { onTripClick?.invoke(trip) }

            deleteButton.setOnClickListener { onDeleteClick(trip) }

            // Metadata section
            tripLoadedMiles.text = String.format(Locale.US, "%.1f mi", trip.loadedMiles)
            tripBounceMiles.text = String.format(Locale.US, "%.1f mi", trip.bounceMiles)
            tripDispatchedMiles.text = String.format(Locale.US, "%.1f mi", trip.loadedMiles + trip.bounceMiles)
            val durationMin = trip.gpsMetadata.tripDurationMinutes.let { if (it > 0) it else trip.durationMinutes }
            tripDuration.text = itemView.context.getString(R.string.duration_minutes_format, durationMin)

            val gps = trip.gpsMetadata

            val startPair = formatLatLngLabel(gps.startLatitude, gps.startLongitude)
            if (startPair != null) {
                tripStartLocationBlock.visibility = View.VISIBLE
                tripStartCoords.text = startPair
            } else {
                tripStartLocationBlock.visibility = View.GONE
            }

            val endPair = formatLatLngLabel(gps.endLatitude, gps.endLongitude)
            if (endPair != null) {
                tripEndLocationBlock.visibility = View.VISIBLE
                tripEndCoords.text = endPair
            } else {
                tripEndLocationBlock.visibility = View.GONE
            }

            val gpsText = buildString {
                if (gps.avgAccuracy > 0) append(String.format(Locale.US, "Accuracy: %.1fm | ", gps.avgAccuracy))
                if (gps.totalPoints > 0) append("Points: ${gps.validPoints}/${gps.totalPoints} | ")
                if (gps.gpsQualityPercentage > 0) append(String.format(Locale.US, "Quality: %.0f%%", gps.gpsQualityPercentage))
                if (gps.accuracyWarnings > 0 || gps.speedAnomalies > 0) {
                    append(" | Warnings: ${gps.accuracyWarnings}, Anomalies: ${gps.speedAnomalies}")
                }
            }
            val gpsDisplay =
                when {
                    showDaySliceCaption && gpsText.isNotEmpty() ->
                        "$gpsText · ${ctx.getString(R.string.trip_gps_metadata_full_trip_hint)}"
                    else -> gpsText
                }

            val insightsBody = buildGpsInsightsLines(ctx, gps, showDaySliceCaption)
            val hasCoordData =
                (gps.startLatitude != null && gps.startLongitude != null) ||
                    (gps.endLatitude != null && gps.endLongitude != null)
            val showInsights = insightsBody.isNotEmpty()

            tripGpsMetadata.text =
                when {
                    gpsDisplay.isNotEmpty() -> gpsDisplay
                    showInsights || hasCoordData -> ctx.getString(R.string.metadata_na)
                    else -> ctx.getString(R.string.no_gps_metadata)
                }

            if (showInsights) {
                tripGpsInsights.visibility = View.VISIBLE
                val sliceNote =
                    if (showDaySliceCaption) {
                        "\n${ctx.getString(R.string.trip_gps_metadata_full_trip_hint)}"
                    } else {
                        ""
                    }
                tripGpsInsights.text = insightsBody + sliceNote
            } else {
                tripGpsInsights.visibility = View.GONE
                tripGpsInsights.text = ""
            }

            // Extended metadata: interstate, back roads, truck stops (show — when no data)
            tripInterstate.text = when {
                gps.interstatePercent > 0 || gps.interstateMinutes > 0 ->
                    itemView.context.getString(R.string.interstate_format, gps.interstatePercent, gps.interstateMinutes)
                else -> itemView.context.getString(R.string.metadata_na)
            }
            tripBackRoads.text = when {
                gps.backRoadsPercent > 0 || gps.backRoadsMinutes > 0 ->
                    itemView.context.getString(R.string.back_roads_format, gps.backRoadsPercent, gps.backRoadsMinutes)
                else -> itemView.context.getString(R.string.metadata_na)
            }
            tripTruckStops.text =
                when {
                    showDaySliceCaption -> ctx.getString(R.string.metadata_na)
                    gps.truckStopsVisited > 0 ->
                        ctx.getString(R.string.truck_stops_format, gps.truckStopsVisited)
                    else -> ctx.getString(R.string.metadata_na)
                }
        }

        private fun formatOORDisplay(oorMiles: Double, oorPercentage: Double): String {
            return when {
                oorMiles > 0.01 -> String.format(Locale.US, "%.1f over (%.1f%%)", oorMiles, oorPercentage)
                oorMiles < -0.01 -> String.format(Locale.US, "%.1f under (%.1f%%)", kotlin.math.abs(oorMiles), kotlin.math.abs(oorPercentage))
                else -> itemView.context.getString(R.string.on_route)
            }
        }

        /** Degrees with N/S/E/W; both lat and lng required (stored together). */
        private fun formatLatLngLabel(lat: Double?, lng: Double?): String? {
            if (lat == null || lng == null) return null
            val latDir = if (lat >= 0) "N" else "S"
            val lngDir = if (lng >= 0) "E" else "W"
            return String.format(
                Locale.US,
                "%.5f° %s, %.5f° %s",
                abs(lat),
                latDir,
                abs(lng),
                lngDir,
            )
        }

        /**
         * Extra trip stats derived from tracking (speed, stops, turns, elevation, etc.).
         * [forDaySlice] suppresses stop/turn/jump lines when the card is a calendar-day slice.
         */
        private fun buildGpsInsightsLines(
            ctx: android.content.Context,
            gps: GpsMetadata,
            forDaySlice: Boolean,
        ): String {
            val lines = mutableListOf<String>()
            if (gps.avgSpeedMph > 0.5) {
                lines.add(ctx.getString(R.string.trip_gps_insight_avg_speed, gps.avgSpeedMph))
            }
            if (gps.maxSpeed > 0.5) {
                lines.add(ctx.getString(R.string.trip_gps_insight_max_speed, gps.maxSpeed))
            }
            if (gps.totalPoints > 0 && !forDaySlice) {
                lines.add(ctx.getString(R.string.trip_gps_insight_stops, gps.stopEventsCount))
                lines.add(ctx.getString(R.string.trip_gps_insight_turns, gps.significantTurnsCount))
            }
            val minEl = gps.elevationMinMeters
            val maxEl = gps.elevationMaxMeters
            if (minEl != null && maxEl != null && maxEl >= minEl) {
                val minFt = minEl * METERS_TO_FEET
                val maxFt = maxEl * METERS_TO_FEET
                lines.add(ctx.getString(R.string.trip_gps_insight_elevation_ft, minFt, maxFt))
            }
            if (gps.distinctTimeZoneCount > 1) {
                lines.add(ctx.getString(R.string.trip_gps_insight_time_zones, gps.distinctTimeZoneCount))
            }
            if (!forDaySlice && gps.locationJumps > 0) {
                lines.add(ctx.getString(R.string.trip_gps_insight_jumps, gps.locationJumps))
            }
            return lines.joinToString("\n")
        }
    }

    private companion object {
        private const val METERS_TO_FEET = 3.28084
    }

    class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Trip, newItem: Trip) = oldItem == newItem
    }
}
