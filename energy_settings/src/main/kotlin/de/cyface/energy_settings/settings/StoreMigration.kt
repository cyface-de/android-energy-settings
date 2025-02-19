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

import android.util.Log
import androidx.datastore.core.DataMigration
import de.cyface.energy_settings.Constants.TAG
import de.cyface.energy_settings.Settings
import de.cyface.utils.settings.MigrationException

/**
 * Migration which ensures DataStore files from all versions are compatible.
 *
 * @author Armin Schnabel
 * @version 1.0.0
 * @since 3.4.0
 */
class StoreMigration : DataMigration<Settings> {

    /**
     * The current version of the datastore schema. Increase this when migration is necessary,
     * e.g. when you add a new field, to ensure a correct default value is set instead of the
     * Protobuf default value for that data type like "" or 0, 0.0, etc.
     */
    private val currentVersion = 1

    override suspend fun shouldMigrate(currentData: Settings): Boolean {
        // When no previous datastore file exists, the version starts at 0, i.e. this ensures
        // that the correct default values are set instead of the Protobuf default value for that
        // data type like "" or 0, 0.0, etc.
        return currentData.version < currentVersion
    }

    override suspend fun migrate(currentData: Settings): Settings {
        val currentVersion = currentData.version
        val targetVersion = currentVersion + 1
        val logTemplate =
            "Migrating ${Settings::class.java.name} from $currentVersion to $targetVersion"
        Log.i(TAG, String.format(logTemplate, currentVersion, targetVersion))

        val builder = currentData.toBuilder()
        when (currentVersion) {
            0 -> {
                builder
                    .setVersion(targetVersion)
                    .setManufacturerWarningShown(false)
            }

            else -> {
                throw MigrationException("No migration code for version ${currentVersion}.")
            }
        }
        return builder.build()
    }

    override suspend fun cleanUp() {
        // Is called when migration was successful
        // Currently, there is no cleanup to do, yet
    }
}
