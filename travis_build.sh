#!/bin/bash
set -e
sbt ++$TRAVIS_SCALA_VERSION test unidoc # publish

if ["$TRAVIS_SCALA_VERSION" == "$RELEASE_SCALA_VERSION"]
then
    sbt ++$RELEASE_SCALA_VERSION 'project core' assembly
fi