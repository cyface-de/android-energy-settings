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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.afollestad.materialdialogs.MaterialDialog
import de.cyface.energy_settings.BackgroundProcessingRestrictionWarningDialog.Companion.create
import de.cyface.energy_settings.GnssDisabledWarningDialog.Companion.create

/**
 * Dialog to show a warning when the background processing is restricted by the energy settings.
 *
 * Two implementation are available:
 *
 * 1. As `DialogFragment`. Use the constructor and use the dialog. Calls [onCreateDialog] internally.
 * 2. As [MaterialDialog]. Use the static [create] method which returns the dialog.
 *
 * @author Armin Schnabel
 * @version 2.0.0
 * @since 1.0.0
 */
internal class BackgroundProcessingRestrictionWarningDialog : EnergySettingDialog() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle(titleRes).setMessage(messageRes)
    val context = context
    if (context != null) {
      builder.setPositiveButton(positiveButtonRes) { _, _ ->
        startActivity(intent(context))
      }
    }
    return builder.create()
  }

  companion object {
    /**
     * The resource pointing to the text used as title.
     */
    private val titleRes = R.string.dialog_background_processing_restriction_warning_title
    /**
     * The resource pointing to the text used as message.
     */
    private val messageRes = R.string.dialog_background_processing_restriction_warning
    /**
     * The resource pointing to the text used as positive button which opens the settings.
     */
    private val positiveButtonRes = R.string.dialog_button_open_settings

    /**
     * @param context Required to get the package name needed for this intent
     * @return The Android `Intent` to be started when the positive button was clicked (opens the location settings).
     */
    private fun intent(context: Context): Intent {
      val packageName = context.packageName
      val intent = Intent()
      intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
      intent.data = Uri.parse("package:$packageName")
      return intent
    }

    /**
     * Alternative, `FragmentManager`-less implementation.
     *
     * @param activity Required to show the dialog
     */
    fun create(activity: Activity): MaterialDialog {

      val dialog = MaterialDialog(activity, DIALOG_BEHAVIOUR)
      dialog.title(titleRes)
      dialog.message(messageRes)
      dialog.positiveButton(positiveButtonRes) {
        activity.startActivity(intent(activity.applicationContext))
      }
      return dialog
    }
  }
}
