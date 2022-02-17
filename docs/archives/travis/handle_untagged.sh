#!/usr/bin/env bash

set -ev

# If we don't have a tag, use the Maven project version to set/update a tag and push.
# Don't set TRAVIS_TAG so that this build will not be deployed.
# The new build that will be triggered by the push will be tagged and deployed as usual.
if [ -z "$TRAVIS_TAG" ] ; then
  VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
  git config --local user.name "Olivier"
  git config --local user.email "ogerardin@yahoo.com"
  git tag -f "$VERSION"
  git push --tags -f https://${GH_TOKEN}@github.com/ogerardin/xpman.git
fi

