package com.example.outofroutebuddy.services

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.data.TripStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Decides when to show the "trip ended" overlay bubble.
 * Combines: trip active, miles driven vs allotted, optional min duration/distance, rate limiting.
 */
class TripEndedDetector(
    private val context: Context,
    private val tripStateManager: TripStateManager,
    private val config: Config = Config(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Emits when the overlay bubble should be shown (at most once per trigger, rate-limited). */
    private val _showBubble = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val showBubble: SharedFlow<Unit> = _showBubble
    private val _showArrivalPrompt = MutableSharedFlow<PromptRequest>(extraBufferCapacity = 1)
    val showArrivalPrompt: SharedFlow<PromptRequest> = _showArrivalPrompt

    private val alreadyFiredForCurrentTrip = AtomicBoolean(false)
    private val promptIssuedForCurrentTrip = AtomicBoolean(false)
    private val _fsmState = MutableStateFlow(DetectorState.IN_PROGRESS)
    val fsmState: StateFlow<DetectorState> = _fsmState.asStateFlow()
    private var candidateEnteredAt = 0L
    @Volatile
    private var latestFeedbackSummary = FeedbackSummary()

    data class Config(
        val milesToleranceMi: Double = 0.3,
        val minTripDurationMs: Long = 5 * 60 * 1000L,
        val minActualMiles: Double = 0.5,
        val cooldownAfterNoContinueMs: Long = 15 * 60 * 1000L,
        val percentThreshold: Double = 98.0,
        val minCompletionRatioForEnding: Double = 0.90,
        /** Enter CANDIDATE_ENDING when confidence crosses this. */
        val candidateConfidenceThreshold: Double = 0.6,
        /** Fire bubble only when confidence stays at or above this (real-world: 0.72 so noisy GPS/activity don't block). */
        val endingConfidenceThreshold: Double = 0.72,
        /** Leave CANDIDATE_ENDING if confidence drops below this. */
        val leaveCandidateThreshold: Double = 0.45,
        /** Min time in CANDIDATE_ENDING before we can transition to ENDING_DETECTED (real-world: 20s). */
        val candidateDebounceMs: Long = 20_000L,
        /** Total time we must stay in CANDIDATE_ENDING with high confidence before firing (real-world: 50s). */
        val endingDebounceMs: Long = 50_000L,
        /** In-vehicle confidence above this = "movement resumed", reset candidate (real-world: 85 so delayed "still" doesn't cancel). */
        val movementResumedInVehicleConfidence: Int = 85,
        /** If trip completion is below this and we lack strong stop evidence, do not prompt yet. */
        val minCompletionRatioForEarlyPrompt: Double = 0.55,
    )

    data class PromptRequest(
        val confidence: Double,
        val feedbackSummary: FeedbackSummary,
    )

    data class FeedbackSummary(
        val confidenceBucket: String = "low",
        val completionBucket: String = "under_55",
        val speedBucket: String = "slow",
        val rollingDistanceBucket: String = "very_low",
        val dwellBucket: String = "none",
        val activityBucket: String = "unknown",
        val nearOrigin: Boolean = false,
        val hasDirectSpeed: Boolean = false,
    )

    enum class DetectorState {
        IN_PROGRESS,
        CANDIDATE_ENDING,
        ENDING_DETECTED,
        ENDED_CONFIRMED,
        RESUMED_CONTINUE_COOLDOWN,
    }

    init {
        combine(
            tripStateManager.tripState,
            TripTrackingService.tripMetrics,
            TripTrackingService.driveState,
            TripTrackingService.tripEndingSignalSnapshot,
            TripTrackingService.activityTransitionSignal,
            TripTrackingService.geofenceContextSignal,
        ) { values ->
            val state = values[0] as TripStateManager.TripState
            val metrics = values[1] as TripMetrics
            val driveState = values[2] as DriveState
            val signals = values[3] as TripEndingSignalSnapshot
            val activity = values[4] as ActivityTransitionSignal
            val geofence = values[5] as GeofenceContextSignal
            DetectionInput(
                state = state,
                metrics = metrics,
                driveState = driveState,
                signals = signals,
                activity = activity,
                geofence = geofence,
                nowMillis = System.currentTimeMillis(),
            )
        }
            .filter { it.state.isActive }
            .onEach { input ->
                evaluateStateMachine(input)
            }
            .launchIn(scope)
    }

    private data class DetectionInput(
        val state: TripStateManager.TripState,
        val metrics: TripMetrics,
        val driveState: DriveState,
        val signals: TripEndingSignalSnapshot,
        val activity: ActivityTransitionSignal,
        val geofence: GeofenceContextSignal,
        val nowMillis: Long,
    )

    private fun evaluateStateMachine(input: DetectionInput) {
        val previousState = _fsmState.value
        val cooldownStartedAt = prefs.getLong(KEY_USER_CHOSE_NO_AT, 0L)
        val cooldownActive = cooldownStartedAt > 0 &&
            (input.nowMillis - cooldownStartedAt) < config.cooldownAfterNoContinueMs
        val confidence = calculateEndConfidence(input, cooldownActive)
        latestFeedbackSummary = buildFeedbackSummary(input, confidence)
        val movementResumed = confidence < config.leaveCandidateThreshold ||
            input.signals.speedMph > 8.0 ||
            input.activity.inVehicleConfidence >= config.movementResumedInVehicleConfidence

        when (_fsmState.value) {
            DetectorState.IN_PROGRESS,
            DetectorState.RESUMED_CONTINUE_COOLDOWN,
            DetectorState.ENDED_CONFIRMED,
            -> {
                if (cooldownActive) {
                    _fsmState.value = DetectorState.RESUMED_CONTINUE_COOLDOWN
                    return
                }
                if (confidence >= config.candidateConfidenceThreshold) {
                    candidateEnteredAt = input.nowMillis
                    _fsmState.value = DetectorState.CANDIDATE_ENDING
                } else {
                    _fsmState.value = DetectorState.IN_PROGRESS
                }
            }
            DetectorState.CANDIDATE_ENDING -> {
                if (cooldownActive) {
                    _fsmState.value = DetectorState.RESUMED_CONTINUE_COOLDOWN
                    return
                }
                if (movementResumed) {
                    _fsmState.value = DetectorState.IN_PROGRESS
                    candidateEnteredAt = 0L
                    promptIssuedForCurrentTrip.set(false)
                    return
                }
                val candidateAge = input.nowMillis - candidateEnteredAt
                if (
                    candidateAge >= config.candidateDebounceMs &&
                    confidence >= config.candidateConfidenceThreshold &&
                    promptIssuedForCurrentTrip.compareAndSet(false, true)
                ) {
                    _showArrivalPrompt.tryEmit(
                        PromptRequest(
                            confidence = confidence,
                            feedbackSummary = latestFeedbackSummary,
                        ),
                    )
                }
                if (
                    candidateAge >= config.candidateDebounceMs &&
                    confidence >= config.endingConfidenceThreshold &&
                    candidateAge >= config.endingDebounceMs
                ) {
                    _fsmState.value = DetectorState.ENDING_DETECTED
                    if (alreadyFiredForCurrentTrip.compareAndSet(false, true)) {
                        _showBubble.tryEmit(Unit)
                        prefs.edit().putLong(KEY_LAST_SHOWN_AT, input.nowMillis).apply()
                        Log.d(TAG, "Trip-ended detector: emit show bubble (confidence=$confidence)")
                    }
                }
            }
            DetectorState.ENDING_DETECTED -> {
                if (movementResumed) {
                    promptIssuedForCurrentTrip.set(false)
                    _fsmState.value = if (cooldownActive) {
                        DetectorState.RESUMED_CONTINUE_COOLDOWN
                    } else {
                        DetectorState.IN_PROGRESS
                    }
                }
            }
        }
        if (previousState != _fsmState.value) {
            Log.d(
                TAG,
                "Detector transition ${previousState.name} -> ${_fsmState.value.name} " +
                    "confidence=${"%.2f".format(confidence)} speed=${"%.1f".format(input.signals.speedMph)} " +
                    "still=${input.activity.stillConfidence} inVehicle=${input.activity.inVehicleConfidence} " +
                    "nearOrigin=${input.geofence.isNearOrigin} dwellMs=${input.geofence.dwellInOriginMillis}",
            )
        }
    }

    private fun calculateEndConfidence(input: DetectionInput, cooldownActive: Boolean): Double {
        if (cooldownActive) return 0.0
        if (!input.state.isActive) return 0.0

        val allottedMiles = (input.state.loadedMiles.toDoubleOrNull() ?: 0.0) +
            (input.state.bounceMiles.toDoubleOrNull() ?: 0.0)
        if (allottedMiles <= 0.0) return 0.0

        val legacyBubble = shouldShowBubble(
            state = input.state,
            metrics = input.metrics,
            now = input.nowMillis,
            lastUserChoseNoAtMillis = prefs.getLong(KEY_USER_CHOSE_NO_AT, 0L),
            config = config,
        )
        val completionRatio = input.metrics.totalMiles / allottedMiles
        val strongStopEvidence =
            input.signals.sustainedStopMillis >= 60_000L ||
                (
                    input.signals.lowMotionSampleCount >= 3 &&
                        input.signals.rollingDistanceMeters <= 80.0 &&
                        input.signals.speedMph <= 3.0
                    )
        if (!legacyBubble && completionRatio < config.minCompletionRatioForEarlyPrompt && !strongStopEvidence) {
            return 0.0
        }
        return computeEndConfidence(
            input = ConfidenceInput(
                ratio = completionRatio,
                speedMph = input.signals.speedMph,
                rollingDistanceMeters = input.signals.rollingDistanceMeters,
                headingVarianceDeg = input.signals.headingVarianceDeg,
                sustainedStopMillis = input.signals.sustainedStopMillis,
                lowMotionSampleCount = input.signals.lowMotionSampleCount,
                isNearOrigin = input.geofence.isNearOrigin,
                originDwellMillis = input.geofence.dwellInOriginMillis,
                isWalkingOrStill = input.driveState == DriveState.WALKING_OR_STATIONARY,
                stillConfidence = input.activity.stillConfidence,
                inVehicleConfidence = input.activity.inVehicleConfidence,
                gpsAccuracyMeters = input.signals.gpsAccuracyMeters,
                hasDirectSpeed = input.signals.hasDirectSpeed,
                legacyBubbleTriggered = legacyBubble,
            ),
            completionRatioSoftGate = config.minCompletionRatioForEnding,
        )
    }

    /** Call when user taps "No, continue trip" so we cooldown before re-triggering. */
    fun onUserChoseNoContinueTrip() {
        prefs.edit().putLong(KEY_USER_CHOSE_NO_AT, System.currentTimeMillis()).apply()
        alreadyFiredForCurrentTrip.set(false)
        promptIssuedForCurrentTrip.set(false)
        _fsmState.value = DetectorState.RESUMED_CONTINUE_COOLDOWN
        Log.d(TAG, "Trip-ended detector: user chose no, cooldown set")
    }

    /** Call when trip ends (user completed or cleared) so we can fire again for next trip. */
    fun onTripEnded() {
        prefs.edit().remove(KEY_USER_CHOSE_NO_AT).apply()
        alreadyFiredForCurrentTrip.set(false)
        promptIssuedForCurrentTrip.set(false)
        _fsmState.value = DetectorState.ENDED_CONFIRMED
        candidateEnteredAt = 0L
        latestFeedbackSummary = FeedbackSummary()
    }

    fun getLatestFeedbackSummary(): FeedbackSummary = latestFeedbackSummary

    private fun buildFeedbackSummary(input: DetectionInput, confidence: Double): FeedbackSummary {
        val activityBucket =
            when {
                input.activity.inVehicleConfidence >= 80 -> "in_vehicle_high"
                input.activity.stillConfidence >= 70 || input.driveState == DriveState.WALKING_OR_STATIONARY -> "still_high"
                else -> "unknown"
            }
        return FeedbackSummary(
            confidenceBucket = bucketConfidence(confidence),
            completionBucket = bucketCompletion(input.metrics.totalMiles, input.state),
            speedBucket = bucketSpeed(input.signals.speedMph),
            rollingDistanceBucket = bucketRollingDistance(input.signals.rollingDistanceMeters),
            dwellBucket = bucketDwell(input.signals.sustainedStopMillis),
            activityBucket = activityBucket,
            nearOrigin = input.geofence.isNearOrigin,
            hasDirectSpeed = input.signals.hasDirectSpeed,
        )
    }

    companion object {
        private const val TAG = "TripEndedDetector"
        private const val PREFS_NAME = "trip_ended_overlay"
        private const val KEY_LAST_SHOWN_AT = "last_shown_at"
        private const val KEY_USER_CHOSE_NO_AT = "user_chose_no_at"

        internal data class ConfidenceInput(
            val ratio: Double,
            val speedMph: Double,
            val rollingDistanceMeters: Double,
            val headingVarianceDeg: Double,
            val sustainedStopMillis: Long,
            val lowMotionSampleCount: Int,
            val isNearOrigin: Boolean,
            val originDwellMillis: Long,
            val isWalkingOrStill: Boolean,
            val stillConfidence: Int,
            val inVehicleConfidence: Int,
            val gpsAccuracyMeters: Float,
            val hasDirectSpeed: Boolean,
            val legacyBubbleTriggered: Boolean,
        )

        internal fun computeEndConfidence(
            input: ConfidenceInput,
            completionRatioSoftGate: Double = 0.90,
        ): Double {
            var score = 0.0
            if (input.legacyBubbleTriggered) score += 0.32
            if (input.ratio >= 0.98) score += 0.14 else if (input.ratio >= 0.9) score += 0.08 else if (input.ratio >= 0.75) score += 0.04
            if (input.ratio >= 1.0) score += 0.04 // Over allotted = stronger "trip done"
            if (input.speedMph <= 1.0) score += 0.12 else if (input.speedMph <= 3.0) score += 0.08
            if (input.rollingDistanceMeters <= 80.0) score += 0.1
            if (input.headingVarianceDeg <= 10.0) score += 0.05
            if (input.sustainedStopMillis >= 120_000L) score += 0.18 else if (input.sustainedStopMillis >= 60_000L) score += 0.12 else if (input.sustainedStopMillis >= 30_000L) score += 0.06
            if (input.lowMotionSampleCount >= 3) score += 0.06
            if (input.isNearOrigin) score += 0.08
            if (input.originDwellMillis >= 120_000L) score += 0.1
            if (input.isWalkingOrStill) score += 0.08
            if (input.stillConfidence >= 70) score += 0.12
            if (input.inVehicleConfidence >= 80) score -= 0.2 // Only penalize when strongly "in vehicle" (real-world: API lags)
            if (input.gpsAccuracyMeters > 60f) score -= 0.08 // Only penalize poor GPS (real-world: 50–60m common)
            if (!input.hasDirectSpeed && input.rollingDistanceMeters > 140.0) score -= 0.08
            if (!input.legacyBubbleTriggered && input.ratio < completionRatioSoftGate) score -= 0.1
            if (input.speedMph >= 12.0) score = 0.0
            return score.coerceIn(0.0, 1.0)
        }

        private fun bucketConfidence(confidence: Double): String =
            when {
                confidence >= 0.85 -> "very_high"
                confidence >= 0.72 -> "high"
                confidence >= 0.6 -> "medium"
                confidence >= 0.4 -> "low"
                else -> "very_low"
            }

        private fun bucketSpeed(speedMph: Double): String =
            when {
                speedMph <= 1.0 -> "stopped"
                speedMph <= 3.0 -> "slow"
                speedMph <= 8.0 -> "rolling"
                else -> "moving"
            }

        private fun bucketRollingDistance(distanceMeters: Double): String =
            when {
                distanceMeters <= 40.0 -> "very_low"
                distanceMeters <= 80.0 -> "low"
                distanceMeters <= 160.0 -> "medium"
                else -> "high"
            }

        private fun bucketDwell(dwellMillis: Long): String =
            when {
                dwellMillis >= 120_000L -> "120s_plus"
                dwellMillis >= 60_000L -> "60_119s"
                dwellMillis >= 30_000L -> "30_59s"
                else -> "under_30s"
            }

        private fun bucketCompletion(
            totalMiles: Double,
            state: TripStateManager.TripState,
        ): String {
            val loaded = state.loadedMiles.toDoubleOrNull() ?: 0.0
            val bounce = state.bounceMiles.toDoubleOrNull() ?: 0.0
            val allotted = loaded + bounce
            if (allotted <= 0.0) return "unknown"
            val ratio = totalMiles / allotted
            return when {
                ratio >= 1.0 -> "100_plus"
                ratio >= 0.9 -> "90_99"
                ratio >= 0.75 -> "75_89"
                ratio >= 0.55 -> "55_74"
                else -> "under_55"
            }
        }

        /**
         * Pure logic: should the overlay be shown? Used by detector and by scenario tests.
         * @param lastUserChoseNoAtMillis 0 if user has not recently chosen "No, continue"; else timestamp.
         */
        internal fun shouldShowBubble(
            state: TripStateManager.TripState,
            metrics: TripMetrics,
            now: Long,
            lastUserChoseNoAtMillis: Long,
            config: Config = Config(),
        ): Boolean {
            if (!state.isActive) return false
            val loaded = state.loadedMiles.toDoubleOrNull() ?: 0.0
            val bounce = state.bounceMiles.toDoubleOrNull() ?: 0.0
            val allotted = loaded + bounce
            if (allotted <= 0) return false

            val totalMiles = metrics.totalMiles
            if (totalMiles < config.minActualMiles) return false

            val startTime = state.startTime?.time ?: return false
            if (now - startTime < config.minTripDurationMs) return false

            if (lastUserChoseNoAtMillis > 0 && (now - lastUserChoseNoAtMillis) < config.cooldownAfterNoContinueMs) return false

            val ratio = totalMiles / allotted
            val meetsPercent = ratio * 100 >= config.percentThreshold
            val meetsTolerance = totalMiles >= allotted - config.milesToleranceMi
            return meetsPercent || meetsTolerance
        }
    }
}
