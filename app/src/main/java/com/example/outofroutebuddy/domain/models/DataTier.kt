package com.example.outofroutebuddy.domain.models

/**
 * Upper-echelon data tier for trip (and future) data.
 * Separates data by reliability and retention policy. Data may be promoted or terminated
 * according to tier rules.
 *
 * **SILVER** — Unsure / very unsure / may delete.
 * - Candidate for termination; low confidence.
 * - Synthetic and simulated data may live at this tier and below.
 *
 * **PLATINUM** — Maybe useful in future; subject to demotion or promotion.
 * - Synthetic and simulated data stored at this level and below.
 * - Can be promoted to GOLD (e.g. after human verification) or demoted to SILVER.
 *
 * **GOLD** — Purely human data; digital gold.
 * - Treat with care: parse carefully, copy when needed instead of mutating.
 * - No synthetic/simulated data at this tier.
 */
enum class DataTier {
    /** Unsure / very unsure / may delete; subject to termination. */
    SILVER,

    /** Maybe useful; subject to demotion/promotion; synthetic/simulated data at this level and below. */
    PLATINUM,

    /** Purely human data; digital gold; copy when needed, parse carefully. */
    GOLD,
}
