package de.cyface.energy_settings

import android.content.Context
import androidx.core.content.edit
import de.cyface.energy_settings.Constants.PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY
import de.cyface.utils.AppPreferences

class CustomPreferences(context: Context): AppPreferences(context) {

    fun saveWarningShown(warningShown: Boolean) {
        preferences.edit {
            putBoolean(PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY, warningShown)
            apply()
        }
    }

    fun getWarningShown(): Boolean {
        return preferences.getBoolean(PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY, false)
    }
}