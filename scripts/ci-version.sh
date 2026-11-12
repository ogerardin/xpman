#!/bin/bash

# Computes the project version for CI builds (tag-based versioning).
#
# Usage (both forms take an optional REF argument — a tag or branch name):
#   source scripts/ci-version.sh [REF]   # sets REVISION and IS_SNAPSHOT in the caller
#   scripts/ci-version.sh [REF]          # prints REVISION=... and IS_SNAPSHOT=...
#
# REF is typically $GITHUB_REF_NAME, $CIRCLE_TAG, or $APPVEYOR_REPO_TAG_NAME.
# It may be a tag, a branch name, or empty/unset for an untagged build.
#
# Resolution:
#   - tagged with a strict semantic version X.Y.Z  -> REVISION=X.Y.Z (release), IS_SNAPSHOT=0
#   - branch release/X.Y, latest X.Y.* tag is X.Y.Z -> REVISION=X.Y.(Z+1)-SNAPSHOT, IS_SNAPSHOT=1
#   - branch release/X.Y, no X.Y.* tag yet          -> REVISION=X.Y.0-SNAPSHOT, IS_SNAPSHOT=1
#   - untagged (main or other), latest semver tag X.Y.Z -> REVISION=X.Y.(Z+1)-SNAPSHOT, IS_SNAPSHOT=1
#   - untagged (main or other), no semver tag present    -> REVISION=1.0.1-SNAPSHOT (base 1.0.0), IS_SNAPSHOT=1
#
# The computed REVISION is passed to Maven as -Drevision=<value>; see the root pom.

set -euo pipefail

SEMVER_REGEX='^[0-9]+\.[0-9]+\.[0-9]+$'

# Parse "major.minor.incremental" into <prefix>_MAJOR/_MINOR/_INCREMENTAL variables.
# Uses eval for bash 3.2 compatibility (printf -v is bash 4+).
parse_version() {
    local version="$1" prefix="$2"
    local major minor incremental
    IFS='.' read -r major minor incremental <<< "$version"
    eval "${prefix}_MAJOR=\${major}"
    eval "${prefix}_MINOR=\${minor}"
    eval "${prefix}_INCREMENTAL=\${incremental}"
}

compute_version() {
    local tag="${1:-}"

    if [[ "$tag" =~ $SEMVER_REGEX ]]; then
        REVISION="$tag"
        IS_SNAPSHOT=0
        GH_VERSION="$tag"
        return
    fi

    # release/X.Y maintenance branch: snapshot toward the next X.Y.* patch release.
    if [[ "$tag" =~ ^release/([0-9]+)\.([0-9]+)$ ]]; then
        local major="${BASH_REMATCH[1]}" minor="${BASH_REMATCH[2]}"
        local latest_in_line=""
        latest_in_line="$(git tag --sort=version:refname 2>/dev/null \
            | grep -E "^${major}\.${minor}\.[0-9]+$" | tail -n 1 || true)"
        if [[ -n "$latest_in_line" ]]; then
            IFS='.' read -r _ _ z <<< "$latest_in_line"
            REVISION="${major}.${minor}.$((z + 1))-SNAPSHOT"
        else
            REVISION="${major}.${minor}.0-SNAPSHOT"
        fi
        IS_SNAPSHOT=1
        GH_VERSION="${REVISION}-sha.$(git rev-parse --short HEAD)"
        return
    fi

    local latest="" base="1.0.0"
    latest="$(git tag --sort=version:refname 2>/dev/null | grep -E "$SEMVER_REGEX" | tail -n 1 || true)"
    if [[ -n "$latest" ]]; then
        base="$latest"
    fi

    parse_version "$base" "BASE"
    REVISION="${BASE_MAJOR}.${BASE_MINOR}.$((BASE_INCREMENTAL + 1))-SNAPSHOT"
    GH_VERSION="${REVISION}-sha.$(git rev-parse --short HEAD)"
    IS_SNAPSHOT=1
}

main() {
    compute_version "${1:-}"
    echo "REVISION=${REVISION}"
    echo "GH_VERSION=${GH_VERSION}"
    echo "IS_SNAPSHOT=${IS_SNAPSHOT}"
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
    main "$@"
else
    compute_version "${1:-}"
fi
