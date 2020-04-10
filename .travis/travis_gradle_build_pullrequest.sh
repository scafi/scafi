#!/bin/bash
set -e
./gradlew check -PscalaVersions=$TRAVIS_SCALA_VERSION
