#!/usr/bin/env bash
set +e
./gradlew :app:connectedDebugAndroidTest -PkeyboardTestAbi=x86_64
keyboard_test_status=$?
adb logcat -d > keyboard-logcat.txt
adb exec-out screencap -p > keyboard-screen.png
mkdir -p keyboard-aosp
cp -r app/build/outputs/androidTest-results keyboard-aosp/
cp -r app/build/reports/androidTests keyboard-aosp/
if [ "$keyboard_test_status" -ne 0 ]; then exit "$keyboard_test_status"; fi

# Repeat with the user's keyboard; this changes only the disposable emulator.
curl -fL --retry 3 https://github.com/futo-org/android-keyboard/releases/download/0.1.30/keyboard-0.1.30.apk -o /tmp/futo-keyboard.apk || exit 1
adb install -r /tmp/futo-keyboard.apk || exit 1
futo_ime=$(adb shell ime list -a -s | tr -d '\r' | awk '/org.futo/ { print; exit }')
if [ -z "$futo_ime" ]; then adb shell ime list -a -s; echo 'FUTO input method not found'; exit 1; fi
adb shell ime enable "$futo_ime" || exit 1
adb shell ime set "$futo_ime" || exit 1
adb logcat -c
./gradlew :app:connectedDebugAndroidTest -PkeyboardTestAbi=x86_64 -Pandroid.testInstrumentationRunnerArguments.keyboard=futo
keyboard_test_status=$?
adb logcat -d > keyboard-futo-logcat.txt
adb exec-out screencap -p > keyboard-futo-screen.png
exit "$keyboard_test_status"
