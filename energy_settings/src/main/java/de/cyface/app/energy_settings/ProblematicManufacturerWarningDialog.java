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

import static de.cyface.app.energy_settings.Constants.MANUFACTURER_SONY;
import static de.cyface.app.energy_settings.Constants.PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY;
import static de.cyface.app.energy_settings.Constants.TAG;

import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog to show a warning when a phone manufacturer is identified which allows to prevent background tracking
 * by manufacturer-specific settings.
 * <p>
 * This dialog is customized by searching for a match in a known list of manufacturer-specific energy settings pages.
 * If a match is found the required steps to adjust the settings are shown and the setting page is accessible via a
 * dialog button. Else, a generic text is shown in the dialog to help the user finding the energy settings by itself.
 * <p>
 * This dialog also contains a button which allows the user to disable the auto-popup of this dialog.
 * This preference is not respected when the user requests guidance explicitly.
 *
 * @author Armin Schnabel
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProblematicManufacturerWarningDialog extends DialogFragment {

    /**
     * The e-mail address to which the feedback email should be addressed to in the generated template.
     */
    private final String recipientEmail;

    /**
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     *            template.
     */
    ProblematicManufacturerWarningDialog(@NonNull final String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        // Generic dialog which is shown when no manufacturer-specific energy setting page is found below
        int dialogTextId = R.string.dialog_manufacturer_warning_generic;

        // Generate dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_problematic_manufacturer_warning_title);

        // Allow the user to express its preference to disable auto-popup of this dialog
        builder.setNegativeButton(R.string.dialog_button_do_not_show_again, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                preferences.edit().putBoolean(PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY, true).apply();
            }
        });

        // Context-less scenario
        final Context context = getContext();
        if (context == null) {
            Log.w(TAG, "No ProblematicManufacturerWarningDialog shown, context is null.");
            builder.setMessage(dialogTextId);
            return builder.create();
        }

        // Try to find device specific settings intent
        final Map.Entry<Intent, Integer> deviceSpecificIntent = getDeviceSpecificIntent(context);

        // If a device specific settings page was found, add a button to open it directly
        if (deviceSpecificIntent != null) {
            dialogTextId = deviceSpecificIntent.getValue();
            final Intent settingsIntent = deviceSpecificIntent.getKey();
            builder.setPositiveButton(R.string.dialog_button_open_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(settingsIntent);
                }
            });
            builder.setMessage(dialogTextId);
            return builder.create();
        }

        // Show Sony STAMINA specific dialog (no manufacturer specific intent name known yet)
        if (Build.MANUFACTURER.toLowerCase().equals(MANUFACTURER_SONY)) {
            dialogTextId = R.string.dialog_manufacturer_warning_sony_stamina;
        }

        // Generate a feedback button
        builder.setPositiveButton(R.string.dialog_button_help, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Intent emailIntent = TrackingSettings.generateFeedbackEmailIntent(context,
                        getString(R.string.feedback_error_description), recipientEmail);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback_choose_email_app)));
            }
        });

        builder.setMessage(dialogTextId);
        return builder.create();
    }

    /**
     * Searches for a match of a list of known device-specific energy setting pages.
     *
     * @param context The {@link Context} to check if the intent is resolvable on this device
     * @return The intent to open the settings page and the resource id of the message string which describes what to do
     *         on the settings page. Returns {@code Null} if no intent was resolved.
     */
    @Nullable
    private static Map.Entry<Intent, Integer> getDeviceSpecificIntent(@NonNull final Context context) {
        final Map<Intent, Integer> intentMap = new ArrayMap<>();

        /*
         * Huawei/Honor
         * - EMUI OS (https://de.wikipedia.org/wiki/EMUI#Versionen)
         *******************************************************************************************/

        // "Protected Apps", EMUI <5, Android <7
        intentMap.put(new Intent().setComponent(new ComponentName("com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity")),
                R.string.dialog_manufacturer_warning_huawei_protected_app);

        // P20, EMUI 9, Android 9, 2018 - comment in https://stackoverflow.com/a/35220476/5815054
        intentMap.put(new Intent().setComponent(new ComponentName("com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
                R.string.dialog_manufacturer_warning_huawei_app_launch);

        // P20, EMUI 9, Android 9, 2019 - comment in https://stackoverflow.com/a/48641229/5815054
        // when adding the permissions above does not work with the intent above
        intentMap.put(new Intent().setComponent(new ComponentName("com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
                R.string.dialog_manufacturer_warning_huawei_app_launch);

        /*
         * Samsung
         * - One UI (https://de.wikipedia.org/wiki/One_UI)
         * - TouchWiz (https://en.wikipedia.org/wiki/TouchWiz)
         *******************************************************************************************/

        // Android 7+, "Unmonitored Apps"
        // - http://sleep.urbandroid.org/documentation/faq/alarms-sleep-tracking-dont-work/#sleeping-apps
        // - Unable to test if this is necessary as we don't have a Samsung Android 7 device
        intentMap.put(new Intent().setComponent(new ComponentName("com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity")),
                R.string.dialog_manufacturer_warning_samsung_device_care);

        // Android 5-6 - https://code.briarproject.org/briar/briar/issues/1100
        // - tested: settings found and opened on Android 6.0.1 (office phone: S5 neo)
        // - by default (our phone) this was off but people might have enabled both general and app-specific option
        intentMap.put(new Intent().setComponent(new ComponentName("com.samsung.android.sm",
                "com.samsung.android.sm.ui.battery.BatteryActivity")),
                R.string.dialog_manufacturer_warning_samsung_smart_manager);

        /*
         * Xiaomi
         * - MIUI (https://en.wikipedia.org/wiki/MIUI#Version_history)
         *******************************************************************************************/

        // Tested on Redmi Note 5 (Android 8.1, MIUI 10)
        intentMap.put(new Intent().setComponent(new ComponentName("com.miui.securitycenter",
                "com.miui.powercenter.PowerSettings")),
                R.string.dialog_manufacturer_warning_xiaomi_power_settings);

        intentMap.put(
                new Intent().setComponent(new ComponentName("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                R.string.dialog_manufacturer_warning_xiaomi_auto_start);

        // from https://github.com/dirkam/backgroundable-android
        // Unsure if this is the correct dialog
        intentMap.put(new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT),
                R.string.dialog_manufacturer_warning_xiaomi_power_settings);

        // Unsure if this is the correct dialog
        intentMap.put(new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
                R.string.dialog_manufacturer_warning_xiaomi_auto_start);

        /*
         * Other manufacturers
         * - The following manufacturers seem to be as restrictive as Huawei etc. (see https://dontkillmyapp.com)
         * - but we have no negative reports yet from such devices, nor can we test them.
         * - For those reasons this part is uncommented but we keep this info here as it's some work to collect this
         * data.
         *******************************************************************************************/

        /*
         * HTC
         * - Boost+ App https://www.htc.com/de/support/htc-one-a9s/howto/830580.html
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.htc.pitroad",
         * "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *******************************************************************************************/

        /*
         * Oppo
         *
         * Might require the following permissions:
         * From comment in https://stackoverflow.com/a/48641229/5815054 - required for
         * com.coloros.safecenter/.startupapp.StartupAppListActivity
         * <uses-permission android:name="oppo.permission.OPPO_COMPONENT_SAFE"/>
         * From comment in https://stackoverflow.com/a/48641229/5815054 - P30 Pro (VOG-L29) requires
         * <uses-permission android:name="com.huawei.permission.external_app_settings.USE_COMPONENT"/>
         * From https://stackoverflow.com/a/51726040/5815054
         * <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.coloros.safecenter",
         * "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * // From https://stackoverflow.com/a/51726040/5815054 and https://github.com/dirkam/backgroundable-android
         * // Android >= 7 (ColorOS >= 3)
         * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
         * intentMap.put(new Intent().setComponent(new ComponentName("com.coloros.safecenter",
         * "com.coloros.safecenter.startupapp.StartupAppListActivity")).setAction(Settings.
         * /* VIOLATION WARNING: ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS * /
         * ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
         * .setData(Uri.parse("package:" + context.getPackageName())),
         * R.string.dialog_manufacturer_warning_generic);
         * }
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.oppo.safe",
         * "com.oppo.safe.permission.startup.StartupAppListActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * // From https://stackoverflow.com/a/51726040/5815054
         * intentMap.put(new Intent().setComponent(new ComponentName("com.coloros.oppoguardelf",
         * "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.coloros.oppoguardelf",
         * "com.coloros.powermanager.fuelgaue.PowerSaverModeActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.coloros.oppoguardelf",
         * "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         */

        /*
         * Asus
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.asus.mobilemanager",
         * "com.asus.mobilemanager.MainActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * // From https://stackoverflow.com/a/51726040/5815054
         * intentMap.put(new Intent().setComponent(new ComponentName("com.asus.mobilemanager",
         * "com.asus.mobilemanager.entry.FunctionActivity"))
         * // From https://stackoverflow.com/a/49110392/5815054
         * .setData(Uri.parse("mobilemanager://function/entry/AutoStart")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.asus.mobilemanager",
         * "com.asus.mobilemanager.autostart.AutoStartActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         */

        /*
         * Letv
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.letv.android.letvsafe",
         * "com.letv.android.letvsafe.AutobootManageActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         */

        /*
         * Meizu
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.meizu.safe",
         * "com.meizu.safe.security.SHOW_APPSEC"))
         * .addCategory(Intent.CATEGORY_DEFAULT).putExtra("packageName", BuildConfig.APPLICATION_ID),
         * R.string.dialog_manufacturer_warning_generic);
         */

        /*
         * Vivo
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.iqoo.secure",
         * "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.vivo.permissionmanager",
         * "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
         * R.string.dialog_manufacturer_warning_generic);
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.iqoo.secure",
         * "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
         * R.string.dialog_manufacturer_warning_generic);
         */

        /*
         * Dewav
         * - https://stackoverflow.com/a/49110392/5815054
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.dewav.dwappmanager",
         * "com.dewav.dwappmanager.memory.SmartClearupWhiteList")),
         * R.string.dialog_manufacturer_warning_generic);
         */

        /*
         * Qmobile
         * - from comment in https://stackoverflow.com/a/49110392/5815054
         *
         * intentMap.put(new Intent().setComponent(new ComponentName("com.dewav.dwappmanager",
         * "com.dewav.dwappmanager.memory.SmartClearupWhiteList")),
         * R.string.dialog_manufacturer_warning_generic);
         */

        // Search for a match in the list of known energy settings pages
        for (Map.Entry<Intent, Integer> entry : intentMap.entrySet()) {
            if (context.getPackageManager().resolveActivity(entry.getKey(),
                    PackageManager.MATCH_DEFAULT_ONLY) != null) {
                return entry;
            }
        }

        return null;
    }

}
