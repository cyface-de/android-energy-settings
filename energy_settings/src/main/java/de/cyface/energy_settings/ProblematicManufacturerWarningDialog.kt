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
package de.cyface.energy_settings

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import de.cyface.energy_settings.Constants.TAG
import de.cyface.energy_settings.GnssDisabledWarningDialog.Companion.create
import de.cyface.energy_settings.ProblematicManufacturerWarningDialog.Companion.create
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * Dialog to show a warning when a phone manufacturer is identified which allows to prevent background tracking
 * by manufacturer-specific settings.
 *
 * This dialog is customized by searching for a match in a known list of manufacturer-specific energy settings pages.
 * If a match is found the required steps to adjust the settings are shown and the setting page is accessible via a
 * dialog button. Else, a generic text is shown in the dialog to help the user finding the energy settings by itself.
 *
 * This dialog also contains a button which allows the user to disable the auto-popup of this dialog.
 * This preference is not respected when the user requests guidance explicitly.
 *
 * Two implementation are available:
 *
 * 1. As `DialogFragment`. Use the constructor and use the dialog. Calls [onCreateDialog] internally.
 * 2. As [MaterialDialog]. Use the static [create] method which returns the dialog.
 *
 * @author Armin Schnabel
 * @version 2.0.3
 * @since 1.0.0
 *
 * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated template.
 */
internal class ProblematicManufacturerWarningDialog(private val recipientEmail: String) : EnergySettingDialog() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

    // Generate dialog
    val builder = AlertDialog.Builder(activity)
    builder.setTitle(titleRes)

    // Allow the user to express its preference to disable auto-popup of this dialog
    builder.setNegativeButton(negativeButtonRes) { _, _ ->
      onNegativeButtonCall(context)
    }

    // Show Sony STAMINA specific dialog (no manufacturer specific intent name known yet)
    val messageRes = if (isSony()) sonyStaminaMessageRes else genericMessageRes

    // Context-less scenario
    val context = context
    if (context == null) {
      Log.w(TAG, "No ProblematicManufacturerWarningDialog shown, context is null.")
      builder.setMessage(messageRes)
      return builder.create()
    }

    // If a device specific settings page is found, add a button to open it directly
    val deviceSpecificIntent = getDeviceSpecificIntent(context)
    if (deviceSpecificIntent != null) {
      val settingsIntent = deviceSpecificIntent.key
      builder.setPositiveButton(openSettingsButtonRes) { _, _ -> startActivity(settingsIntent) }
      builder.setMessage(deviceSpecificIntent.value)
      return builder.create()
    }

    // Generate a feedback button
    builder.setPositiveButton(feedbackButtonRes) { _, _ ->
      startActivity(Intent.createChooser(intent(context, recipientEmail), getString(chooseEmailAppRes)))
    }

    builder.setMessage(messageRes)
    return builder.create()
  }

  companion object {
    /**
     * The resource pointing to the text used as title.
     */
    private val titleRes = R.string.dialog_problematic_manufacturer_warning_title
    /**
     * The resource pointing to the text of the generic dialog which is shown when no manufacturer-specific
     * energy setting page is found
     */
    private var genericMessageRes = R.string.dialog_manufacturer_warning_generic
    /**
     * The resource pointing to the text of the dialog which is shown when no manufacturer-specific
     * energy setting page is found but a sony device is detected
     */
    private val sonyStaminaMessageRes = R.string.dialog_manufacturer_warning_sony_stamina
    /**
     * The resource pointing to the text used as positive button which opens the settings.
     */
    private val openSettingsButtonRes = R.string.dialog_button_open_settings
    /**
     * The resource pointing to the text used as positive button which opens a feedback email template.
     */
    private val feedbackButtonRes = R.string.dialog_button_help
    /**
     * The resource pointing to the text used as negative button which stores the "don't show again" preference.
     */
    private val negativeButtonRes = R.string.dialog_button_do_not_show_again

    private fun isSony(): Boolean {
      return Build.MANUFACTURER.lowercase(Locale.ROOT) == Constants.MANUFACTURER_SONY
    }

    /**
     * Saves the user's preference to disable auto-popup of this dialog
     */
    private fun onNegativeButtonCall(context: Context?) {
      val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
      preferences.edit().putBoolean(Constants.PREFERENCES_MANUFACTURER_WARNING_SHOWN_KEY, true).apply()
    }

    /**
     * Alternative, `FragmentManager`-less implementation.
     *
     * @param activity Required to show the dialog
     */
    fun create(activity: Activity, recipientEmail: String): MaterialDialog {

      // Generate dialog
      val dialog = MaterialDialog(activity, DIALOG_BEHAVIOUR)
      dialog.title(titleRes, null)

      // Allow the user to express its preference to disable auto-popup of this dialog
      dialog.negativeButton(negativeButtonRes) {
        onNegativeButtonCall(activity.applicationContext)
      }

      // Show Sony STAMINA specific dialog (no manufacturer specific intent name known yet)
      val messageRes = if (isSony()) sonyStaminaMessageRes else genericMessageRes

      // Context-less scenario
      val context = activity.applicationContext
      if (context == null) {
        Log.w(TAG, "No ProblematicManufacturerWarningDialog shown, context is null.")
        dialog.message(messageRes)
        return dialog
      }

      // If a device specific settings page is found, add a button to open it directly
      val deviceSpecificIntent = getDeviceSpecificIntent(context)
      if (deviceSpecificIntent != null) {
        val settingsIntent = deviceSpecificIntent.key
        dialog.positiveButton(openSettingsButtonRes) { activity.startActivity(settingsIntent) }
        dialog.message(deviceSpecificIntent.value)
        return dialog
      }

      // Generate a feedback button
      dialog.positiveButton(feedbackButtonRes) {
        activity.startActivity(Intent.createChooser(intent(context, recipientEmail), context.getString(chooseEmailAppRes)))
      }

      dialog.message(messageRes)
      return dialog
    }

    /**
     * Searches for a match of a list of known device-specific energy setting pages.
     *
     * @param context The [Context] to check if the intent is resolvable on this device
     * @return The intent to open the settings page and the resource id of the message string which describes what to do
     * on the settings page. Returns `Null` if no intent was resolved. Always returns the first match,
     * which means the intent which was added in a later Android version if there are multiple matches.
     */
    private fun getDeviceSpecificIntent(context: Context): Map.Entry<Intent, Int>? {
      // (!) This needs to be an ordered map or else StartupAppControlActivity might be returned in favor
      // of StartupNormalAppListActivity if both exist which leads to an error [MOV-989]
      val intentMap: MutableMap<Intent, Int> = LinkedHashMap()

      /*
       * Huawei/Honor
       * - EMUI OS (https://de.wikipedia.org/wiki/EMUI#Versionen)
       *******************************************************************************************/

      // "App-Start", e.g. EMUI 9, 10

      // [MOV-989] On EMUI 10.0.0 (e.g. our P smart 2019) it finds both:
      // 1. StartupNormalAppListActivity (which works on new EMUI versions)
      // 2. StartupAppControlActivity (which crashes on new EMUI even with USE_COMPONENT permission)
      // This is why we need to keep the intents here in an ordered map with (1) earlier in the map
      // This way always (1) is returned before (2)

      // P20, EMUI 9, Android 9, 2019 - comment in https://stackoverflow.com/a/48641229/5815054
      // when adding the permissions above does not work with the intent above
      intentMap[Intent().setComponent(ComponentName("com.huawei.systemmanager",
        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"))] = R.string.dialog_manufacturer_warning_huawei_app_launch

      // P20, EMUI 9, Android 9, 2018 - comment in https://stackoverflow.com/a/35220476/5815054
      // [STAD-280] This check should not be necessary as `StartupNormalAppListActivity` should
      // always be prioritised by the current code if both are found. However, on some Android 10
      // devices it seems like this activity is still called. Thus, we completely disable it on Q+.
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        intentMap[Intent().setComponent(ComponentName("com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"))] = R.string.dialog_manufacturer_warning_huawei_app_launch
      }

      // "Protected Apps", EMUI <5, Android <7
      intentMap[Intent().setComponent(ComponentName("com.huawei.systemmanager",
        "com.huawei.systemmanager.optimize.process.ProtectActivity"))] = R.string.dialog_manufacturer_warning_huawei_protected_app

      /*
       * Samsung
       * - One UI (https://de.wikipedia.org/wiki/One_UI)
       * - TouchWiz (https://en.wikipedia.org/wiki/TouchWiz)
       *******************************************************************************************/

      // Android 7+, "Unmonitored Apps"
      // - http://sleep.urbandroid.org/documentation/faq/alarms-sleep-tracking-dont-work/#sleeping-apps
      // - Unable to test if this is necessary as we don't have a Samsung Android 7 device
      intentMap[Intent().setComponent(ComponentName("com.samsung.android.lool",
        "com.samsung.android.sm.ui.battery.BatteryActivity"))] = R.string.dialog_manufacturer_warning_samsung_device_care

      // Android 5-6 - https://code.briarproject.org/briar/briar/issues/1100
      // - tested: settings found and opened on Android 6.0.1 (office phone: S5 neo)
      // - by default (our phone) this was off but people might have enabled both general and app-specific option
      intentMap[Intent().setComponent(ComponentName("com.samsung.android.sm",
        "com.samsung.android.sm.ui.battery.BatteryActivity"))] = R.string.dialog_manufacturer_warning_samsung_smart_manager

      /*
       * Xiaomi
       * - MIUI (https://en.wikipedia.org/wiki/MIUI#Version_history)
       *******************************************************************************************/

      // Tested on Redmi Note 5 (Android 8.1, MIUI 10)
      intentMap[Intent().setComponent(ComponentName("com.miui.securitycenter",
        "com.miui.powercenter.PowerSettings"))] = R.string.dialog_manufacturer_warning_xiaomi_power_settings
      intentMap[Intent().setComponent(ComponentName("com.miui.securitycenter",
        "com.miui.permcenter.autostart.AutoStartManagementActivity"))] = R.string.dialog_manufacturer_warning_xiaomi_auto_start

      // from https://github.com/dirkam/backgroundable-android
      // Unsure if this is the correct dialog
      intentMap[Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT)] = R.string.dialog_manufacturer_warning_xiaomi_power_settings

      // Unsure if this is the correct dialog
      intentMap[Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT)] = R.string.dialog_manufacturer_warning_xiaomi_auto_start

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
       * / * VIOLATION WARNING: ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS * /
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
      // Return the first match. If not this leads to [MOV-989], see note above.
      for(entry in intentMap.entries) {
        if (context.packageManager.resolveActivity(entry.key,
            PackageManager.MATCH_DEFAULT_ONLY) != null) {
          return entry
        }
      }

      return null
    }
  }
}
