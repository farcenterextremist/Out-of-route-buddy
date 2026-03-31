package com.example.outofroutebuddy.domain.models

/**
 * Realistic driver archetypes for virtual fleet simulation.
 * Each archetype defines behavioral parameters grounded in real-world
 * delivery/trucking data (ATRI operational costs, DSP route data, gig platform stats).
 *
 * OOR ranges, mile ranges, and GPS quality are calibrated against:
 * - Industry average OOR deviation ~4.34% (Per Diem Plus / ATRI)
 * - Amazon DSP routes: 120-220 mi, long-haul: 180-740 mi
 * - Gig platforms: 50-150 mi per multi-stop block
 * - Fleet telematics GPS quality benchmarks (80%+ = reliable)
 */
enum class DriverArchetype(
    val displayName: String,
    val description: String,
    val minPlannedMiles: Double,
    val maxPlannedMiles: Double,
    val minOorPercent: Double,
    val maxOorPercent: Double,
    val oorStdDev: Double,
    val avgTripsPerDay: Double,
    val gpsQualityRange: ClosedFloatingPointRange<Double>,
    val avgSpeedMph: Double,
    val nightRunProbability: Double,
    val improvementTrend: Double,
) {
    /**
     * 15+ years experience, knows every shortcut that actually works.
     * Consistently low OOR because they've already optimized their routes.
     */
    VETERAN(
        displayName = "The Veteran",
        description = "15yr experience, rock-steady, rarely deviates",
        minPlannedMiles = 400.0,
        maxPlannedMiles = 650.0,
        minOorPercent = 1.0,
        maxOorPercent = 3.0,
        oorStdDev = 0.6,
        avgTripsPerDay = 1.5,
        gpsQualityRange = 88.0..98.0,
        avgSpeedMph = 58.0,
        nightRunProbability = 0.15,
        improvementTrend = 0.0,
    ),

    /**
     * New to the job, still learning routes and making mistakes.
     * High variability but trending downward over time.
     */
    ROOKIE(
        displayName = "The Rookie",
        description = "Learning routes, high variability, improving over time",
        minPlannedMiles = 200.0,
        maxPlannedMiles = 400.0,
        minOorPercent = 6.0,
        maxOorPercent = 12.0,
        oorStdDev = 2.5,
        avgTripsPerDay = 1.2,
        gpsQualityRange = 72.0..90.0,
        avgSpeedMph = 50.0,
        nightRunProbability = 0.05,
        improvementTrend = -0.15,
    ),

    /**
     * Follows GPS religiously, never takes an unplanned detour.
     * Lowest OOR in the fleet, almost robotic adherence.
     */
    EFFICIENT(
        displayName = "The Efficient",
        description = "GPS-obsessed, refuses detours, best OOR in fleet",
        minPlannedMiles = 300.0,
        maxPlannedMiles = 550.0,
        minOorPercent = 0.5,
        maxOorPercent = 2.0,
        oorStdDev = 0.4,
        avgTripsPerDay = 1.8,
        gpsQualityRange = 92.0..99.0,
        avgSpeedMph = 55.0,
        nightRunProbability = 0.10,
        improvementTrend = 0.0,
    ),

    /**
     * Always "knows a better way", stops for food, fuel, rest areas.
     * High OOR but swears each detour was necessary.
     */
    DETOUR_KING(
        displayName = "The Detour King",
        description = "Always 'knows a shortcut', stops for food/fuel",
        minPlannedMiles = 250.0,
        maxPlannedMiles = 500.0,
        minOorPercent = 8.0,
        maxOorPercent = 20.0,
        oorStdDev = 4.0,
        avgTripsPerDay = 1.0,
        gpsQualityRange = 75.0..92.0,
        avgSpeedMph = 48.0,
        nightRunProbability = 0.10,
        improvementTrend = -0.05,
    ),

    /**
     * Prefers late shifts, less traffic but more construction zones
     * and unfamiliar road conditions in the dark.
     */
    NIGHT_OWL(
        displayName = "The Night Owl",
        description = "Night shifts, less traffic but unfamiliar conditions",
        minPlannedMiles = 300.0,
        maxPlannedMiles = 500.0,
        minOorPercent = 3.0,
        maxOorPercent = 6.0,
        oorStdDev = 1.2,
        avgTripsPerDay = 1.3,
        gpsQualityRange = 78.0..93.0,
        avgSpeedMph = 52.0,
        nightRunProbability = 0.85,
        improvementTrend = -0.02,
    ),

    /**
     * Short urban delivery runs with frequent stops.
     * High OOR percentage but low absolute OOR miles.
     */
    LOCAL(
        displayName = "The Local",
        description = "Short urban runs, high OOR% but low absolute miles",
        minPlannedMiles = 80.0,
        maxPlannedMiles = 200.0,
        minOorPercent = 4.0,
        maxOorPercent = 8.0,
        oorStdDev = 1.5,
        avgTripsPerDay = 3.0,
        gpsQualityRange = 80.0..95.0,
        avgSpeedMph = 32.0,
        nightRunProbability = 0.05,
        improvementTrend = -0.03,
    ),

    /**
     * Long interstate hauls with minimal deviation.
     * Highway driving = fewer route choices = less OOR.
     */
    HIGHWAY_WARRIOR(
        displayName = "The Highway Warrior",
        description = "Interstate long-haul, minimal deviation",
        minPlannedMiles = 500.0,
        maxPlannedMiles = 800.0,
        minOorPercent = 1.0,
        maxOorPercent = 4.0,
        oorStdDev = 0.8,
        avgTripsPerDay = 0.8,
        gpsQualityRange = 85.0..97.0,
        avgSpeedMph = 62.0,
        nightRunProbability = 0.30,
        improvementTrend = 0.0,
    ),

    /**
     * Multi-stop gig delivery (DoorDash/Flex style).
     * Most variable OOR because each stop is a mini-route decision.
     */
    GIG_RUNNER(
        displayName = "The Gig Runner",
        description = "Multi-stop delivery, most variable OOR",
        minPlannedMiles = 50.0,
        maxPlannedMiles = 150.0,
        minOorPercent = 5.0,
        maxOorPercent = 15.0,
        oorStdDev = 3.5,
        avgTripsPerDay = 5.0,
        gpsQualityRange = 70.0..90.0,
        avgSpeedMph = 28.0,
        nightRunProbability = 0.20,
        improvementTrend = -0.08,
    ),
    ;

    val midOorPercent: Double
        get() = (minOorPercent + maxOorPercent) / 2.0

    val midPlannedMiles: Double
        get() = (minPlannedMiles + maxPlannedMiles) / 2.0
}
