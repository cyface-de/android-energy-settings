Cyface Android Energy Settings library
========================================

[<img src="https://github.com/cyface-de/android-energy-settings/workflows/Gradle%20Build/badge.svg">](https://github.com/cyface-de/android-energy-settings/actions)
[<img src="https://github.com/cyface-de/android-energy-settings/workflows/Gradle%20Publish/badge.svg">](https://github.com/cyface-de/android-energy-settings/actions)

This project contains the Cyface Android Energy Settings library which check and
handle settings required for background location tracking.

- [Integration Guide](#integration-guide)
- [API Usage Guide](#api-usage-guide)
- [Development Guide](#development-guide)
- [License](#license)


Integration Guide
---------------------

This library is published to the Github Package Registry.

To use it as a dependency you need to:

1. Make sure you are authenticated to the repository:

    * You need a Github account with read-access to this Github repository
    * Create a [personal access token on Github](https://github.com/settings/tokens) with "read:packages" permissions
    * Create or adjust a `local.properties` file in the project root containing:

    ```
    github.user=YOUR_USERNAME
    github.token=YOUR_ACCESS_TOKEN
    ```

    * Add the custom repository to your `build.gradle`:

    ```
    def properties = new Properties()
    properties.load(new FileInputStream("local.properties"))

    repositories {
        // Other maven repositories, e.g.:
        jcenter()
        google()
        // Repository for this library
        maven {
            url = uri("https://maven.pkg.github.com/cyface-de/android-energy-settings")
            credentials {
                username = properties.getProperty("github.user")
                password = properties.getProperty("github.token")
            }
        }
    }
    ```

2. Add this package as a Maven dependency to your app's `build.gradle`:

    ```
    dependencies {
        implementation "de.cyface:android-energy-settings:$energySettingsVersion"
    }
    ```

3. Set the `$energySettingsVersion` gradle variable to the [latest version](https://github.com/cyface-de/android-energy-settings/releases).


API Usage Guide
------------------

- [Check Energy Settings](#check-energy-settings)
	- [GNSS Enabled](#gnss-enabled)
	- [Energy Safer Active](#energy-safer-active)
	- [Restricted Background Processing Enabled](#restricted-background-processing-enabled)
	- [Problematic Manufacturer Identified](#problematic-manufacturer-identified)
- [Show Dialogs](#show-dialogs)
	- [GNSS Disabled Warning](#gnss-disabled-warning)
	- [Energy Safer Warning](#energy-safer-warning)
	- [Restricted Background Processing Warning](#restricted-background-processing-warning)
	- [Problematic Manufacturer Warning](#problematic-manufacturer-warning)
	- [No Guidance Needed](#no-guidance-needed)
- [Dismiss Dialogs](#dismiss-dialogs)


### Check Energy Settings

If you only want to ensure a specific setting is set, you can use the following APIs.
For details about the specific settings, see the [Show Dialogs](#show-dialogs) section.

#### GNSS Enabled

To check if a GNSS service is enabled (e.g. GPS) use:

```
if (!isGnssEnabled(context)) {
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

Two implementation are available:

1. `DialogFragment` implementation: Uses the Android `FragmentDialog` class.
2. `MaterialDialog` implementation: Alternative when no SupportFragmentManger is available
    (https://github.com/afollestad/material-dialogs)

#### GNSS Disabled Warning

If your tracking requires GNSS (like GPS) location service you can use this dialog to check whether it's enabled
and warn the user with a dialog to enable this.

The specific settings page can be opened via a *Settings* button at the end of the dialog.

```
showGnssWarningDialog(activity);
// or: Android FragmentDialog implementation
showGnssWarningDialog(context, fragment);
```

#### Energy Safer Warning

In energy safer mode the GPS location service is often disabled so your tracking does
not receive new updates while the display is off or while the app is in background.

This allows you to check whether the energy safer mode is active at this moment.
In this case a dialog is opened, informing the user to Stop the energy safer mode.

The specific settings page can be opened via a *Settings* button at the end of the dialog.

```
showEnergySaferWarningDialog(activity)
// or: Android FragmentDialog implementation
showEnergySaferWarningDialog(context, fragment)
```

#### Restricted Background Processing Warning

Newer Android Settings contain an option to disable background processing.

This allows you to check whether this option is enabled for your application.
In this case a dialog is opened, informing the user to disable this setting.

The specific settings page can be opened via a *Settings* button at the end of the dialog.

```
showRestrictedBackgroundProcessingWarningDialog(activity);
// or: Android FragmentDialog implementation
showRestrictedBackgroundProcessingWarningDialog(context, fragment);
```

#### Problematic Manufacturer Warning

Some manufacturers, e.g. Huawei, Xiaomi and Samsung, implement individual energy settings
which block your app from background processing or which disable the GPS location service.

This method checks whether such a manufacturer was identified.

It automatically searches for manufacturer specific setting pages on the phone.
If such a page is found the user is shown a specific dialog which explains how to adjust
those settings.

If the settings page is found automatically, it can be opened via a *Settings* button at the end of the dialog.
If not, the user is shown a generic dialog and a help button which generates an email template
for a feedback email which is addressed to the email address provided as parameter.

```
showProblematicManufacturerDialog(activity, true, "support@your-domain.com"))
// or: Android FragmentDialog implementation
showProblematicManufacturerDialog(context, fragment, true, "support@your-domain.com"))
```

#### No Guidance Needed

When your app contains a button to check for energy setting problems and you use
the return value of the [Show Dialogs](#show-dialogs) methods you may want to show
the user a dialog that no problems where identified.

This method does just this. It also shows a help button, which generates an email template,
for a feedback email which is addressed to the email address provided as parameter
so the user can report an identified problem which was not found automatically by this library.

```
showNoGuidanceNeededDialog(activity, "support@your-domain.com");
// or: Android FragmentDialog implementation
showNoGuidanceNeededDialog(fragment, "support@your-domain.com");
```

### Dismiss Dialogs

You may want to dismiss all dialogs created by this library when the app is paused, e.g. because the user may
be pausing the app to change the settings as explained in the warning.

This way you can use the [Show Dialogs](#show-dialogs) again in `onResume()`
and only show the dialogs again if the settings are not correct, when the app is opened again.

```
Override
protected void onPause() {
    // Only required when using the Android FragmentDialog implementations:
    TrackingSettings.dismissAllDialogs(fragmentManager);
    
    super.onPause();
}
```


Development Guide
--------------------

### Release a new version

This library is published to the Github Package Registry.

To publish a new version you need to:

1. Make sure you are authenticated to the repository:

    * You need a Github account with write-access to this Github repository
    * Create a [personal access token on Github](https://github.com/settings/tokens) with "write:packages" permissions
    * Create or adjust a `local.properties` file in the project root containing:

    ```
    github.user=YOUR_USERNAME
    github.token=YOUR_ACCESS_TOKEN
    ```

2. Publish a new version

    * Increment the `build.gradle`'s `ext.version`
    * Execute the publish command `./gradlew publish`


License
-------------------
Copyright 2019 Cyface GmbH

This file is part of the Cyface Energy Settings library for Android.

The Cyface Energy Settings library is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Cyface Energy Settings library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the Cyface Energy Settings library. If not, see <http://www.gnu.org/licenses/>.
