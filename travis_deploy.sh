#!/bin/bash
set -e
#sbt ++$TRAVIS_SCALA_VERSION releaseEarly
# https://github.com/jvican/sbt-release-early/issues/34 https://github.com/jvican/sbt-release-early/issues/32
sbt +releaseEarly sonatypeBundleRelease