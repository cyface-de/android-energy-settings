/*
 * Copyright 2019 Cyface GmbH
 *
 * This file is part of the Cyface SDK for Android.
 *
 * The Cyface SDK for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Cyface SDK for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Cyface SDK for Android. If not, see <http://www.gnu.org/licenses/>.
 */
package de.cyface.energy_settings;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * This class is used to identify dialogs which are opened by this library.
 * <p>
 * This is needed to only close such dialogs in {@link TrackingSettings#dismissAllDialogs(FragmentManager)} and to not
 * interfere with other dialogs.
 *
 * @author Armin Schnabel
 * @version 1.0.0
 * @since 1.1.0
 */
abstract class EnergySettingDialog extends DialogFragment {
    // Nothing to do
}
