/*
 * Copyright 2019 Cyface GmbH
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
package de.cyface.energy_settings;

import static de.cyface.energy_settings.Constants.DIALOG_BACKGROUND_RESTRICTION_WARNING_CODE;
import static de.cyface.energy_settings.Constants.DIALOG_ENERGY_SAFER_WARNING_CODE;
import static de.cyface.energy_settings.Constants.DIALOG_GPS_DISABLED_WARNING_CODE;
import static de.cyface.energy_settings.Constants.DIALOG_NO_GUIDANCE_NEEDED_DIALOG_CODE;
import static de.cyface.energy_settings.Constants.DIALOG_PROBLEMATIC_MANUFACTURER_WARNING_CODE;
import static de.cyface.energy_settings.Constants.MANUFACTURER_HONOR;
import static de.cyface.energy_settings.Constants.MANUFACTURER_HUAWEI;
import static de.cyface.energy_settings.Constants.MANUFACTURER_SAMSUNG;
import static de.cyface.energy_settings.Constants.MANUFACTURER_SONY;
import static de.cyface.energy_settings.Constants.MANUFACTURER_XIAOMI;
import static de.cyface.energy_settings.Constants.PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY;
import static de.cyface.energy_settings.Constants.TAG;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.DialogBehavior;
import com.afollestad.materialdialogs.MaterialDialog;

import de.cyface.utils.Validate;

/**
 * Holds the API for this energy setting library.
 * <p>
 * Offers checks and dialogs for energy settings required for background tracking.
 *
 * @author Armin Schnabel
 * @version 1.1.1
 * @since 1.0.0
 */
public class TrackingSettings {

    /**
     * Checks whether the energy safer mode is active *at this moment*.
     * <p>
     * If this mode is active the GPS location service is disabled on most devices.
     * <p>
     * API 28+
     * - due to Android documentation GPS is only disabled starting with API 28 when the display is off
     * - manufacturers can change this. On a Pixel 2 XL e.g. GPS is offline. On most other devices, too.
     * - we explicitly check if this is the case on the device and only return true if GPS gets disabled
     * <p>
     * API < 28
     * - Some manufacturers implemented an own energy saving mode (e.g. on Honor 8, Android 7) which also kills GPS
     * when this mode is active and the display disabled. Thus, we don't also don't allow energy safer mode on lower
     * APIs.
     *
     * @param context The {@code Context} required to check the system settings
     * @return {@code True} if an energy safer mode is currently active which very likely disables the GPS service
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean isEnergySaferActive(@NonNull final Context context) {

        final PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        final boolean isInPowerSavingMode = powerManager != null && powerManager.isPowerSaveMode();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return isInPowerSavingMode;
        }
        // On newer APIs we can check if the power safer mode actually kills location tracking in background
        return isInPowerSavingMode
                && powerManager.getLocationPowerSaveMode() != PowerManager.LOCATION_MODE_FOREGROUND_ONLY
                && powerManager.getLocationPowerSaveMode() != PowerManager.LOCATION_MODE_NO_CHANGE;
    }

    /**
     * Checks whether background processing is restricted by the settings.
     * <p>
     * If this mode is enabled, the background processing is paused when the app is in background or the display is off.
     * <p>
     * This was tested on a Pixel 2 XL device. Here this setting is disabled by default.
     * <p>
     * On other manufacturers, e.g. on Xiaomi's MIUI this setting is enabled by default.
     *
     * @param context The {@code Context} required to check the system settings
     * @return {@code True} if the processing is restricted
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean isBackgroundProcessingRestricted(@NonNull final Context context) {
        final ActivityManager activityManager = (ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager != null
                && activityManager.isBackgroundRestricted();
    }

    /**
     * Checks whether a manufacturer was identified which implements manufacturer-specific energy settings known to
     * prevent background tracking when set up wrong or even with the default settings.
     *
     * @return {@code True} if such a manufacturer is identified
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean isProblematicManufacturer() {

        final String manufacturer = Build.MANUFACTURER;
        switch (manufacturer.toLowerCase()) {
            case MANUFACTURER_HUAWEI:
            case MANUFACTURER_HONOR:
            case MANUFACTURER_SAMSUNG:
            case MANUFACTURER_XIAOMI:
            case MANUFACTURER_SONY:
                /*
                 * Sony STAMINA
                 * - On Android 8 we may be able to check if STAMINA is disabled completely according to:
                 * https://stackoverflow.com/a/50740898/5815054/, https://dontkillmyapp.com/sony
                 * Settings.Secure.getInt(context.getContentResolver(), "somc.stamina_mode", 0) > 0;
                 * - However, as this does not work on Android 6 we just show the warning to all SONY phones
                 */

                /*
                 * Other manufacturers
                 * - The following manufacturers seem to be as restrictive as Huawei etc. (see
                 * https://dontkillmyapp.com)
                 * - but we have no negative reports yet from such devices, nor can we test them.
                 * - For those reasons this part is uncommented but we keep this info here as it's some work to collect
                 * this data.
                 *
                 * case MANUFACTURER_HTC:
                 * case MANUFACTURER_OPPO:
                 * case MANUFACTURER_ASUS:
                 * case MANUFACTURER_LETV:
                 * case MANUFACTURER_VIVO:
                 * case MANUFACTURER_MEIZU:
                 * case MANUFACTURER_DEWAV:
                 * case MANUFACTURER_QMOBILE:
                 */
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks whether GPS is enabled in the settings.
     *
     * @param context The {@code Context} required to check the system settings
     * @return {@code True} if GPS is enabled
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean isGpsEnabled(@NonNull final Context context) {

        final LocationManager manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return manager == null || manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /*
     * Battery Optimization setting
     *
     * - Tests showed that this option should not be needed. (It also may sound scary to the user.)
     * - Android settings explain that this setting is only needed if the was not Doze-optimized.
     * - Our app should be Doze-optimized - which our tests indicated, too.
     *
     * else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
     * && powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
     *
     * There are two native dialogs which can be popped up:
     *
     * 1) Request direct white-listing
     * - this is not allowed in all cases and can end up in a denied play store release
     * - this would required the following permissions:
     * <uses-permission
     * android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
     *
     * 2) Open the settings page where the user hat to white-list the app himself
     * final Intent intent = new Intent();
     * intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
     * intent.setData(Uri.parse("package:" + packageName));
     * fragmentRoot.getContext().startActivity(intent);
     * }
     */

    /**
     * Shows a {@link EnergySaferWarningDialog} when {@link #isEnergySaferActive(Context)} is true.
     * <p>
     * Checks {@link #isEnergySaferActive(Context)} before showing the dialog.
     *
     * @return {@code True} if the dialog is shown
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean showEnergySaferWarningDialog(@NonNull final Context context,
            @NonNull final Fragment fragment) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isEnergySaferActive(context)) {
            final FragmentManager fragmentManager = fragment.getFragmentManager();
            Validate.notNull(fragmentManager);
            final EnergySaferWarningDialog dialog = new EnergySaferWarningDialog();
            dialog.setTargetFragment(fragment, DIALOG_ENERGY_SAFER_WARNING_CODE);
            dialog.show(fragmentManager, "ENERGY_SAFER_WARNING_DIALOG");
            return true;
        }
        return false;
    }

    /**
     * Shows a {@link BackgroundProcessingRestrictionWarningDialog} when
     * {@link #isBackgroundProcessingRestricted(Context)} is true.
     * <p>
     * Checks {@link #isBackgroundProcessingRestricted(Context)} before showing the dialog.
     *
     * @param context The {@code Context} required to check the system settings
     * @param fragment The {@code Fragment} where the dialog should be shown
     * @return {@code True} if the dialog is shown
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean showRestrictedBackgroundProcessingWarningDialog(@NonNull final Context context,
            @NonNull final Fragment fragment) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isBackgroundProcessingRestricted(context)) {
            final FragmentManager fragmentManager = fragment.getFragmentManager();
            Validate.notNull(fragmentManager);
            final BackgroundProcessingRestrictionWarningDialog dialog = new BackgroundProcessingRestrictionWarningDialog();
            dialog.setTargetFragment(fragment, DIALOG_BACKGROUND_RESTRICTION_WARNING_CODE);
            dialog.show(fragmentManager, "BACKGROUND_RESTRICTION_WARNING_DIALOG");
            return true;
        }
        return false;
    }

    /**
     * Shows a {@link ProblematicManufacturerWarningDialog} if {@link #isProblematicManufacturer()} is true.
     * <p>
     * This also checks if the user prefers not to see the dialog if he did not request guidance by himself.
     *
     * @param context The {@code Context} required to check the system settings
     * @param fragment The {@code Fragment} where the dialog should be shown
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     *            template.
     * @param force {@code True} if the dialog should be shown no matter of the preferences state
     * @return {@code True} if the dialog is shown
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean showProblematicManufacturerDialog(@NonNull final Context context,
            @NonNull final Fragment fragment,
            final boolean force, @NonNull final String recipientEmail) {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (isProblematicManufacturer()
                && (force || !preferences.getBoolean(PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY, false))) {

            final FragmentManager fragmentManager = fragment.getFragmentManager();
            Validate.notNull(fragmentManager);
            final ProblematicManufacturerWarningDialog dialog = new ProblematicManufacturerWarningDialog(
                    recipientEmail);
            dialog.setTargetFragment(fragment, DIALOG_PROBLEMATIC_MANUFACTURER_WARNING_CODE);
            dialog.show(fragmentManager, "PROBLEMATIC_MANUFACTURER_WARNING_DIALOG");
            return true;
        }
        return false;
    }

    /**
     * Shows a {@link GpsDisabledWarningDialog} when {@link #isGpsEnabled(Context)} is true.
     *
     * @param context The {@code Context} required to check the system settings
     * @param fragment The {@code Fragment} where the dialog should be shown
     * @return {@code True} if the dialog is shown
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static boolean showGpsWarningDialog(@NonNull final Context context, @NonNull final Fragment fragment) {

        if (isGpsEnabled(context)) {
            return false;
        }

        /*final FragmentManager fragmentManager = fragment.getFragmentManager();
        Validate.notNull(fragmentManager);
        final GpsDisabledWarningDialog dialog = new GpsDisabledWarningDialog();
        dialog.setTargetFragment(fragment, DIALOG_GPS_DISABLED_WARNING_CODE);
        dialog.show(fragmentManager, "GPS_DISABLED_WARNING_DIALOG");*/

        DialogBehavior behavior = MaterialDialog.getDEFAULT_BEHAVIOR();
        MaterialDialog dialog = new MaterialDialog(context, behavior);
        dialog.title(R.string.dialog_gps_disabled_warning_title, null);
        dialog.message(R.string.dialog_gps_disabled_warning, null, null);
        //dialog.message(R.string.dialog_gps_disabled_warning, null, null);
        //builder.setPositiveButton(R.string.dialog_button_open_settings, new DialogInterface.OnClickListener() {
        //    public void onClick(DialogInterface dialog, int which) {
        //        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //        startActivity(intent);
        //    }
        //});
        dialog.show();

        return true;
    }

    /**
     * Shows a {@link NoGuidanceNeededDialog}.
     *
     * @param fragment The {@code Fragment} where the dialog should be shown
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     *            template.
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static void showNoGuidanceNeededDialog(@NonNull final Fragment fragment,
            @NonNull final String recipientEmail) {

        final FragmentManager fragmentManager = fragment.getFragmentManager();
        Validate.notNull(fragmentManager);
        final NoGuidanceNeededDialog dialog = new NoGuidanceNeededDialog(recipientEmail);
        dialog.setTargetFragment(fragment, DIALOG_NO_GUIDANCE_NEEDED_DIALOG_CODE);
        dialog.show(fragmentManager, "NO_GUIDANCE_NEEDED_DIALOG");
    }

    /**
     * Generates an {@code Intent} which can be used to open an e-mail app where a new e-mail draft is opened
     * containing a template for a feedback email.
     *
     * @param context The {@code Context} required to get the app version for the template's subject field
     * @param extraText The title for the text area where the user can write his message. E.g. "Your message"
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     *            template.
     * @return The intent
     */
    @SuppressWarnings("unused") // Used by implementing app
    public static Intent generateFeedbackEmailIntent(@NonNull final Context context, @NonNull final String extraText,
            @NonNull final String recipientEmail) {

        final String appVersion = getAppVersion(context);
        final String appAndDeviceInfo = prepareAppAndDeviceInformation(context, appVersion);

        String mailSubject = context.getString(R.string.app_name) + " "+context.getString(R.string.feedback_email_subject) + " (" + appVersion + "-"
                + Build.VERSION.SDK_INT + ")";
        final Intent emailIntent;
        emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, appAndDeviceInfo + "\n---\n" + extraText + ":\n\n\n");
        return emailIntent;
    }

    /**
     * Load the app version as string.
     *
     * @param context The {@code Context} required to get the app version
     * @return The app version as string
     */
    private static String getAppVersion(final Context context) {

        final PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e(TAG, "App version could not be identified: " + e);
            return "N/A";
        }
    }

    /**
     * This function loads information about the app and device.
     *
     * @param context The {@code Context} required to get the text template
     * @param appVersion The app version as string
     * @return Device and app information
     */
    private static String prepareAppAndDeviceInformation(final Context context, final String appVersion) {

        // Replace app version, commit id and device info dynamically
        return context.getString(R.string.feedback_version_text) + ": " + appVersion + "\n"
                + context.getString(R.string.feedback_device_text) + ": " + Build.MANUFACTURER + ", "
                + Build.MODEL + " (" + Build.DEVICE + ")\n" + context.getString(R.string.feedback_android_text) + ": "
                + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }

    /**
     * Dismisses all {@link EnergySettingDialog}s.
     * <p>
     * You can use this in your {@code Activity#onPause()} method to hide all dialogs from this library
     * as the user might have changed his settings while the app is paused so you should recheck the settings
     * in {@code Activity#onResume()} and only show the dialogs required then.
     *
     * @param fragmentManager The {@code FragmentManager} required to search for open dialogs.
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression"}) // Used by implementing apps
    public static void dismissAllDialogs(@NonNull final FragmentManager fragmentManager) {
        final List<Fragment> fragments = fragmentManager.getFragments();

        for (final Fragment fragment : fragments) {
            if (fragment instanceof EnergySettingDialog) {
                final EnergySettingDialog dialogFragment = (EnergySettingDialog)fragment;
                dialogFragment.dismissAllowingStateLoss();
            }

            final FragmentManager childFragmentManager = fragment.getChildFragmentManager();
            dismissAllDialogs(childFragmentManager);
        }
    }
}
