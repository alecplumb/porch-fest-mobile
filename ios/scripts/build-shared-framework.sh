#!/bin/sh
# Xcode build phase: builds the shared Kotlin framework for the current configuration,
# SDK and architecture into shared/build/xcode-frameworks/, where the app target links it.
set -e

# Xcode runs scripts without your shell profile, so JAVA_HOME is usually unset here, and the
# system default JDK may be newer than Gradle supports. Prefer Android Studio's bundled JDK,
# then an installed JDK 17.
if [ -z "$JAVA_HOME" ]; then
  STUDIO_JDK="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  if [ -x "$STUDIO_JDK/bin/java" ]; then
    export JAVA_HOME="$STUDIO_JDK"
  elif JDK17="$(/usr/libexec/java_home -v 17 2>/dev/null)" && [ -n "$JDK17" ]; then
    export JAVA_HOME="$JDK17"
  fi
fi

cd "$SRCROOT/../shared"
./gradlew embedAndSignAppleFrameworkForXcode
