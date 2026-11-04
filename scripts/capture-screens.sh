#!/usr/bin/env bash
# Drives the installed OceanWalls debug build on an emulator and captures screenshots.
# Usage: bash scripts/capture-screens.sh [output-dir] [apk-path]
set -uo pipefail

OUT="${1:-screenshots}"
APK="${2:-app/build/outputs/apk/debug/app-debug.apk}"
PKG="com.oceanwalls.app.debug"
ACT="$PKG/com.oceanwalls.app.MainActivity"

mkdir -p "$OUT"
adb wait-for-device
adb install -r "$APK" || exit 1

# Keep the system out of the way: no ANR/crash dialogs, no animations.
adb shell settings put global hide_error_dialogs 1
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell settings put global heads_up_notifications_enabled 0

SIZE=$(adb shell wm size | tr -d '\r' | awk '{print $3}')
W="${SIZE%x*}"; H="${SIZE#*x}"
echo "screen: ${W}x${H}"

shot() { sleep 2; adb exec-out screencap -p > "$OUT/$1.png"; echo "captured $1"; }
tap()  { adb shell input tap "$1" "$2"; sleep 1; }

adb shell am force-stop "$PKG" || true
adb shell am start -W -n "$ACT" || exit 1
echo "waiting for live feeds to load..."
sleep 26
shot 01-home

# scroll the home feed
adb shell input swipe $((W/2)) $((H*78/100)) $((W/2)) $((H*22/100)) 350
sleep 5
shot 02-home-collections
adb shell input swipe $((W/2)) $((H*25/100)) $((W/2)) $((H*85/100)) 300
sleep 3

# search tab (second of four bottom-bar items)
tap $((W*37/100)) $((H*91/100))
sleep 3
shot 03-search
tap $((W*45/100)) $((H*145/1000))
sleep 1
adb shell input text "jellyfish"
sleep 12
adb shell input keyevent KEYCODE_BACK   # close the keyboard
sleep 2
shot 04-search-results

# open the first result
tap $((W*25/100)) $((H*42/100))
sleep 12
shot 05-wallpaper-detail
adb shell input swipe $((W/2)) $((H*82/100)) $((W/2)) $((H*42/100)) 400
sleep 3
shot 06-wallpaper-credits
adb shell input keyevent KEYCODE_BACK
sleep 3

# credits screen from the home header
tap $((W*12/100)) $((H*91/100))
sleep 4
tap $((W*92/100)) $((H*10/100))
sleep 4
shot 07-credits
adb shell input keyevent KEYCODE_BACK
sleep 3

# a category screen: trending marine animals pill
tap $((W*22/100)) $((H*62/100))
sleep 10
shot 08-category
echo "done"
ls -l "$OUT"
