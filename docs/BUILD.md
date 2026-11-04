# Building OceanWalls

## Requirements

| Tool | Version |
| --- | --- |
| JDK | 17 |
| Android SDK Platform | 34 (`platforms;android-34`) |
| Android Build Tools | 34.0.0 |
| Gradle | 8.7 (wrapper properties included) |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 2.0.21 (+ Compose compiler plugin, KSP 2.0.21-1.0.28) |

## Android Studio

`File > Open` the project folder, let Gradle sync, press **Run**. The debug variant installs as
`com.oceanwalls.app.debug` so it can sit beside a release build.

## Command line

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
echo "sdk.dir=$ANDROID_HOME" > local.properties

./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease   # unsigned release APK
```

`local.properties` is git-ignored on purpose - never commit an SDK path.

## First run checklist

1. Device or emulator on Android 7.0 (API 24) or newer, with internet.
2. Open the app: the home rails fill from the live Commons API within a second or two.
3. Pull down on Home - each refresh requests a deeper slice of the live result set, so new
   photographs appear instead of repeats.
4. Open any wallpaper: the credit block shows photographer, licence, licence URL, source page
   and the modification note.
5. "Home" / "Lock" / "Both" call `WallpaperManager`; "Gallery" writes into
   `Pictures/OceanWalls`; "Share" attaches the image plus the full credit text.

## Verifying the API layer without a device

```bash
python3 tools/verify_commons_api.py
```

This mirrors the app's queries and filters against the live Wikimedia Commons API and prints,
per category, how many licence-clean photographs were found (plus a JSON report).
