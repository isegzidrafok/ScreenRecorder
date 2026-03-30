#!/usr/bin/env sh
##############################################################################
# Gradle start up script for UN*X
##############################################################################
set -e
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

JAVA_HOME_CMD=""
if [ -n "$JAVA_HOME" ] ; then
    JAVA_HOME_CMD="$JAVA_HOME/bin/java"
fi
JAVA_CMD="${JAVA_HOME_CMD:-java}"

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
