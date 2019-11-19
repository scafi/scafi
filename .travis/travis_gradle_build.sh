#!/bin/bash
set -e
./gradlew check -PscalaVersions=$TRAVIS_SCALA_VERSION
./gradlew fatJar -PscalaVersions=$TRAVIS_SCALA_VERSION
