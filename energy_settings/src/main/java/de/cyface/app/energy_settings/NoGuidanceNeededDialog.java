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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

/**
 * Dialog to show a dialog when the user requests guidance from a phone on which there are no problems known.
 *
 * @author Armin Schnabel
 * @version 1.0.1
 * @since 1.0.0
 */
public class NoGuidanceNeededDialog extends EnergySettingDialog {

    /**
     * The e-mail address to which the feedback email should be addressed to in the generated template.
     */
    private final String recipientEmail;

    /**
     * @param recipientEmail The e-mail address to which the feedback email should be addressed to in the generated
     *            template.
     */
    NoGuidanceNeededDialog(@NonNull final String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_no_guidance_needed_title)
                .setMessage(R.string.dialog_no_guidance_needed);
        final Context context = getContext();
        if (context != null) {
            // show a feedback button
            builder.setPositiveButton(R.string.dialog_button_help, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final Intent emailIntent;
                    emailIntent = TrackingSettings.generateFeedbackEmailIntent(context,
                            getString(R.string.feedback_error_description), recipientEmail);
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback_choose_email_app)));
                }
            });
        }
        return builder.create();
    }

}
