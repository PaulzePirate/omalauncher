#!/usr/bin/env bash
set +e
./gradlew :app:connectedDebugAndroidTest -PkeyboardTestAbi=x86_64
keyboard_test_status=$?
adb logcat -d > keyboard-logcat.txt
adb exec-out screencap -p > keyboard-screen.png
exit "$keyboard_test_status"
