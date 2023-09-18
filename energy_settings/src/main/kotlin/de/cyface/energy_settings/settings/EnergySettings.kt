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
package de.cyface.energy_settings.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import de.cyface.energy_settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Settings used by this library.
 *
 * We currently don't use a repository to abstract the interface of the data types from the data
 * source. The reason for this is the class is very simple and we don't plan multiple data sources.
 * If this changes, consider using the standard Android Architecture, see `MeasurementRepository`.
 *
 * @author Armin Schnabel
 * @version 2.0.0
 * @since 3.3.4
 * @param context The context to access the settings from.
 */
class EnergySettings(context: Context) {

    /**
     * This avoids leaking the context when this object outlives the Activity of Fragment.
     */
    private val appContext = context.applicationContext

    /**
     * The data store with single-process support.
     *
     * We don't use multi-process support as this should usually only run in the ui process.
     * I.e. there is no need for that overhead.
     *
     * Attention:
     * - Never mix SingleProcessDataStore with MultiProcessDataStore for the same file.
     * - We use SingleProcessDataStore, so don't access preferences from multiple processes.
     * - Only create one instance of `DataStore` per file in the same process.
     * - We use ProtoBuf to ensure type safety. Rebuild after changing the .proto file.
     */
    private var dataStore: DataStore<Settings> = DataStoreFactory.create(
        serializer = SettingsSerializer,
        produceFile = { appContext.dataStoreFile("energy_settings.pb") },
        migrations = listOf(
            PreferencesMigrationFactory.create(appContext),
            StoreMigration()
        )
    )

    /**
     * Saves whether the user marked the manufacturer-specific warning as "don't show again".
     *
     * @param value The boolean value to save.
     */
    @Suppress("unused") // Part of the API
    suspend fun setManufacturerWarningShown(value: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setManufacturerWarningShown(value)
                .build()
        }
    }

    /**
     * @return Whether user marked the manufacturer-specific warning as "don't show again".
     */
    val manufacturerWarningShownFlow: Flow<Boolean> = dataStore.data
        .map { settings ->
            settings.manufacturerWarningShown
        }
}