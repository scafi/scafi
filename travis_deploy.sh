#!/bin/bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
  openssl aes-256-cbc -K $encrypted_e508d9beb0ba_key -iv $encrypted_e508d9beb0ba_iv -in .travis/secrets.tar.enc -out .travis/local.secrets.tar -d;
  tar xv -C .travis -f .travis/secrets.tar;
  sbt ++$TRAVIS_SCALA_VERSION releaseEarly;
fi
