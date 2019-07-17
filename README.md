Cyface Android Energy Settings library
========================================

This project contains the Cyface Android Energy Settings library which check and handle settings required for background location tracking.

- [How to integrate the library](#how-to-integrate-the-library)
- [License](#license)


How to integrate the library
-----------------------------

- [Check Energy Settings](#check-energy-settings)
	- [GPS Enabled](#gps-enabled)
	- [Energy Safer Active](#energy-safer-active)
	- [Restricted Background Processing Enabled](#restricted-background-processing-enabled)
	- [Problematic Manufacturer Identified](#problematic-manufacturer-identified)
- [Show Dialogs](#show-dialogs)
	- [GPS Disabled Warning](#gps-disabled-warning)
	- [Energy Safer Warning](#energy-safer-warning)
	- [Restricted Background Processing Warning](#restricted-background-processing-warning)
	- [Problematic Manufacturer Warning](#problematic-manufacturer-warning)
	- [No Guidance Needed](#no-guidance-needed)
- [Dismiss Dialogs](#dismiss-dialogs)


### Check Energy Settings

If you only want to ensure a specific setting is set, you can use the following APIs.
For details about the specific settings, see the [Show Dialogs](#show-dialogs) section.

#### GPS Enabled 

```
if (!isGpsEnabled(context)) {
    // Your logic
}
```

#### Energy Safer Active

```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isEnergySaferActive(context))
    // Your logic
} 
```

#### Restricted Background Processing Enabled

```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isBackgroundProcessingRestricted(context)) {
    // Your logic
}
```

#### Problematic Manufacturer Identified

```
if (isProblematicManufacturer()) {
    // Your logic
}
```

### Show Dialogs

The show dialog methods below automatically check the specific energy setting if possible
and only show the dialog if the setting is not correct. You can check whether the dialog
was shown from the returned boolean value.

#### GPS Disabled Warning

If your tracking required GPS location service you can use this dialog to check whether it's enabled
and warn the user with a dialog that we needs to enable this.

The specific settings page can be opened via a *Settings* button at the end of the dialog. 

```
showGpsWarningDialog(context, fragment);
```

#### Energy Safer Warning

In energy safer mode the GPS location service is often disabled so your tracking does
not receive new updated while the display is off or while the app is in background.

This allows you to check whether the energy safer mode is active at this moment.
In this case a dialog is opened, informing the user that he needs to Stop the energy safer mode.

The specific settings page can be opened via a *Settings* button at the end of the dialog.

```
showEnergySaferWarningDialog(context, fragment)
```

#### Restricted Background Processing Warning

Newer Android Settings contain an option to disable background processing.

This allows you to check whether the this option is enabled for your application.
In this case a dialog is opened, informing the user that he needs to disable this setting.

The specific settings page can be opened via a *Settings* button at the end of the dialog.

```
showRestrictedBackgroundProcessingWarningDialog(context, fragment);
```

#### Problematic Manufacturer Warning

Some manufacturers, e.g. Huawei, Xiaomi and Samsung, implement own energy settings
which block your app from processing in background or which disable GPS location service.

This method checks whether such a manufacturer was identified.

It automatically searches for manufacturer specific setting pages on the phone.
If such a page is found the user is shown a specific dialog which explains how to adjust
those settings.

If the settings page is found automatically, it can be opened via a *Settings* button at the end of the dialog.
If not, the user is shown a generic dialog and a help button which generates an email template
for a feedback email which is addressed to the email address provided as parameter.

```
showProblematicManufacturerDialog(context, fragment, true, "support@your-domain.com"))
```

#### No Guidance Needed

When your app contains a button to check for energy setting problems and you use
the return value of the [Show Dialogs](#show-dialogs) methods you may want to show
the user a dialog that no problems where identified.

This method does just this. It also a help button which generates an email template
for a feedback email which is addressed to the email address provided as parameter
so the user can report when he identified a problem which was not found automatically by this library.

```
showNoGuidanceNeededDialog(fragment, "support@your-domain.com");
```

### Dismiss Dialogs

You may want to dismiss all dialogs when the app is paused, e.g. because the user may
be pausing the app to change the settings as explained in the warning.

This way you can use the [Show Dialogs](#show-dialogs) again in `onResume()`
and only show the dialogs again if the settings are not correctly when the app is opened again.

```
Override
protected void onPause() {
    dismissAllDialogs(fragmentManager);
    super.onPause();
}

private void dismissAllDialogs(@NonNull final FragmentManager fragmentManager) {
    final List<Fragment> fragments = fragmentManager.getFragments();
    for (final Fragment fragment : fragments) {
        if (fragment instanceof DialogFragment) {
            final DialogFragment dialogFragment = (DialogFragment)fragment;
            dialogFragment.dismissAllowingStateLoss();

            final FragmentManager childFragmentManager = fragment.getChildFragmentManager();
            dismissAllDialogs(childFragmentManager);
        }
    }
}
```


License
-------------------
Copyright 2019 Cyface GmbH

This file is part of the Cyface library for Android.

The Cyface library for Android is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Cyface library for Android is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the Cyface library for Android. If not, see <http://www.gnu.org/licenses/>.
