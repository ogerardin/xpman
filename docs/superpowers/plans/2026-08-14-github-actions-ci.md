# Plan: GitHub Actions CI (parallel to CircleCI + AppVeyor)

Date: 2026-08-14

## Goal

Add **GitHub Actions** as a third CI pipeline for XPman, running **alongside** the existing CircleCI and AppVeyor pipelines (none is replaced). It builds the same native installers on the same 3 OSes:

- Linux x64 → `.deb`, `.rpm`, repackaged jar
- Windows x64 → `.exe`, `.msi`, repackaged jar
- macOS ARM64 → `.pkg`, `.dmg`, repackaged jar

Artifacts only — **no release publishing** on GHA (CircleCI keeps its `publish-github-release` job).

## Decisions (confirmed with user)

1. GHA is an **additional** CI, not a replacement — CircleCI and AppVeyor stay.
2. **No release publishing** from GHA — default read-only token is enough; no release job.
3. Triggers: `push` to `main` + `pull_request`. During testing only, `feat/github-actions` is temporarily added to `push.branches` as a test lane.
4. CircleCI is restricted to **`main` only** so feature branches never trigger it (removes the need for `[skip ci]`-style markers). This is a permanent behavior change:
   - CircleCI no longer builds feature branches or PRs — GHA becomes the pre-merge CI.
   - `publish-github-release` now fires only from `main` (incidentally fixes releases being overwritten from feature branches via `ghr -delete`).
5. AppVeyor is already `main`-only — unchanged.

## Changes

### 1. `.circleci/config.yml`

Add `filters: branches: only: main` to all 4 jobs of the `default` workflow (`build-linux`, `build-mac`, `build-windows`, `publish-github-release`). Identical filters across `requires` satisfy CircleCI's requires+filters consistency rule.

### 2. `.github/workflows/build.yml` (new)

```yaml
on:
  push:
    branches: [main, feat/github-actions]   # feat/github-actions is TEMPORARY — removed before merge
  pull_request:
```

Jobs: `linux` (ubuntu-24.04), `windows` (windows-2025), `mac` (macos-26, arm64). Each job:

- `actions/checkout@v4`
- Configure JDK 25 via runner env vars written to `GITHUB_ENV`/`GITHUB_PATH` (do **not** use `${{ env.JAVA_HOME_25_X64 }}` — the `env` context excludes runner machine vars). `actions/setup-java` is the fallback if this proves brittle.
- `actions/cache@v4` on `~/.m2` (key `maven-${{ hashFiles('**/pom.xml') }}`, restore-keys `maven-`)
- Build: `mvn -v` then `mvn -B -DskipTests clean package`
- Prepare artifacts (sed rename `-fx-dist`→`` and `repackaged`→`<platform>`, identical to CircleCI renames; Windows uses `shell: bash` — git-bash ships on the image)
- `actions/upload-artifact@v4` with `if-no-files-found: error`

Notes:

- Linux: `sudo apt-get install -y rpm fakeroot` (jpackage needs them); rpm/fakeroot not pre-installed.
- Windows: `MAVEN_OPTS: -Dstdout.encoding=UTF-8`; defensive WIX env-var resolution for the MSI build.
- mac: assert `${JAVA_HOME_25_arm64:?}` so an ARM runner fails loudly rather than silently building x64; Python 3.14 on PATH satisfies `build-dmg.sh`.
- All runner toolchains are pre-installed (Maven 3.9.16, JDK 25, ImageMagick, Inno Setup, WiX, Python) — no scoop/brew/setup-java installs.

### 3. `AGENTS.md`

Update the CI section: CircleCI is main-only; add a GitHub Actions bullet describing the workflow, the artifact naming, and the temporary-lane convention (a `push.branches` entry that must be removed before merge).

## Test procedure (two-step merge — sequencing matters)

1. **Merge the CircleCI main-only change to `main` first.** CircleCI reads config from the default branch, so the filter only protects feature branches once it's on `main`. This merge triggers a normal main build (expected).
2. Create `feat/github-actions`; add `.github/workflows/build.yml` (with the temporary branch entry) + AGENTS.md; push.
   - CircleCI: no pipeline (main-only config now active).
   - AppVeyor: no build (main-only).
   - GHA: runs on the push.
3. Iterate on the branch; each push re-runs GHA. Download the 3 artifacts, verify all 9 files, confirm the DMG is arm64 (`file X-Plane Manager*.dmg`).
4. **Remove `feat/github-actions` from `push.branches`**, merge to `main` → all three CIs build on main (final verification).

Watch-outs:

- Do not open a PR from the branch during testing — the `pull_request` trigger would double-build.
- The temporary branch entry is easy to forget — its removal is an explicit task and documented in AGENTS.md.

## Verification

- Syntax-check both YAML files (actionlint if available, else a YAML parse).
- On the branch: 3 GHA jobs green; 9 artifact files across 3 artifact bundles.
- On `main`: all three CIs green.
