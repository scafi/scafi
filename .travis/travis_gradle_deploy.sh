#!/bin/bash
set -e
./gradlew publish -PscalaVersions=$TRAVIS_SCALA_VERSION
