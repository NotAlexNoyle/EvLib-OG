#!/usr/bin/bash

# Fetch all submodule content.
git submodule update --force --recursive --init --remote

# Pre-build Utilities-OG.
cd libs/Utilities-OG

./gradlew clean build

cd ../..
