#!/bin/bash
set -e
sbt ++$TRAVIS_SCALA_VERSION test
sbt ++$TRAVIS_SCALA_VERSION unidoc
sbt ++$TRAVIS_SCALA_VERSION 'project core' assembly