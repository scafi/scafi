#!/bin/bash
set -e
./gradlew check -PscalaVersions=$TRAVIS_SCALA_VERSION
./gradlew :scafi-core:fatJar -PscalaVersions=$TRAVIS_SCALA_VERSION
