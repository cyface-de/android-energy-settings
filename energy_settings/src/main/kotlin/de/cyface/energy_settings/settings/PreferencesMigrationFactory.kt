/*
 * Copyright 2023-2025 Cyface GmbH
 *
 * This file is part of the Cyface Energy Settings for Android.
 *
 * The Cyface Energy Settings for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Cyface Energy Settings for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Cyface Energy Settings for Android. If not, see <http://www.gnu.org/licenses/>.
 */
package de.cyface.energy_settings.settings

import android.content.Context
import android.util.Log
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import de.cyface.energy_settings.Constants.TAG
import de.cyface.energy_settings.Settings

/**
 * Factory for the migration which imports preferences from the previously used SharedPreferences.
 *
 * @author Armin Schnabel
 * @version 1.0.0
 * @since 4.3.0
 */
object PreferencesMigrationFactory {
    /**
     * The filename, keys and defaults of the preferences, historically.
     *
     * *Don't change this, this is migration code!*
     */
    private const val PREFERENCES_NAME = "AppPreferences"
    private const val MANUFACTURER_WARNING_KEY =
        "de.cyface.energy_settings.manufacturer_warning_shown"

    /**
     * @param context The context to search and access the old SharedPreferences from.
     * @return The migration code which imports preferences from the SharedPreferences if found.
     */
    fun create(context: Context): SharedPreferencesMigration<Settings> {
        return SharedPreferencesMigration(
            context,
            PREFERENCES_NAME,
            migrate = ::migratePreferences
        )
    }

    private fun migratePreferences(
        preferences: SharedPreferencesView,
        settings: Settings
    ): Settings {
        Log.i(TAG, "Migrating from shared preferences to version 1")
        return settings.toBuilder()
            // Setting version to 1 as it would else default to Protobuf default of 0 which would
            // trigger the StoreMigration from 0 -> 1 which ignores previous settings.
            // This way the last supported version of SharedPreferences is hard-coded here and
            // then the migration steps in StoreMigration starting at version 1 continues from here.
            .setVersion(1)
            .setManufacturerWarningShown(preferences.getBoolean(MANUFACTURER_WARNING_KEY, false))
            .build()
    }
}
