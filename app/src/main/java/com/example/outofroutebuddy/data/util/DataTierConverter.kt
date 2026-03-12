package com.example.outofroutebuddy.data.util

import androidx.room.TypeConverter
import com.example.outofroutebuddy.domain.models.DataTier

/**
 * Room TypeConverter for [DataTier].
 * Persists enum as string for readability and safe migrations.
 */
class DataTierConverter {
    @TypeConverter
    fun fromString(value: String?): DataTier {
        return when (value) {
            "SILVER" -> DataTier.SILVER
            "PLATINUM" -> DataTier.PLATINUM
            "GOLD" -> DataTier.GOLD
            else -> DataTier.GOLD // Default for null/legacy/unknown
        }
    }

    @TypeConverter
    fun toString(tier: DataTier): String {
        return tier.name
    }
}
