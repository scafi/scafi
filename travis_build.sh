#!/bin/bash
set -e
#sbt ++$TRAVIS_SCALA_VERSION -v test unidoc
#sbt ++$TRAVIS_SCALA_VERSION -v 'project core' assembly
sbt +test
if [ "$TRAVIS_SCALA_VERSION" == "$DOC_SCALA_VERSION" ]; then
  sbt ++$TRAVIS_SCALA_VERSION unidoc
fi
sbt 'project core' +assembly