#!/usr/bin/env bash
set -e

function build_framework() {
  xcrun -sdk iphoneos clang -o "$1"/"$1" "$1"/"$1".m -shared -lobjc -m32 -arch armv7 -miphoneos-version-min=7.1 -framework "$2" && \
  mv "$1"/"$1" "$1"/"$1"32 && \
  xcrun -sdk iphoneos clang -o "$1"/"$1" "$1"/"$1".m -shared -lobjc -m64 -arch arm64 -miphoneos-version-min=7.1 -framework "$2" && \
  mv "$1"/"$1" "$1"/"$1"64 && \
  lipo -create "$1"/"$1"32 "$1"/"$1"64 -output "$1"/"$1" && \
  rm "$1"/"$1"32 "$1"/"$1"64 && \
  mkdir -p ../../../resources/ios/7.1/System/Library/Frameworks/"$1".framework && \
  mv "$1"/"$1" ../../../resources/ios/7.1/System/Library/Frameworks/"$1".framework/
}

build_framework UIKit "Foundation"
build_framework JavaScriptCore "Foundation"
build_framework MultipeerConnectivity "Foundation"
build_framework PushKit "Foundation"
build_framework WebKit "Foundation"
build_framework AdSupport "Foundation"
build_framework Photos "Foundation"
build_framework ExternalAccessory "Foundation"
build_framework AddressBookUI "Foundation"
build_framework AddressBook "Foundation"
build_framework CoreLocation "Foundation"
build_framework ImageIO "Foundation"
build_framework AVFoundation "Foundation"
build_framework AudioToolbox "Foundation"
build_framework CoreMotion "Foundation"
build_framework GLKit "Foundation"
build_framework MediaPlayer "Foundation"
build_framework SceneKit "Foundation"
build_framework Metal "Foundation"
build_framework SystemConfiguration "CoreFoundation"
build_framework CoreTelephony "Foundation"
build_framework PassKit "Foundation"
build_framework Contacts "Foundation"
build_framework AssetsLibrary "Foundation"
build_framework GSS "Foundation"
