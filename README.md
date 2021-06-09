 ![icon](assets/logo.png)

## User guide

Look into the wiki for GIFs-based tutorials: [LINK](https://github.com/federicoiosue/Omni-Notes/wiki)

** This version is extended with the sync feature, beside the official documentation, please read the section below **

## Connect Google Identity for sync features

First, find the SHA-1 hash of the signing certificate by running the project gradle "android -> signingReport" script.
After acquiring it, follow the instructions present in the official [google identity docs](https://developers.google.com/identity/sign-in/android/start-integrating)
to connect your application with a google cloud project. Finally, as the application uses google drive permission, attach DRIVE_FILE scope to the cloud project.
While creating the oauth identifier, remember that this branch application package is it.feio.android.omninotes.alpha.

## Enabling sync inside application
Go to **Settings** -> Enable sync and log in through your google account.

## Build

Watch the following terminal session recording on how to compile distributable files
[![asciicast](https://asciinema.org/a/102898.png)](https://asciinema.org/a/102898)

To be sure that build environment is fully compliant with the project the following command creates a container with all the needed tools to compile the code:

```
cd {project-folder}; rm local.properties; docker rm android-omninotes; docker run -v $PWD:/workspace --name android-omninotes tabrindle/min-alpine-android-sdk:latest bash -c "yes | sdkmanager --update && yes | sdkmanager --licenses && cd /workspace && ./gradlew build --stacktrace -Dorg.gradle.daemon=true -Pandroid.useDeprecatedNdk=true"

```

## Test

To execute all tests included into the project connect a device or emulator, then run the following command:

```shell
./gradlew testAll
```

### Testing Pyramid

To speedup the development more levels of testing are available following the [testing pyramid approach](https://martinfowler.com/articles/practical-test-pyramid.html), each type test requires more time than the previous one.

### Unit Tests
```shell
./gradlew --stacktrace test
```

### Integration Tests
```shell
./gradlew --stacktrace -Pandroid.testInstrumentationRunnerArguments.notAnnotation=androidx.test.filters.LargeTest connectedAndroidTest
```

### UI Tests
```shell
./gradlew --stacktrace -Pandroid.testInstrumentationRunnerArguments.annotation=androidx.test.filters.LargeTest connectedPlayDebugAndroidTest
```
Notice that in this case I specified a single flavor to run tests on. This could be a useful and faster approach when you're testing specific flavor features.  

## Contributing

Due to the fact that I'm using [gitflow](https://github.com/nvie/gitflow) as code versioning methodology, you as developer should **always** start working on [develop branch](https://github.com/federicoiosue/Omni-Notes/tree/develop) that contains the most recent changes.

There are many features/improvements that are not on **my** roadmap but someone else could decide to work on them anyway: hunt for issues tagged as [Help Wanted](https://github.com/federicoiosue/Omni-Notes/issues?utf8=✓&q=label%3A"Help+wanted") to find them!

Feel free to add yourself to [contributors.md](https://github.com/federicoiosue/Omni-Notes/blob/develop/contributors.md) file.

### New feature or improvements contributions

This kind of contributions **must** have screenshots or screencast as demonstration of the new additions.

### Code style

If you plan to manipulate the code then you'll have to do it by following a [specific code style](https://gist.github.com/federicoiosue/dee53e882b3c70d544f8608769eb02fc).
Also pay attention if you're using any plugin that automatically formats/cleans/rearrange your code and set it to only change that code that you touched and not the whole files.

### Test your code contributions!

All code changes and additions **must** be tested.
See the [related section](#test) for more informations or this two pull requests comments: [one](https://github.com/federicoiosue/Omni-Notes/pull/646#pullrequestreview-187973443) and [two](https://github.com/federicoiosue/Omni-Notes/pull/683#issuecomment-506206689)

### Forking project

When forking the project you'll have to modify some files that are strictly dependent from my own development / build / third-party-services environment. Files that need some attention are the following:

  - *gradle.properties*: this is overridden by another file with the same name inside the *omniNotes* module. You can do the same or leave as it is, any missing property will let the app gracefully fallback on a default behavior.

## Code quality

A public instance of SonarQube is available both to encourage other developers to improve their code contributions (and existing code obviously) and to move the project even further into transparency and openness.

Checkout for it [here](https://sonarcloud.io/dashboard?id=omni-notes)

Pull requests will be automatically analyzed and rejected if they'll rise the code technical debt.

## Dependencies

They're all listed into the [build.gradle](https://github.com/federicoiosue/Omni-Notes/blob/develop/omniNotes/build.gradle) file but due to the fact that many of the dependences have been customized by me I'd like to say thanks here to the original developers of these great libraries:

* https://github.com/RobotiumTech/robotium
* https://github.com/LarsWerkman/HoloColorPicker
* https://github.com/keyboardsurfer/Crouton
* https://github.com/romannurik/dashclock/wiki/API
* https://github.com/ACRA/acra
* https://github.com/Shusshu/Android-RecurrencePicker
* https://github.com/gabrielemariotti/changeloglib
* https://github.com/greenrobot/EventBus
* https://github.com/futuresimple/android-floating-action-button
* https://github.com/keyboardsurfer
* https://github.com/bumptech/glide
* https://github.com/neopixl/PixlUI
* https://github.com/afollestad/material-dialogs
* https://github.com/ical4j
* https://github.com/square/leakcanary
* https://github.com/pnikosis/materialish-progress
* https://github.com/apl-devs/AppIntro
* https://github.com/ReactiveX/RxAndroid
* https://github.com/artem-zinnatullin/RxJavaProGuardRules
* https://github.com/tbruyelle/RxPermissions
* https://github.com/ocpsoft/prettytime
* https://github.com/piwik/piwik-sdk-android
* https://github.com/mrmans0n/smart-location-lib


## Mentioned on

[XDA](https://www.xda-developers.com/omni-notes-the-open-source-note-app/)
[Android Authority](https://www.androidauthority.com/best-note-taking-apps-for-android-205356/)
[Droid Advisor](https://droidadvisor.com/omni-notes-note-taking-app/)
[Addictive Tips](https://www.addictivetips.com/android/note-taking-apps-for-android/)
[Techalook](https://techalook.com/apps/best-sticky-notes-android-iphone/)
[DZone](https://dzone.com/articles/amazing-open-source-android-apps-written-in-java)
[Slash Gear](https://www.slashgear.com/best-note-taking-apps-for-android-phones-and-tablets-04529297/)
[quaap.com](https://quaap.com/D/use-fdroid)

## Developed with love and passion by


* Federico Iosue - [Website](https://federico.iosue.it)
* [Other contributors](https://github.com/federicoiosue/Omni-Notes/blob/master/https://github.com/federicoiosue/Omni-Notes/blob/master/CONTRIBUTORS.md)



## License


    Copyright 2013-2020 Federico Iosue
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

