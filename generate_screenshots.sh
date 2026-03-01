#!/bin/bash

# Longboi Launcher Screenshot Generator Script
# This script runs an instrumented test on the device/emulator to capture screenshots
# of different app parts and then pulls them to your local machine.

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}🚀 Starting Longboi Launcher Screenshot Generation...${NC}"

# Check if adb is available
if ! command -v adb &> /dev/null
then
    echo -e "${RED}❌ Error: adb is not installed or not in PATH.${NC}"
    exit 1
fi

# Check if a device is connected
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep "device" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ Error: No device or emulator connected via adb.${NC}"
    exit 1
fi

echo -e "${BLUE}🧹 Cleaning device state...${NC}"
adb uninstall com.longboilauncher.app &> /dev/null
adb uninstall com.longboilauncher.app.test &> /dev/null

echo -e "${BLUE}📦 Building app and test APK...${NC}"
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed.${NC}"
    exit 1
fi

echo -e "${BLUE}📲 Installing APKs...${NC}"
adb install app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

echo -e "${BLUE}🧪 Running the ScreenshotGeneratorTest...${NC}"
# Use the test runner specified in app/build.gradle
adb shell am instrument -w -e class com.longboilauncher.app.ScreenshotGeneratorTest com.longboilauncher.app.test/com.longboilauncher.app.HiltTestRunner

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Test execution failed.${NC}"
    exit 1
fi

echo -e "${BLUE}📂 Pulling screenshots from internal storage...${NC}"
mkdir -p screenshots
# List files and pull each one since run-as doesn't support recursive pull well
FILES=$(adb shell "run-as com.longboilauncher.app ls /data/data/com.longboilauncher.app/files/screenshots" | tr -d '\r')
for FILE in $FILES; do
    if [[ $FILE == *.png ]]; then
        echo "Pulling $FILE..."
        adb shell "run-as com.longboilauncher.app cat /data/data/com.longboilauncher.app/files/screenshots/$FILE" > "screenshots/$FILE"
    fi
done

if [ "$(ls -A screenshots)" ]; then
    echo -e "${GREEN}✅ Screenshots successfully saved to the 'screenshots' directory!${NC}"
    ls -1 screenshots
else
    echo -e "${RED}❌ Failed to pull screenshots or no screenshots found.${NC}"
fi

echo -e "${BLUE}🧹 Cleaning up device directory...${NC}"
adb shell "run-as com.longboilauncher.app rm -rf /data/data/com.longboilauncher.app/files/screenshots"

echo -e "${GREEN}✨ Done!${NC}"
