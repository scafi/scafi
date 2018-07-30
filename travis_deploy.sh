#!/bin/bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
  sbt ++$TRAVIS_SCALA_VERSION releaseEarly;
fi
