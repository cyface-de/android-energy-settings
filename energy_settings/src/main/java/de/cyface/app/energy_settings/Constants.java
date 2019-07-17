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
package de.cyface.app.energy_settings;

/**
 * Holds constants required by multiple classes.
 *
 * @author Armin Schnabel
 * @version 1.0.0
 * @since 1.0.0
 */
final class Constants {

    final static String TAG = "de.cyface.app.es";
    private final static String PACKAGE = "de.cyface.app.energy_settings";

    // Dialog codes to identify the different dialogs
    final static int DIALOG_ENERGY_SAFER_WARNING_CODE = 2019071101;
    final static int DIALOG_BACKGROUND_RESTRICTION_WARNING_CODE = 2019071102;
    final static int DIALOG_PROBLEMATIC_MANUFACTURER_WARNING_CODE = 2019071103;
    final static int DIALOG_GPS_DISABLED_WARNING_CODE = 2019071104;
    final static int DIALOG_NO_GUIDANCE_NEEDED_DIALOG_CODE = 2019071105;

    // Preference key for shared preferences
    final static String PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY = PACKAGE + ".manufacturer_warning_shown";

    // Manufacturer names
    final static String MANUFACTURER_HUAWEI = "huawei";
    final static String MANUFACTURER_HONOR = "honor"; // sub brand of huawei
    final static String MANUFACTURER_SAMSUNG = "samsung";
    final static String MANUFACTURER_XIAOMI = "xiaomi";
    final static String MANUFACTURER_SONY = "sony";

    // The following manufacturers seem to be as restrictive as Huawei etc. (see https://dontkillmyapp.com)
    // but we have no negative reports yet from such devices, nor can we test them.
    // For those reasons this part is uncommented but we keep this info here as it's some work to collect this data.
    // public final static String MANUFACTURER_HTC = "htc";
    // public final static String MANUFACTURER_OPPO = "oppo";
    // public final static String MANUFACTURER_ASUS = "asus";
    // public final static String MANUFACTURER_LETV = "letv";
    // public final static String MANUFACTURER_VIVO = "vivo";
    // public final static String MANUFACTURER_MEIZU = "meizu";
    // public final static String MANUFACTURER_DEWAV = "dewav";
    // public final static String MANUFACTURER_QMOBILE = "qmobile";
}
