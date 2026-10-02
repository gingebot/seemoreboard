#!/usr/bin/env bash
set -e

echo "Updating packages..."
sudo apt update

echo "Installing Java..."
sudo apt install -y openjdk-17-jdk unzip wget

ANDROID_HOME="$HOME/android-sdk"
CMDLINE_TOOLS="$ANDROID_HOME/cmdline-tools/latest"

echo "Creating Android SDK directories..."
mkdir -p "$CMDLINE_TOOLS"

echo "Downloading Android command-line tools..."
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline.zip

echo "Extracting tools..."
unzip -q /tmp/cmdline.zip -d /tmp/cmdline
mv /tmp/cmdline/cmdline-tools/* "$CMDLINE_TOOLS"

echo "Setting environment variables..."
{
  echo "export ANDROID_HOME=$ANDROID_HOME"
  echo "export PATH=\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH"
} >> ~/.bashrc

source ~/.bashrc

echo "Installing SDK components..."
yes | sdkmanager \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0"

echo "Android SDK installation complete."
