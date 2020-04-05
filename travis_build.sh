#!/bin/bash
set -e
sbt ++$TRAVIS_SCALA_VERSION -v test unidoc
sbt ++$TRAVIS_SCALA_VERSION -v 'project core' assembly