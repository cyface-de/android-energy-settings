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
import android.content.Intent
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import de.cyface.energy_settings.NoGuidanceNeededDialog.Companion.create

/**
 * Dialog to show a dialog when the user requests guidance from a phone on which there are no problems known.
 *
 * Two implementation are available:
 *
 * 1. As `DialogFragment`. Use the constructor and use the dialog. Calls [onCreateDialog] internally.
 * 2. As [MaterialDialog]. Use the static [create] method which returns the dialog.
 *
 * @author Armin Schnabel
 * @version 2.0.0
 * @since 1.0.0
 *
 * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
 * template.
 */
internal class NoGuidanceNeededDialog(private val recipientEmail: String) : EnergySettingDialog() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle(titleRes).setMessage(messageRes)
    val context = context
    if (context != null) {
      builder.setPositiveButton(positiveButtonRes) { _, _ ->
        startActivity(Intent.createChooser(intent(context, recipientEmail), getString(chooseEmailAppRes)))
      }
    }
    return builder.create()
  }

  companion object {
    /**
     * The resource pointing to the text used as title.
     */
    private val titleRes = R.string.dialog_no_guidance_needed_title
    /**
     * The resource pointing to the text used as message.
     */
    private val messageRes = R.string.dialog_no_guidance_needed
    /**
     * The resource pointing to the text used as positive button which opens a feedback email template.
     */
    private val positiveButtonRes = R.string.dialog_button_help

    /**
     * Alternative, `FragmentManager`-less implementation.
     *
     * @param activity Required to show the dialog
     */
    fun create(activity: Activity, recipientEmail: String): MaterialDialog {

      val dialog = MaterialDialog(activity, DIALOG_BEHAVIOUR)
      dialog.title(titleRes)
      dialog.message(messageRes)
      dialog.positiveButton(positiveButtonRes) {
        activity.startActivity(Intent.createChooser(intent(activity.applicationContext, recipientEmail), activity.getString(chooseEmailAppRes)))
      }
      return dialog
    }
  }
}
