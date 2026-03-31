package com.example.outofroutebuddy.presentation.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.outofroutebuddy.R

/**
 * **Ludacris settings** — optional high-detail rows on the live trip card.
 * Opened from **Look & feel** in main Settings. Switch keys remain `ludicrous_show_*` in SharedPreferences for upgrades.
 */
class LudacrisSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "app_settings"
        setPreferencesFromResource(R.xml.preferences_ludacris, rootKey)
    }
}
