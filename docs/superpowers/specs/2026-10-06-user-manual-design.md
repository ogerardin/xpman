# XPman User Manual — Design

## Goal

Ship a user manual for XPman that:
1. Is rendered as a PDF during CI and uploaded as a release artifact.
2. Populates the GitHub Wiki pages on every `main` push.

Source-of-truth is Markdown checked into the repo; CI derives both artifacts from it.

## Decisions recorded

| Question | Answer |
|---|---|
| PDF engine | Typst (via pandoc `--pdf-engine=typst`) |
| Wiki auth | `WIKI_TOKEN` repository secret containing a classic PAT with `repo` scope |
| Wiki update cadence | Every `main` push (never PRs) |
| Screenshots | Reuse existing `assets/screenshots/*.png` |
| Manual location | `docs/manual/` |
| Source language | English |

## Layout

```
docs/manual/
  00-introduction.md       → wiki: Home.md
  01-getting-started.md
  02-home-dashboard.md
  03-aircraft.md
  04-scenery.md
  05-scenery-classes.md
  06-navdata.md
  07-plugins.md
  08-tools.md
  09-installing-addons.md
  10-updating-addons.md
  11-settings.md
  12-troubleshooting.md
```

Numeric prefixes define PDF concatenation order. Each chapter maps 1:1 to a Wiki page (prefix stripped on copy).

## `scripts/build-manual.sh`

Single script used by both CI and local dev. Responsibilities:
- Accept an optional version argument (default `dev`); date is `$(date +%Y-%m-%d)`.
- Download a pinned Typst binary into a local cache if absent (idempotent).
- Run `pandoc` with `--pdf-engine=typst`, `--toc`, title page metadata (`XPman User Manual`, subtitle with version + date), output PDF.
- Screenshots referenced via repo-relative paths (`../assets/screenshots/...`); pandoc resolves them from the repo root.
- Produce a `target/manual-wiki/` tree containing:
  - Chapters copied with prefix stripped (`01-getting-started.md` → `getting-started.md`)
  - `00-introduction.md` copied to `Home.md`
  - Generated `_Sidebar.md` listing the chapters in order
  - Image paths rewritten from `../assets/` to `https://raw.githubusercontent.com/ogerardin/xpman/main/assets/` (one `sed`)

The script follows the existing `assets/mac/dmgbuild/build-dmg.sh` convention — CI calls it like any other asset script.

## CI — new `docs` job

Added to `.github/workflows/build.yml`, runs on `ubuntu-24.04`, parallel with the build jobs (no Maven, no JDK).

```
docs:
  steps:
    - checkout (fetch-depth: 0)
    - compute version via scripts/ci-version.sh (REVISION, IS_SNAPSHOT)
    - run scripts/build-manual.sh $REVISION
    - upload artifact named "manual" (PDF + wiki tree)
      artifact file renamed to XPman-User-Manual[-SNAPSHOT].pdf
      (matches installer convention: -SNAPSHOT inserted before extension)
    - wiki push (main pushes only):
        clone using secrets.WIKI_TOKEN into $RUNNER_TEMP/wiki-repo
        rsync target/manual-wiki/ into the clone, excluding /.git/
        commit and push using git -C $RUNNER_TEMP/wiki-repo
```

`release` job's `needs:` extended with `docs` so the PDF ships with every GitHub Release (the existing `download-artifact` with `merge-multiple: true` picks it up automatically).

The wiki is a separate Git repository, so the docs job uses the `WIKI_TOKEN` repository secret with a classic PAT (`repo` scope). The clone lives under `$RUNNER_TEMP`, outside the checkout. Rsync must exclude `/.git/` when using `--delete`, or it erases the wiki clone's Git metadata; Git commands use `git -C` so they never fall back to the main checkout. Wiki publication errors fail the docs job rather than silently skipping publication.

## Manual steps (one-time, out-of-band)

1. Create a classic GitHub personal access token with `repo` scope.
2. Add it as the repository Actions secret `WIKI_TOKEN`.

The wiki must be initialized through the GitHub UI once before its Git repository is available.

## Chapter content

Each chapter is grounded in the actual UI — exact labels, buttons, menus, and flows from the feature inventory. No filler.

- **00-introduction**: what is XPman, features overview, installation per platform (SmartScreen/Gatekeeper notes reused from README), TOC of chapters
- **01-getting-started**: first run, selecting X-Plane folder (directory chooser), recent folders (`File → Open Recent`), sidebar navigation (5 panels + shortcuts), theme toggle, window position persistence
- **02-home-dashboard**: X-Plane header, Start X-Plane, X-Plane Updates Available section, Disk Usage bar, Library tiles, Updates Available section
- **03-aircraft**: toolbar (Reload, Install, Search, Filter), card grid, livery mini-cards, context menu actions (Reveal, Uninstall, Inspect, Explore properties, Skunkcrafts, Links/Manuals)
- **04-scenery**: table columns, enable/disable toggle, drag + ▲▼ reorder, Organize workflow + Save, Unsaved Changes indicator
- **05-scenery-classes**: rules table, regex, built-in classes (non-deletable), workflow: edit → Apply → Organize → Save
- **06-navdata**: layer cards, status balls, AIRAC cycle, overrides semantics, Nav data info dialog
- **07-plugins**: tree table, FlyWithLua scripts (enable/disable/quarantine), macOS quarantine removal, uninstall, Links/Manuals
- **08-tools**: tools manager dialog, Available/Installed filter, install/uninstall, console dialog, running tools from the Tools menu
- **09-installing-addons**: 3-page universal install wizard (source archive → inspection → progress), ERROR disables Next
- **10-updating-addons**: 2-page Skunkcrafts update wizard, "Already up to date", "Locked by developer" state
- **11-settings**: settings dialog layout, General (Confirm quit), Tools category, ~/.xpman file
- **12-troubleshooting**: invalid X-Plane folder, Windows SmartScreen, macOS Gatekeeper, submitting issues, logs

Screenshots are embedded where they add clarity (main window, each major panel, install wizard, settings).

## Verification

- **Local**: run `scripts/build-manual.sh` without args → produces `XPman-User-Manual-dev.pdf` and `target/manual-wiki/`. Open PDF, verify title page, TOC, screenshots, chapter order.
- **CI (PR)**: `docs` job produces artifact; download and spot-check PDF.
- **CI (main push)**: verify the `Publish wiki` step pushes the generated tree to `ogerardin/xpman.wiki.git` and that the wiki renders the updated pages.
- **Release**: confirm the PDF is present in the GitHub Release alongside installers.
