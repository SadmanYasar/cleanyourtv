#!/usr/bin/env bash
# Quick build and reinstall script for the Apple TV-style launcher
set -euo pipefail

cd "$(dirname "$0")"
export TV_HOST="${TV_HOST:-127.0.0.1}"
export TV_PORT="${TV_PORT:-15555}"
export JAVA_HOME=/usr/local/opt/openjdk@17
export ANDROID_HOME=/usr/local/share/android-commandlinetools

cd launcher
./build-and-install.sh
