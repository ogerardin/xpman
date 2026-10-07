# XPman User Manual — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a user manual as Markdown in the repo, rendered to PDF during CI (uploaded as a release artifact), and synced to the GitHub Wiki on every `main` push.

**Architecture:** 13 Markdown chapters in `docs/manual/` serve as the single source of truth. A build script (`scripts/build-manual.sh`) downloads a pinned Typst binary and runs pandoc to produce a PDF (title page + TOC). The script also generates a wiki-ready tree (prefix-stripped, images rewritten to raw.githubusercontent.com URLs, `_Sidebar.md` generated). A new `docs` CI job builds the PDF, uploads the artifact, and pushes the wiki tree to `ogerardin/xpman.wiki.git`. The `release` job gains `docs` in its `needs:` so the PDF ships with every GitHub Release.

**Tech Stack:** Bash (build script), pandoc 3.x + Typst v0.15.1 (PDF engine), GitHub Actions (CI), Git (wiki push).

---

## File Structure

| Path | Action | Responsibility |
|---|---|---|
| `scripts/build-manual.sh` | Create | Build PDF + wiki tree from `docs/manual/*.md` |
| `docs/manual/00-introduction.md` | Create | Intro, features, install, platform notes |
| `docs/manual/01-getting-started.md` | Create | First run, X-Plane folder, sidebar, shortcuts, theme |
| `docs/manual/02-home-dashboard.md` | Create | Start X-Plane, updates, disk usage, library |
| `docs/manual/03-aircraft.md` | Create | Browse, search, install, liveries, uninstall |
| `docs/manual/04-scenery.md` | Create | Table, enable/disable, reorder, Organize + Save |
| `docs/manual/05-scenery-classes.md` | Create | Regex rules, built-in classes, workflow |
| `docs/manual/06-navdata.md` | Create | Layers, AIRAC cycles, overrides |
| `docs/manual/07-plugins.md` | Create | Enable/disable, FlyWithLua, quarantine |
| `docs/manual/08-tools.md` | Create | Tools manager, install/uninstall, console |
| `docs/manual/09-installing-addons.md` | Create | Universal install wizard (3 pages) |
| `docs/manual/10-updating-addons.md` | Create | Skunkcrafts wizard, locked state |
| `docs/manual/11-settings.md` | Create | Settings dialog, `~/.xpman` |
| `docs/manual/12-troubleshooting.md` | Create | Common issues, submitting bugs |
| `.github/workflows/build.yml` | Modify | Add `docs` job; add `docs` to `release` needs |

---

### Task 1: Create `scripts/build-manual.sh`

**Files:**
- Create: `scripts/build-manual.sh`

- [ ] **Step 1: Create the build script**

```bash
#!/usr/bin/env bash
set -euo pipefail

# Usage: scripts/build-manual.sh [VERSION]
# VERSION defaults to "dev" for local builds; CI passes the real version.

VERSION="${1:-dev}"
DATE="$(date +%Y-%m-%d)"
TYPST_VERSION="${TYPST_VERSION:-0.15.1}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CACHE_DIR="$REPO_ROOT/.cache/typst"
OUTPUT_DIR="$REPO_ROOT/target"
MANUAL_DIR="$REPO_ROOT/docs/manual"
WIKI_DIR="$OUTPUT_DIR/manual-wiki"

# PDF filename follows installer convention: -SNAPSHOT inserted before extension for snapshots
SNAP=""
[[ "$VERSION" == *"-SNAPSHOT"* ]] && SNAP="-SNAPSHOT"
PDF_NAME="XPman-User-Manual${SNAP}.pdf"

# --- Download Typst if not cached ---
TYPST_BIN="$CACHE_DIR/typst-${TYPST_VERSION}/typst"
if [[ ! -x "$TYPST_BIN" ]]; then
    mkdir -p "$CACHE_DIR"
    URL="https://github.com/typst/typst/releases/download/v${TYPST_VERSION}/typst-x86_64-unknown-linux-musl.tar.xz"
    echo "Downloading Typst v${TYPST_VERSION}..."
    curl -sL "$URL" | tar -xJ -C "$CACHE_DIR"
    EXTRACTED=$(ls -d "$CACHE_DIR"/typst-x86_64-unknown-linux-musl 2>/dev/null || true)
    [[ -n "$EXTRACTED" ]] && mv "$EXTRACTED" "$CACHE_DIR/typst-${TYPST_VERSION}"
    chmod +x "$TYPST_BIN"
fi
export PATH="$(dirname "$TYPST_BIN"):$PATH"

# --- Build PDF ---
mkdir -p "$OUTPUT_DIR"
echo "Building PDF: $OUTPUT_DIR/$PDF_NAME"
pandoc \
    --pdf-engine=typst \
    --toc \
    --toc-depth=2 \
    -M "title=XPman User Manual" \
    -M "subtitle=Version ${VERSION} (${DATE})" \
    -M "author=The XPman Team" \
    -o "$OUTPUT_DIR/$PDF_NAME" \
    "$MANUAL_DIR"/[0-9]*.md

echo "PDF built: $OUTPUT_DIR/$PDF_NAME"

# --- Build wiki tree ---
rm -rf "$WIKI_DIR"
mkdir -p "$WIKI_DIR"

# Copy chapters with numeric prefix stripped; 00-introduction → Home
for f in "$MANUAL_DIR"/[0-9]*.md; do
    base="$(basename "$f")"
    stripped="${base#[0-9][0-9]-}"
    if [[ "$base" == "00-introduction.md" ]]; then
        stripped="Home.md"
    fi
    # Rewrite image paths: ../../assets/ → absolute raw.githubusercontent URL
    sed "s|\.\./\.\./assets/|https://raw.githubusercontent.com/ogerardin/xpman/main/assets/|g" \
        "$f" > "$WIKI_DIR/$stripped"
done

# Generate _Sidebar.md
{
    echo "# XPman User Manual"
    echo ""
    echo "- [Home](Home)"
    for f in "$MANUAL_DIR"/[0-9]*.md; do
        base="$(basename "$f")"
        [[ "$base" == "00-introduction.md" ]] && continue
        stripped="${base#[0-9][0-9]-}"
        title="${stripped%.md}"
        # Convert kebab-case to Title Case for sidebar display
        display=$(echo "$title" | sed 's/-/ /g' | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)}1')
        echo "- [${display}](${title})"
    done
} > "$WIKI_DIR/_Sidebar.md"

echo "Wiki tree built: $WIKI_DIR"
echo "Done."
```

- [ ] **Step 2: Make the script executable**

```bash
chmod +x scripts/build-manual.sh
```

- [ ] **Step 3: Verify the script runs (will need chapters first)**

Skip until Task 4 is complete. After chapters exist, run:

```bash
scripts/build-manual.sh
```

Expected: `target/XPman-User-Manual-dev.pdf` and `target/manual-wiki/` with `Home.md`, 12 chapter files, and `_Sidebar.md`.

---

### Task 2: Write chapters 00–03

**Files:**
- Create: `docs/manual/00-introduction.md`
- Create: `docs/manual/01-getting-started.md`
- Create: `docs/manual/02-home-dashboard.md`
- Create: `docs/manual/03-aircraft.md`

- [ ] **Step 1: Write `00-introduction.md`**

Content:
- Title: `# Introduction`
- What is XPman: "A cross-platform configuration manager for X-Plane 11 and 12."
- Feature overview (bullet list matching README): Aircraft, Scenery, Plugins, FlyWithLua, Nav data, Tools, X-Plane updates, Disk usage, Skunkcrafts Updater integration
- **Installation**: download link, table of platform formats (macOS `.dmg`/`.pkg`, Windows `.exe`/`.msi`, Linux `.deb`/`.rpm`). "All packages are bundled with a Java runtime — no separate Java installation required."
- **Windows SmartScreen**: "When running the EXE or MSI, Windows SmartScreen may display a warning. Click **More info** then **Run anyway** — XPman is 100% open source."
- **macOS Gatekeeper**: "macOS may block the app because it is not signed with an Apple Developer ID. Go to **System Settings > Privacy & Security** and click **Open Anyway**."
- Screenshot: Main window (`../../assets/screenshots/Screenshot 2026-09-17 at 23.50.17.png`) with caption "The XPman main window."
- Brief TOC sentence: "Read on to learn about each feature, or jump to [Troubleshooting](12-troubleshooting.md) if something goes wrong."

- [ ] **Step 2: Write `01-getting-started.md`**

Content:
- Title: `# Getting Started`
- **Selecting your X-Plane folder**: On first launch, XPman prompts you to select your X-Plane installation directory. Use **File → Select X-Plane folder...** at any time to switch. Invalid folders produce the error "Invalid X-Plane folder." XPman remembers your last folder and recent folders.
- **Recent folders**: **File → Open Recent** lists recently opened X-Plane installations. Stale entries (folder no longer exists) are offered for removal.
- **Main window layout**: Left sidebar with 5 navigation panels (Home, Aircraft, Scenery, Nav data, Plugins). Right content area shows the selected panel. Sidebar footer has Light/Dark theme toggle, Settings, and About buttons.
- **Keyboard shortcuts**: `Cmd/Ctrl+1..5` jumps to each panel. `Cmd/Ctrl+,` opens Settings. `Cmd/Ctrl+Q` quits.
- **Theme**: Click **Light theme** / **Dark theme** in the sidebar footer to toggle.
- **Window position**: XPman remembers your window position and size between launches.

- [ ] **Step 3: Write `02-home-dashboard.md`**

Content:
- Title: `# Home Dashboard`
- **X-Plane header**: Shows `X-Plane {version} ({variant})` or `No X-Plane installation selected`. The folder path is a hyperlink (click to reveal the executable in Finder/Explorer/Files). The `Log file` hyperlink opens the X-Plane log.
- **Buttons**: `Start X-Plane` (disabled if the selected X-Plane installation is for a different platform), `Install anything...` (opens the install wizard).
- **X-Plane Updates Available**: Rows for `Release` and `Beta` updates. Version arrows show current → available. `Run Installer` launches the updater. Release notes link when available. Beta updates show a confirmation dialog with a hint: "Check for new betas as well as updates."
- Screenshot: Main window (`../../assets/screenshots/Screenshot 2026-09-17 at 23.50.17.png`) with caption "The Home dashboard."
- **Disk usage**: Horizontal segmented bar showing Aircraft / Global scenery / Custom scenery / Disabled scenery / Other. Hover for details. Sizes are computed asynchronously.
- **Library tiles**: Four clickable tiles — `Aircraft`, `Scenery`, `Nav data`, `Plugins`. Each navigates to its panel. Badges show `N updates` when Skunkcrafts updates are available.
- **Updates Available**: Skunkcrafts updates grouped by `Aircraft` / `Scenery` / `Plugins`. Each row shows the addon name, `current → latest`, and an `Update` button (opens the Skunkcrafts update wizard).

- [ ] **Step 4: Write `03-aircraft.md`**

Content:
- Title: `# Aircraft`
- **Toolbar**: `Reload`, `Install...`, search field (`Name, author or studio`, debounced), `Filter` combo (`All`, `Category: …`, `Studio: …`, `Studio ≠ Laminar Research`).
- **Card grid**: Each aircraft is shown as a card with a thumbnail, name, `studio / author`, and badges (version, `Update available`, `N liveries`). Liveries are expandable mini-cards.
- **Hover actions** (icon buttons on card hover): Explore properties, Inspect, Reveal, Uninstall. Double-click opens **Aircraft details** (Name/Value tree of ACF properties).
- Screenshot: Aircraft panel (`../../assets/screenshots/Screenshot 2026-09-17 at 23.51.12.png`) with caption "The Aircraft panel."
- **Context menu** (right-click a card or livery):
  - `Reveal in Finder` / `Show in Explorer` / `Show in Files` (platform-dependent label)
  - `Uninstall aircraft` (with confirmation listing affected aircraft)
  - `Inspect`
  - `Explore properties`
  - `Update via Skunkcrafts Updater` (if applicable)
  - `Skunkcrafts: Locked by developer` (when the developer has disabled updates)
  - `Links` submenu (dynamic, from manifest)
  - `Manuals` submenu (dynamic, PDF manuals)
- **Empty state**: `No aircraft to show`

---

### Task 3: Write chapters 04–07

**Files:**
- Create: `docs/manual/04-scenery.md`
- Create: `docs/manual/05-scenery-classes.md`
- Create: `docs/manual/06-navdata.md`
- Create: `docs/manual/07-plugins.md`

- [ ] **Step 1: Write `04-scenery.md`**

Content:
- Title: `# Scenery`
- **Toolbar**: `Reload`, `▲`/`▼` (Move Scenery Pack up/down one rank in `scenery_packs.ini`), `Install...`, `Scenery classes...`, `Organize scenery_pack.ini`, `Open scenery_packs.ini` (opens in system text editor).
- **Table columns**: (icon), (update marker), `Rank`, `Enabled` (toggle switch — only for ini-listed entries), `Name`, `Version`, `Has airport?` (Airport pill), `Library?` (Library pill), `Tile count`, `Obj count`, `Class` (from organizer; `Folder missing` when applicable). Column-header tooltips explain each.
- **Reordering**: Drag-and-drop rows (ini-listed entries only), or use `▲`/`▼` toolbar buttons. Drop-target highlight shows the insertion point.
- **Enable/Disable**: Toggle the `Enabled` switch. Disabled entries are styled differently. Folder-missing entries are also visually distinct.
- **Context menu** (right-click a row):
  - `Reveal`
  - `Enable Scenery Package` / `Disable Scenery Package`
  - `Add to scenery_packs.ini` / `Remove from scenery_packs.ini`
  - `Uninstall` (with confirmation)
  - `Inspect`
  - `Update via Skunkcrafts Updater` / `Skunkcrafts: Locked by developer`
  - `Links` submenu (dynamic)
- **Organize workflow**: Click `Organize scenery_pack.ini`. A confirmation dialog appears: "This will add unlisted scenery packages, remove invalid entries, and sort scenery packages by class rank. No change will be saved until you click on the 'Save' button." After organizing, the `UNSAVED CHANGES` indicator appears in the toolbar. Review the new order, then click `Save` to persist.
- Screenshot: Scenery panel (`../../assets/screenshots/Screenshot 2026-09-17 at 23.52.27.png`) with caption "The Scenery panel."
- **Empty state**: `No scenery to show`

- [ ] **Step 2: Write `05-scenery-classes.md`**

Content:
- Title: `# Scenery Classes`
- Open via **Scenery classes...** button in the Scenery panel toolbar.
- **Purpose**: Scenery classes determine the load order in `scenery_packs.ini`. Name-matching (regex) rules are consulted before file-based classes (Airport, Library, Other); position determines rank.
- **Rules table**: Columns `Priority` (row number), `Name` (editable), `RegEx` (editable, validated). Toolbar: `+` (add rule), `-` (delete rule; built-in rows are italic and not deletable), `▲`/`▼` (move rule), `Restore defaults`.
- **Built-in classes** (file-based, non-editable): `Airport`, `Library`, `Other`. Default regex classes: `X-Plane Landmark`, `Global Airports`, `Overlay scenery`, `Mesh scenery`.
- **Workflow**: Edit classes → click `Apply` (persists to `~/.xpman`, reloads the Scenery panel) → optionally click `Organize scenery_pack.ini` in the Scenery panel → review → `Save`.
- **Buttons**: `Apply` (persists changes), `Close`.

- [ ] **Step 3: Write `06-navdata.md`**

Content:
- Title: `# Nav Data`
- **Toolbar**: `Reload`, `Install...`
- **Layer cards**: Vertical stack. Each card shows a status ball (inactive/OK/warnings/errors; hover for reasons), name, `Layer i/n` badge, short state (e.g., `Cycle 2610`), and a help button.
- **Help button**: Opens the **Nav data info dialog**: rendered description, folder path, `AIRACcycle`, `Metadata`, `Build`, link `X-Plane nav data documentation`.
- **Override semantics**: If a layer overrides others, it shows an `overrides N layers` badge. If it's overridden, `ignored by layer i`.
- **Files**: Each card has an `N files` toggle. Expanding shows per-file rows (icon, name, `absent` marker if missing, `AIRAC …` badge, size · modified date).
- Screenshot: Nav data panel (`../../assets/screenshots/Screenshot 2026-09-17 at 23.52.45.png`) with caption "The Nav data panel."
- **Context menu**: `Reveal`, `Inspect` (opens Inspection results dialog).
- **Empty states**: `Loading nav data...`, `No nav data to show`

- [ ] **Step 4: Write `07-plugins.md`**

Content:
- Title: `# Plugins`
- **Toolbar**: `Reload`, `Install...`
- **Tree table**: Columns (system icon), (update marker), `Enabled` (toggle switch), `Name`, `Description`, `Version`, `Latest version`. FlyWithLua plugin expands to its Lua scripts, plus groups `Disabled scripts (n)` / `Quarantined scripts (n)`.
- **Plugin context menu**:
  - `Reveal`
  - `Remove macOS quarantine` (macOS only, when quarantined)
  - `Enable plugin` / `Disable plugin`
  - `Uninstall plugin` (with confirmation showing details)
  - `Inspect`
  - `Update via Skunkcrafts Updater` / `Skunkcrafts: Locked by developer`
  - `Links` submenu (dynamic)
  - `Manuals` submenu (dynamic)
- **Script context menu** (FlyWithLua scripts):
  - `Enable script` / `Disable script`
  - `Quarantine script` / `Unquarantine script`
  - `Reveal`
  - `Open in text editor`
  - `Manuals` submenu
  - `Links` submenu
  - `Uninstall script` (with confirmation)
- Screenshot: Plugins panel (`../../assets/screenshots/Screenshot 2026-09-17 at 23.53.36.png`) with caption "The Plugins panel."
- **Empty states**: `Loading plugins...`, `No plugins to show`

---

### Task 4: Write chapters 08–12

**Files:**
- Create: `docs/manual/08-tools.md`
- Create: `docs/manual/09-installing-addons.md`
- Create: `docs/manual/10-updating-addons.md`
- Create: `docs/manual/11-settings.md`
- Create: `docs/manual/12-troubleshooting.md`

- [ ] **Step 1: Write `08-tools.md`**

Content:
- Title: `# Tools`
- **Toolbar**: `Reload`, `Install...`
- **Tools manager dialog** (open via **Tools → Manage tools...** or **File → Settings... → Tools**): Segmented filter `Available` / `Installed`. Left side: tool cards (name, version, `Installed`/`Available` status, `Install`/`Uninstall` action buttons). Right side: detail view (icon, name, version, status, description, `Platform:`, `X-Plane version:`, `Installed at:`, `Tool homepage` link).
- **Install/Uninstall**: Opens the **Console dialog** (title "Installing/Uninstalling {tool}"): progress indicator, message, progress bar, output log. Buttons: `Cancel` (during operation), `Close` (after completion).
- **Running tools**: Each installed tool appears in the **Tools** menu. Clicking a tool launches it. If a tool is not installed, XPman asks "{tool} is not installed. Do you want to install and run it now?"
- Screenshot: Tools panel (`../../assets/screenshots/Screenshot 2026-09-17 at 23.53.58.png`) with caption "The Tools panel."
- **Empty state**: `No tools to show`

- [ ] **Step 2: Write `09-installing-addons.md`**

Content:
- Title: `# Installing Add-ons`
- XPman can install aircraft, scenery, plugins, and other add-ons from `.zip` or `.7z` archives via the **Universal installer**.
- **Opening the wizard**: **File → Universal installer...**, or `Install...` buttons in each panel's toolbar.
- **Page 1 — Source archive**: "Please select source archive (.zip or .7z file)". Text field `Source file:` + `Browse...` button (file chooser filters for `Archives *.zip *.7z`). `Next` is enabled only when the file exists ("File does not exist!" validation if invalid).
- **Page 2 — Inspection results**: "Please review the messages below and press 'Next' to proceed with installation." Table with columns `Severity` (icon) and `Message`. XPman auto-detects the archive content (aircraft, scenery, plugin, etc.) and runs inspections. If any `ERROR` is present, `Next` is disabled. Warnings are non-blocking.
- **Page 3 — Installing**: "Installing...". Shows current file label + progress bar. `Previous`/`Finish` are disabled during installation. Errors are shown in an error dialog. On success, shows "Done!" and `Finish` becomes enabled.
- **Notes**: The universal installer handles all content types. XPman detects whether the archive contains an aircraft, scenery package, plugin, or tool, and applies the appropriate installation logic.

- [ ] **Step 3: Write `10-updating-addons.md`**

Content:
- Title: `# Updating Add-ons`
- XPman integrates with the **Skunkcrafts Updater** protocol. Plugins, aircraft, and scenery packages with a `skunkcrafts_updater.cfg` file can be updated directly from their repositories.
- **Detecting updates**: The Home dashboard shows an `Updates Available` section grouped by category. Each panel (Aircraft, Scenery, Plugins) also shows an update marker on addons with available updates. Library tiles show `N updates` badges.
- **Starting an update**: Click `Update` in the Home dashboard, or right-click the addon and choose `Update via Skunkcrafts Updater`.
- **Skunkcrafts Update wizard**:
  - **Page 1 — Review**: "Review update information". Shows `Addon:`, `Current version:`, `Available version:` (or "Could not fetch remote version"), `Update status:` ("Already up to date" / "N file(s) to update (size)"). `Next` is disabled when nothing to update.
  - **Page 2 — Updating**: "Updating...". Shows `Preparing update...` label + progress bar.
- **Locked by developer**: Some addons show `Skunkcrafts: Locked by developer` instead of an update option. This means the developer has disabled remote updates for that addon. Contact the developer for updates.

- [ ] **Step 4: Write `11-settings.md`**

Content:
- Title: `# Settings`
- **Opening Settings**: **File → Settings...** (or `Cmd/Ctrl+,`). The Settings dialog is modal with a category tree on the left and a settings pane on the right. Buttons: `OK` / `Cancel`.
- **General category**: Checkbox `Confirm quit`. When enabled (default), XPman shows a "Do you really want to quit?" dialog on quit.
- **Tools category**: Embeds the tools manager (same as **Tools → Manage tools...**). Reload toolbar, `Available` / `Installed` filter, tool cards, detail view.
- **Persistence**: XPman saves your preferences to `~/.xpman` (JSON). Persisted settings:
  - `lastXPlanePath` — last opened X-Plane folder (reopened at startup)
  - `recentPaths` — recent X-Plane folders (File → Open Recent)
  - `theme` — `"dark"` (default) / `"light"`
  - `confirmQuit` (default `true`)
  - `lastPosition` — main window position and size
  - `settingsPosition` + `settingsCategory` — Settings dialog geometry and last category
  - `sceneryClasses` — user-defined scenery organizer rules
- **Dialog position**: The Settings dialog remembers its position and the last selected category.

- [ ] **Step 5: Write `12-troubleshooting.md`**

Content:
- Title: `# Troubleshooting`
- **Invalid X-Plane folder**: If you select a folder that is not a valid X-Plane installation, XPman shows the error "Invalid X-Plane folder." Ensure you select the top-level X-Plane directory (the one containing the executable).
- **Windows SmartScreen**: See [Installation](00-introduction.md#installation). Click **More info** then **Run anyway**.
- **macOS Gatekeeper**: See [Installation](00-introduction.md#macos-gatekeeper). Go to **System Settings > Privacy & Security** and click **Open Anyway**.
- **macOS quarantine on plugins**: Some plugins downloaded from the internet are quarantined by macOS. Right-click the plugin in the Plugins panel and choose `Remove macOS quarantine` to allow it to run.
- **Submitting issues**: If you encounter a bug or unexpected behavior, use **Help → Submit issue…** to open the GitHub issues page. Include your X-Plane version, XPman version (from **Help → About XPman**), and steps to reproduce.
- **Logs**: XPman logs are written to the console. Use **Help → ? Help** to open the Wiki (this manual). The X-Plane log is accessible via the `Log file` hyperlink on the Home dashboard.
- **About XPman**: **Help → About XPman** shows the version, build number, commit hash, Java runtime, VM, CPU cores, and links to the GitHub repo and license (GPLv3).

---

### Task 5: Modify `.github/workflows/build.yml`

**Files:**
- Modify: `.github/workflows/build.yml`

- [ ] **Step 1: Add the `docs` job**

Insert the following job **before** the `release:` job (around line 190, after the `mac:` job ends):

```yaml
  docs:
    name: Build manual (PDF + Wiki)
    runs-on: ubuntu-24.04
    timeout-minutes: 15
    steps:
      - uses: actions/checkout@v5
        with:
          fetch-depth: 0

      - name: Compute version
        shell: bash
        run: |
          source scripts/ci-version.sh "${GITHUB_REF_NAME:-}"
          echo "REVISION=${REVISION}" >> "$GITHUB_ENV"

      - name: Build manual (PDF + wiki tree)
        run: scripts/build-manual.sh "$REVISION"

      - name: Prepare artifact
        run: |
          mkdir -p dist/manual
          cp target/XPman-User-Manual*.pdf dist/manual/
          ls -l dist/manual

      - name: Upload manual artifact
        uses: actions/upload-artifact@v6
        with:
          name: manual
          path: dist/manual
          if-no-files-found: error

      - name: Publish wiki
        if: github.event_name == 'push' && github.ref == 'refs/heads/main'
        continue-on-error: true  # Temporary: GITHUB_TOKEN may not push to wiki; swap to secrets.WIKI_TOKEN if it fails
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          git clone --depth 1 "https://x-access-token:${GH_TOKEN}@github.com/ogerardin/xpman.wiki.git" target/wiki-repo
          rsync -a --delete target/manual-wiki/ target/wiki-repo/
          cd target/wiki-repo
          git config user.name "github-actions[bot]"
          git config user.email "github-actions[bot]@users.noreply.github.com"
          git add -A
          if git diff --cached --quiet; then
            echo "No wiki changes to commit."
          else
            git commit -m "Update manual from ${GITHUB_SHA}"
            git push
          fi
```

- [ ] **Step 2: Update the `release` job's `needs:`**

Change line 194 (or the line with `needs: [linux, windows, mac]`) to:

```yaml
    needs: [linux, windows, mac, docs]
```

This ensures the PDF is built before the release job runs, so it's included in the GitHub Release (the existing `download-artifact` with `merge-multiple: true` picks it up automatically).

---

### Task 6: Verify

- [ ] **Step 1: Run the build script locally**

```bash
scripts/build-manual.sh
```

Expected output:
```
Building PDF: /path/to/repo/target/XPman-User-Manual-dev.pdf
PDF built: /path/to/repo/target/XPman-User-Manual-dev.pdf
Wiki tree built: /path/to/repo/target/manual-wiki
Done.
```

- [ ] **Step 2: Inspect the PDF**

Open `target/XPman-User-Manual-dev.pdf` and verify:
- Title page: "XPman User Manual", "Version dev (YYYY-MM-DD)", "The XPman Team"
- Table of contents with all 13 chapters
- Chapters appear in order (00 through 12)
- Screenshots are embedded and visible
- Formatting is clean

- [ ] **Step 3: Inspect the wiki tree**

List `target/manual-wiki/` and verify:
- `Home.md` (renamed from `00-introduction.md`)
- `getting-started.md`, `home-dashboard.md`, ..., `troubleshooting.md` (12 files, prefix stripped)
- `_Sidebar.md` (generated, lists chapters in order with title-cased names)
- Image paths in the markdown files are absolute `https://raw.githubusercontent.com/ogerardin/xpman/main/assets/...`

- [ ] **Step 4: Test with a version argument**

```bash
scripts/build-manual.sh "1.0.1-SNAPSHOT"
```

Expected: PDF is named `XPman-User-Manual-SNAPSHOT.pdf` (SNAPSHOT inserted before `.pdf`, matching installer convention).

- [ ] **Step 5: Test CI (optional, after merge)**

After merging to `main`, watch the CI run. The `docs` job should:
- Build the PDF and upload it as an artifact
- Attempt to push the wiki tree (may fail if GITHUB_TOKEN can't push to `.wiki.git`; if it fails, swap to `secrets.WIKI_TOKEN` and remove `continue-on-error`)
- On release, the PDF should appear in the GitHub Release alongside installers

---

## Self-Review Checklist (for the implementer)

After completing all tasks, verify:
- [ ] All 13 chapters exist in `docs/manual/` with numeric prefixes
- [ ] `scripts/build-manual.sh` is executable and produces a valid PDF
- [ ] Wiki tree is generated with prefix-stripped filenames and rewritten image paths
- [ ] CI workflow includes the `docs` job and `release` job's `needs` is updated
- [ ] No placeholders or TBDs in any chapter
