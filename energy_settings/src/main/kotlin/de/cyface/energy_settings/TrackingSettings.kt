/*
 * Copyright 2019-2023 Cyface GmbH
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

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import de.cyface.energy_settings.TrackingSettings.initialize
import de.cyface.energy_settings.settings.EnergySettings
import de.cyface.utils.Validate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Holds the API for this energy setting library.
 *
 * Offers checks and dialogs for energy settings required for background tracking.
 *
 * Attention: You need to call [initialize] before you use this object, e.g. in Activity.onCreate.
 *
 * @author Armin Schnabel
 * @version 2.1.0
 * @since 1.0.0
 */
object TrackingSettings {

    /**
     * Custom settings used by this library.
     */
    private lateinit var settings: EnergySettings

    @JvmStatic
    fun initialize(context: Context) {
        settings = EnergySettings(context)
    }

    /**
     * Checks whether the energy safer mode is active *at this moment*.
     *
     * If this mode is active the GPS location service is disabled on most devices.
     *
     * API 28+
     * - due to Android documentation GPS is only disabled starting with API 28 when the display is off
     * - manufacturers can change this. On a Pixel 2 XL e.g. GPS is offline. On most other devices, too.
     * - we explicitly check if this is the case on the device and only return true if GPS gets disabled
     *
     * API < 28
     * - Some manufacturers implemented an own energy saving mode (e.g. on Honor 8, Android 7) which also kills GPS
     * when this mode is active and the display disabled. Thus, we don't also don't allow energy safer mode on lower
     * APIs.
     *
     * @param context The `Context` required to check the system settings
     * @return `True` if an energy safer mode is currently active which very likely disables the GPS service
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun isEnergySaferActive(context: Context): Boolean {

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isInPowerSavingMode = powerManager.isPowerSaveMode

        // On newer APIs we can check if the power safer mode actually kills location tracking in background
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            isInPowerSavingMode
        } else isInPowerSavingMode
                && powerManager.locationPowerSaveMode != PowerManager.LOCATION_MODE_FOREGROUND_ONLY
                && powerManager.locationPowerSaveMode != PowerManager.LOCATION_MODE_NO_CHANGE
    }

    /**
     * Checks whether background processing is restricted by the settings.
     *
     * If this mode is enabled, the background processing is paused when the app is in background or the display is off.
     *
     * This was tested on a Pixel 2 XL device. Here this setting is disabled by default.
     * On other manufacturers, e.g. on Xiaomi's MIUI this setting is enabled by default.
     *
     * @param context The `Context` required to check the system settings
     * @return `True` if the processing is restricted
     */
    @JvmStatic
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun isBackgroundProcessingRestricted(context: Context): Boolean {

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return (activityManager.isBackgroundRestricted)
    }

    /**
     * Checks whether a manufacturer was identified which implements manufacturer-specific energy settings known to
     * prevent background tracking when set up wrong or even with the default settings.
     *
     * @return `True` if such a manufacturer is identified
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection") // Used by implementing app
    val isProblematicManufacturer: Boolean
        get() {

            return when (Build.MANUFACTURER.lowercase(Locale.ROOT)) {
                Constants.MANUFACTURER_HUAWEI, Constants.MANUFACTURER_HONOR, Constants.MANUFACTURER_SAMSUNG,
                Constants.MANUFACTURER_XIAOMI, Constants.MANUFACTURER_SONY -> true
                /* Sony STAMINA
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
                else -> false
            }
        }

    /**
     * Checks whether GPS is enabled in the settings.
     *
     * @param context The `Context` required to check the system settings
     * @return `True` if GPS is enabled
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun isGnssEnabled(context: Context): Boolean {

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
     * Shows a [EnergySaferWarningDialog] when [.isEnergySaferActive] is true.
     *
     * Checks [.isEnergySaferActive] before showing the dialog.
     *
     * @param context The `Context` required to check the system settings
     * @param fragment The `Fragment` where the dialog should be shown
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    @Deprecated("Alternative implementation recommended as this one is currently not being tested.")
    fun showEnergySaferWarningDialog(context: Context, fragment: Fragment): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isEnergySaferActive(context)) {
            val fragmentManager = fragment.fragmentManager
            Validate.notNull(fragmentManager)
            val dialog = EnergySaferWarningDialog()
            dialog.setTargetFragment(fragment, Constants.DIALOG_ENERGY_SAFER_WARNING_CODE)
            dialog.show(fragmentManager!!, "ENERGY_SAFER_WARNING_DIALOG")
            return true
        }
        return false
    }

    /**
     * Shows a [EnergySaferWarningDialog] when [.isEnergySaferActive] is true.
     *
     * Checks [.isEnergySaferActive] before showing the dialog.
     *
     * @param activity Required to show the dialog
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun showEnergySaferWarningDialog(activity: Activity?): Boolean {

        if (activity == null || activity.isFinishing) {
            Log.w(Constants.TAG, "showEnergySaferWarningDialog: aborted, activity is null")
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isEnergySaferActive(activity.applicationContext)) {
            EnergySaferWarningDialog.create(activity).show()
            return true
        }
        return false
    }

    /**
     * Shows a [BackgroundProcessingRestrictionWarningDialog] when
     * [.isBackgroundProcessingRestricted] is true.
     *
     * Checks [.isBackgroundProcessingRestricted] before showing the dialog.
     *
     * @param context The `Context` required to check the system settings
     * @param fragment The `Fragment` where the dialog should be shown
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    @Deprecated("Alternative implementation recommended as this one is currently not being tested.")
    fun showRestrictedBackgroundProcessingWarningDialog(
        context: Context,
        fragment: Fragment
    ): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isBackgroundProcessingRestricted(
                context
            )
        ) {
            val fragmentManager = fragment.fragmentManager
            Validate.notNull(fragmentManager)
            val dialog = BackgroundProcessingRestrictionWarningDialog()
            dialog.setTargetFragment(fragment, Constants.DIALOG_BACKGROUND_RESTRICTION_WARNING_CODE)
            dialog.show(fragmentManager!!, "BACKGROUND_RESTRICTION_WARNING_DIALOG")
            return true
        }
        return false
    }

    /**
     * Shows a [BackgroundProcessingRestrictionWarningDialog] when
     * [.isBackgroundProcessingRestricted] is true.
     *
     * Checks [.isBackgroundProcessingRestricted] before showing the dialog.
     *
     * @param activity Required to show the dialog
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun showRestrictedBackgroundProcessingWarningDialog(activity: Activity?): Boolean {

        if (activity == null || activity.isFinishing) {
            Log.w(
                Constants.TAG,
                "showRestrictedBackgroundProcessingWarningDialog: aborted, activity is null"
            )
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isBackgroundProcessingRestricted(
                activity.applicationContext
            )
        ) {
            BackgroundProcessingRestrictionWarningDialog.create(activity).show()
            return true
        }
        return false
    }

    /**
     * Shows a [ProblematicManufacturerWarningDialog] if [.isProblematicManufacturer] is true.
     *
     * This also checks if the user prefers not to see the dialog if he did not request guidance by himself.
     *
     * @param context The `Context` required to check the system settings
     * @param fragment The `Fragment` where the dialog should be shown
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     * template.
     * @param force `True` if the dialog should be shown no matter of the preferences state
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    @Deprecated("Alternative implementation recommended as this one is currently not being tested.")
    fun showProblematicManufacturerDialog(
        context: Context,
        fragment: Fragment,
        force: Boolean,
        recipientEmail: String
    ): Boolean {
        // TODO: consider async-preloading data at least, see:
        // https://developer.android.com/topic/libraries/architecture/datastore#synchronous
        val warningShown = runBlocking {
            settings.manufacturerWarningShownFlow.first()
        }
        if (isProblematicManufacturer && (force || !warningShown)) {
            val fragmentManager = fragment.fragmentManager
            Validate.notNull(fragmentManager)
            val dialog = ProblematicManufacturerWarningDialog(
                recipientEmail,
                fragment.lifecycleScope,
                settings
            )
            dialog.setTargetFragment(
                fragment,
                Constants.DIALOG_PROBLEMATIC_MANUFACTURER_WARNING_CODE
            )
            dialog.show(fragmentManager!!, "PROBLEMATIC_MANUFACTURER_WARNING_DIALOG")
            return true
        }
        return false
    }

    /**
     * Shows a [ProblematicManufacturerWarningDialog] if [.isProblematicManufacturer] is true.
     *
     * This also checks if the user prefers not to see the dialog if he did not request guidance by himself.
     *
     * @param activity Required to show the dialog
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     * template.
     * @param force `True` if the dialog should be shown no matter of the preferences state
     * @param scope The scope to execute async code in.
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun showProblematicManufacturerDialog(
        activity: Activity?,
        force: Boolean,
        recipientEmail: String,
        scope: LifecycleCoroutineScope
    ): Boolean {

        if (activity == null || activity.isFinishing) {
            Log.w(Constants.TAG, "showProblematicManufacturerDialog: aborted, activity is null")
            return false
        }

        val warningShown = runBlocking {
            settings.manufacturerWarningShownFlow.first()
        }

        if (isProblematicManufacturer && (force || !warningShown)) {
            ProblematicManufacturerWarningDialog.create(activity, recipientEmail, settings, scope)
                .show()
            return true
        }
        return false
    }

    /**
     * Shows a [GnssDisabledWarningDialog] when [.isGpsEnabled] is true.
     *
     * @param context The `Context` required to check the system settings
     * @param fragment The `Fragment` where the dialog should be shown
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    @Deprecated("Alternative implementation recommended as this one is currently not being tested.")
    fun showGnssWarningDialog(context: Context, fragment: Fragment): Boolean {

        if (isGnssEnabled(context)) {
            return false
        }

        val fragmentManager = fragment.fragmentManager
        Validate.notNull(fragmentManager)
        val dialog = GnssDisabledWarningDialog()
        dialog.setTargetFragment(fragment, Constants.DIALOG_GPS_DISABLED_WARNING_CODE)
        dialog.show(fragmentManager!!, "GPS_DISABLED_WARNING_DIALOG")
        return true
    }

    /**
     * Shows a [GnssDisabledWarningDialog] when [.isGpsEnabled] is true.
     *
     * @param activity Required to show the dialog
     * @return `True` if the dialog is shown
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun showGnssWarningDialog(activity: Activity?): Boolean {

        if (activity == null || activity.isFinishing) {
            Log.w(Constants.TAG, "showGnssWarningDialog: aborted, activity is null")
            return false
        }
        val context = activity.applicationContext
        if (isGnssEnabled(context)) {
            return false
        }

        GnssDisabledWarningDialog.create(activity).show()
        return true
    }

    /**
     * Shows a [NoGuidanceNeededDialog].
     *
     * @param fragment The `Fragment` where the dialog should be shown
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     * template.
     */
    @JvmStatic
    @Deprecated("Alternative implementation recommended as this one is currently not being tested.")
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun showNoGuidanceNeededDialog(fragment: Fragment, recipientEmail: String) {

        val fragmentManager = fragment.fragmentManager
        Validate.notNull(fragmentManager)
        val dialog = NoGuidanceNeededDialog(recipientEmail)
        dialog.setTargetFragment(fragment, Constants.DIALOG_NO_GUIDANCE_NEEDED_DIALOG_CODE)
        dialog.show(fragmentManager!!, "NO_GUIDANCE_NEEDED_DIALOG")
    }

    /**
     * Shows a [NoGuidanceNeededDialog].
     *
     * @param activity Required to show the dialog
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     * template.
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun showNoGuidanceNeededDialog(activity: Activity?, recipientEmail: String) {

        if (activity == null || activity.isFinishing) {
            Log.w(Constants.TAG, "showNoGuidanceNeededDialog: aborted, activity is null")
            return
        }

        NoGuidanceNeededDialog.create(activity, recipientEmail).show()
    }

    /**
     * Generates an `Intent` which can be used to open an e-mail app where a new e-mail draft is opened
     * containing a template for a feedback email.
     *
     * @param context The `Context` required to get the app version for the template's subject field
     * @param extraText The title for the text area where the user can write his message. E.g. "Your message"
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     * template.
     * @return The intent
     */
    @JvmStatic
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun generateFeedbackEmailIntent(
        context: Context,
        extraText: String,
        recipientEmail: String
    ): Intent {

        val appVersion = getAppVersion(context)
        val appAndDeviceInfo = prepareAppAndDeviceInformation(context, appVersion)
        val mailSubject =
            (context.getString(R.string.app_name) + " " + context.getString(R.string.feedback_email_subject) + " (" + appVersion + "-"
                    + Build.VERSION.SDK_INT + ")")

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mailSubject)
        emailIntent.type = "plain/text"
        emailIntent.putExtra(Intent.EXTRA_TEXT, "$appAndDeviceInfo\n---\n$extraText:\n\n\n")
        return emailIntent
    }

    /**
     * Load the app version as string.
     *
     * @param context The `Context` required to get the app version
     * @return The app version as string
     */
    private fun getAppVersion(context: Context): String {

        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(Constants.TAG, "App version could not be identified: $e")
            "N/A"
        }
    }

    /**
     * This function loads information about the app and device.
     *
     * @param context The `Context` required to get the text template
     * @param appVersion The app version as string
     * @return Device and app information
     */
    private fun prepareAppAndDeviceInformation(context: Context, appVersion: String): String {

        // Replace app version, commit id and device info dynamically
        return (context.getString(R.string.feedback_version_text) + ": " + appVersion + "\n"
                + context.getString(R.string.feedback_device_text) + ": " + Build.MANUFACTURER + ", "
                + Build.MODEL + " (" + Build.DEVICE + ")\n" + context.getString(R.string.feedback_android_text) + ": "
                + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")")
    }

    /**
     * Dismisses all [EnergySettingDialog]s.
     *
     * You can use this in your `Activity#onPause()` method to hide all dialogs from this library
     * as the user might have changed his settings while the app is paused so you should recheck the settings
     * in `Activity#onResume()` and only show the dialogs required then.
     *
     * @param fragmentManager The `FragmentManager` required to search for open dialogs.
     */
    @JvmStatic
    @Deprecated("Only works with deprecated DialogFragment implementations. Should not be needed for MaterialDialogs.")
    @Suppress("MemberVisibilityCanBePrivate") // Used by implementing app
    fun dismissAllDialogs(fragmentManager: FragmentManager) {

        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is EnergySettingDialog) {
                fragment.dismissAllowingStateLoss()
            }
            val childFragmentManager = fragment.childFragmentManager
            @Suppress("DEPRECATION") // Ok as this is called inside the method marked as deprecated
            dismissAllDialogs(childFragmentManager)
        }
    }
}
