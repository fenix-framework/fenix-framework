#!/usr/bin/env bash

SEMVER_REGEX="^[vV]?((0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(\-(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)(\.(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*))*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?)$"
if [[ "$1" =~ $SEMVER_REGEX ]]; then
  VERSION=${BASH_REMATCH[1]}
else
  echo -e "version $1 does not match the semver scheme '(v)X.Y.Z(-PRERELEASE)(+BUILD)'"
  exit 1
fi
mvn --batch-mode -Dresume=false -DpushChanges=true -DscmCommentPrefix="[fenixedu-releaser]" -DtagNameFormat=v$VERSION -DreleaseVersion=$VERSION -DdevelopmentVersion=DEV-SNAPSHOT org.apache.maven.plugins:maven-release-plugin:2.5.2:prepare -Darguments="-Dmaven.javadoc.skip=true"
