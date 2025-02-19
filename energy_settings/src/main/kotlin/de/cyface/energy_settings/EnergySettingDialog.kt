/*
 * Copyright 2019-2025 Cyface GmbH
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

import android.content.Context
import android.content.Intent
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.DialogBehavior
import com.afollestad.materialdialogs.MaterialDialog

/**
 * This class contains shared static methods of subclasses.
 *
 * It's also needed to only close such dialogs in [TrackingSettings.dismissAllDialogs] and to not interfere
 * with other dialogs. This is only used in the Fragment based Dialog implementations, not the MaterialDialog based.
 *
 * @author Armin Schnabel
 * @version 2.0.1
 * @since 1.1.0
 */
internal abstract class EnergySettingDialog : DialogFragment() {
  companion object {
    /**
     * For all instances of [MaterialDialog].
     */
    internal var DIALOG_BEHAVIOUR: DialogBehavior = MaterialDialog.DEFAULT_BEHAVIOR

    /**
     * The resource pointing to the text used as template for the feedback email in [intent].
     */
    private val feedbackErrorDescriptionRes = R.string.feedback_error_description
    /**
     * The resource pointing to the text used to ask the use to choose an email app which the [intent] should open.
     */
    internal val chooseEmailAppRes = R.string.feedback_choose_email_app

    /**
     * The Android `Intent` to be started when the positive button was clicked (opens feedback email).
     */
    fun intent(context: Context, recipientEmail: String): Intent {
      return TrackingSettings.generateFeedbackEmailIntent(
        context,
        context.getString(feedbackErrorDescriptionRes),
        recipientEmail
      )
    }
  }
}
