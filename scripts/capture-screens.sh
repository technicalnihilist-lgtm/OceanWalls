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

SIZE=$(adb shell wm size | tr -d '\r' | awk '{print $3}')
W="${SIZE%x*}"; H="${SIZE#*x}"
echo "screen: ${W}x${H}"

shot() { sleep 2; adb exec-out screencap -p > "$OUT/$1.png"; echo "captured $1"; }
tap()  { adb shell input tap "$1" "$2"; sleep 1; }

adb shell am force-stop "$PKG" || true
adb shell am start -W -n "$ACT" || exit 1
echo "waiting for live feeds to load..."
sleep 22
shot 01-home

# scroll the home feed
adb shell input swipe $((W/2)) $((H*78/100)) $((W/2)) $((H*22/100)) 350
sleep 4
shot 02-home-collections

# back to top, open credits from the info button
adb shell input swipe $((W/2)) $((H*25/100)) $((W/2)) $((H*85/100)) 300
sleep 2
tap $((W*93/100)) $((H*6/100))
sleep 3
shot 03-credits
adb shell input keyevent KEYCODE_BACK
sleep 2

# search tab (second of four bottom-bar items)
tap $((W*37/100)) $((H*96/100))
sleep 3
shot 04-search
tap $((W*45/100)) $((H*13/100))
sleep 1
adb shell input text "jellyfish"
sleep 9
shot 05-search-results

# open the first result
tap $((W*25/100)) $((H*38/100))
sleep 10
shot 06-wallpaper-detail
adb shell input swipe $((W/2)) $((H*80/100)) $((W/2)) $((H*45/100)) 350
sleep 3
shot 07-wallpaper-credits

# category screen: home -> trending animal pill
adb shell input keyevent KEYCODE_BACK
sleep 2
tap $((W*12/100)) $((H*96/100))
sleep 3
shot 08-home-top
echo "done"
ls -l "$OUT"
