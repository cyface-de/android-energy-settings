/*
 * Copyright 2023 Cyface GmbH
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
package de.cyface.energy_settings

import android.content.Context
import androidx.core.content.edit
import de.cyface.energy_settings.Constants.PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY

/**
 * Custom settings used by this library.
 *
 * Attention:
 * - Never mix SingleProcessDataStore with MultiProcessDataStore for the same file.
 * - We use SingleProcessDataStore, so don't access preferences from multiple processes.
 * - Only create one instance of `DataStore` per file in the same process.
 * - We use ProtoBuf to ensure type safety. Rebuild after changing the .proto file.
 *
 * @author Armin Schnabel
 * @version 2.0.0
 * @since 3.4.0
 */
class CustomPreferences(context: Context) {

    fun saveWarningShown(warningShown: Boolean) {
        //FIXME
        /*preferences.edit {
            putBoolean(PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY, warningShown)
            apply()
        }*/
    }

    fun getWarningShown(): Boolean {
        return true // FIXME
        //return preferences.getBoolean(PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY, false)
    }
}