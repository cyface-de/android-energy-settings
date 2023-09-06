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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Custom settings used by this library.
 *
 * @author Armin Schnabel
 * @version 2.0.0
 * @since 3.3.4
 */
class CustomSettings {

    /**
     * Saves whether the user marked the manufacturer-specific warning as "don't show again".
     *
     * @param value The boolean value to save.
     */
    @Suppress("unused") // Part of the API
    suspend fun setManufacturerWarningShown(value: Boolean) {
        TrackingSettings.dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setManufacturerWarningShown(value)
                .build()
        }
    }

    /**
     * @return Whether user marked the manufacturer-specific warning as "don't show again".
     */
    val manufacturerWarningShownFlow: Flow<Boolean> = TrackingSettings.dataStore.data
        .map { settings ->
            settings.manufacturerWarningShown
        }
}