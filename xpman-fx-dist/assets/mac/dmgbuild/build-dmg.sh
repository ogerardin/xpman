#!/usr/bin/env bash
# Build the X-Plane Manager DMG with dmgbuild.
# Replaces jpackage's DMG installer; reproduces its customization pixel-perfect.
# Sole producer + strict mode: runs in the `package` phase (see pom.xml mac
# profile) and must succeed for the build to pass.
set -euo pipefail

APP_NAME="${1:?usage: build-dmg.sh APP_NAME VERSION}"
VERSION="${2:?usage: build-dmg.sh APP_NAME VERSION}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="${SCRIPT_DIR}/../../../target/.dmgbuild-venv"

# Locate a Python >= 3.10 (dmgbuild requirement). Homebrew Pythons are
# externally managed (PEP 668), so we install into a venv, not --user.
PYTHON=""
for cand in python3.14 python3.13 python3.12 python3.11 python3.10; do
    if command -v "$cand" >/dev/null 2>&1 && "$cand" -c 'import sys; sys.exit(0 if sys.version_info >= (3, 10) else 1)' 2>/dev/null; then
        PYTHON="$cand"
        break
    fi
done
if [ -z "$PYTHON" ]; then
    for cand in /usr/local/bin/python3.14 /usr/local/bin/python3.13 /usr/local/bin/python3.12; do
        if [ -x "$cand" ] && "$cand" -c 'import sys; sys.exit(0 if sys.version_info >= (3, 10) else 1)' 2>/dev/null; then
            PYTHON="$cand"
            break
        fi
    done
fi
if [ -z "$PYTHON" ]; then
    echo "error: no Python >= 3.10 found (dmgbuild requires it)" >&2
    exit 1
fi

if [ ! -x "${VENV_DIR}/bin/dmgbuild" ]; then
    "$PYTHON" -m venv "${VENV_DIR}"
    "${VENV_DIR}/bin/pip" install --quiet dmgbuild
fi

cd "${SCRIPT_DIR}/../../../target"

if [ ! -d "${APP_NAME}.app" ]; then
    echo "error: ${APP_NAME}.app not found in target/ (the jpackage app-image install must run first)" >&2
    exit 1
fi

# Remove any stale output so a failure is never mistaken for a fresh build
OUTPUT_DMG="${APP_NAME}-${VERSION}.dmg"
rm -f "${OUTPUT_DMG}"

"${VENV_DIR}/bin/dmgbuild" \
    -s "${SCRIPT_DIR}/settings.py" \
    -D APP_NAME="${APP_NAME}" \
    -D SCRIPT_DIR="${SCRIPT_DIR}" \
    "${APP_NAME}" \
    "${OUTPUT_DMG}"
