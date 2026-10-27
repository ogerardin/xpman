# Skunkcrafts Beta Update Channels — Design

## Goal

Notify stable and beta Skunkcrafts updates separately, and when both are available let the user choose which one to install. Availability must be driven by actual file content (the CRC diff), matching the original SkunkCrafts Updater's truth model, not by version strings.

## Verified protocol facts

- Beta channels use an optional `skunkcrafts_updater_beta.cfg` shipped **locally** in the addon's Skunkcrafts base folder next to `skunkcrafts_updater.cfg`. Its `module|` URL points at the beta content (verified: SGES ships both cfgs in its archive; openSAM and xa-snow ship both via their templates/releases).
- The beta channel's latest version = `fetchRemoteVersion(betaModuleUrl)` — the normal cfg filename at the beta location.
- The CRC file diff against a channel's whitelist is the protocol's only "update available" signal. The `version|` lines are display-only labels, written back only when files are actually downloaded. This is why the original updater shows "files up to date but local and remote versions mismatch" for openSAM Library (version-only label bumps; plugin and library stamped in lockstep) and SGES beta (stable and beta whitelists currently byte-identical; only the label differs: `81.2` vs `Beta`).

## Semantics

- **Episodic model** — no channel state, no subscription, anywhere. Every update is a fresh choice between the rows offered. The local cfg's `module|` URL is never rewritten; only its `version|` line records what was last installed. Consequence: after installing a beta, the stable row reappears when stable content differs — the way back is free, and the official SkunkCrafts Updater client stays fully compatible with the folder.
- **Availability (content truth, both channels):**
  - Stable row ⟺ stable CRC diff > 0.
  - Beta row ⟺ local beta cfg exists ∧ beta whitelist ≠ stable whitelist ∧ beta CRC diff > 0.
  - Whitelist identity is the dedup rule: when the beta channel carries the same content as stable, the beta row is suppressed regardless of labels (SGES today). Conversely, same-label channels with different content (xa-snow) are offered — content decides, labels never suppress.
  - The version-mismatch state (diff = 0 but labels differ) produces no row and no badge. The wizard's page 1 reports it honestly ("Files up to date — local and remote versions differ") instead of a bare "Already up to date". This also fixes XPman's existing stable-channel dead end for openSAM Library, where a version-only bump currently yields a dashboard row that blocks in the wizard with nothing to download.
- **Install:** the existing `SkunkcraftsUpdater.applyUpdate(folder, config, progress)` already keys everything off the passed config's `moduleUrl()` — passing the parsed beta cfg redirects whitelist, downloads and CRC verification to the beta channel with no protocol changes. Afterwards the beta version label is written into the local `skunkcrafts_updater.cfg` `version|` line (existing `writeVersion` behavior).
- **Labels are display-only**: rows show `installed → channelLatest` from the cfg strings; they never gate anything.

## Approach

Keep all logic in `SkunkcraftsUpdatable` interface defaults so it lives once:

- `SkunkcraftsUpdater` gains a beta cfg filename constant and `findBetaConfig(folder)` (same parse, other filename). `fetchRemoteVersion`, `computeFilesToUpdateSummary` and `applyUpdate` are reused unchanged.
- Interface defaults: `getSkunkcraftsBetaConfig()` (local parse), `getSkunkcraftsBetaLatestVersion()`, `isSkunkcraftsUpdateAvailable()` redefined as stable CRC diff > 0 (was version inequality), `isSkunkcraftsBetaUpdateAvailable()` (the beta formula above), `getSkunkcraftsBetaUpdateSummary()`, `applySkunkcraftsBetaUpdate(progress)`, and an any-channel predicate for badges/counts.
- `Plugin`, `Aircraft`, `SceneryPackage` and `Sges` add lazily-cached availability (and beta latest version) mirroring the existing `skunkcraftsLatestVersion` lazy-getter idiom, so the CRC passes never run on the FX thread. Eligibility gating (`isSkunkcraftsUpdatable()`, including the disabled-plugin/scenery rules from the disabled-addon-updates design) covers both channels.
- The background warm-up pass (`PluginManager` line ~141 and the Home update checker) computes both channels' availability off the FX thread before the UI reads the cached values.
- UI, mirroring the existing X-Plane Release/Beta dashboard rows: Home updates section shows up to two rows per addon (`[Update]`, `[Update Beta]`), each opening the wizard pre-set to that channel; tile badges and card/row markers mean "any channel"; `UpdateAvailableInspection` emits a second "Beta update available: X" WARN message next to the stable one. Context menus keep their single existing item.
- **No dead ends at any entry point.** Every channel-less entry point (aircraft card Update button, context-menu items, version-column buttons and their `@EnabledIf` gates) is gated on *any-channel* availability. The wizard's channel resolution: the requested channel if it has an update, else the sole available channel; the Stable/Beta selector shows only when both channels have updates.
- **Inspection splits on Skunkcrafts-updatability.** For `isSkunkcraftsUpdatable()` targets, `UpdateAvailableInspection` reports content truth (stable message when the stable diff > 0, beta message when the beta row exists). For targets without a Skunkcrafts cfg that override `getLatestVersion()` with a custom publication channel (XIvAp's page scrape, ZiboMod738's Google Drive), the existing version-based WARN is unchanged — no whitelist exists to CRC there and the message is informational, so those addons keep their current behavior.
- **Pre-warm for all managers.** `AircraftManager` and `SceneryManager` gain the `PluginManager`-style pre-warm (both channels' availability computed on the background load thread, before LOADED fires). Today's racy Home-check warm-up is not sufficient under content truth — losing the race would put a whitelist fetch plus full CRC pass on the FX thread.
- `Sges` applies its existing `identifiesAsSges` ownership guard to the beta cfg, mirroring `ownUpdaterCfg`. Whitelist identity (the dedup rule) compares the parsed `List<WhitelistEntry>` (records give value equality).
- The exact page-1 selector widget (radio buttons vs segmented control) is left to the builder.

## Testing and documentation

- Availability matrix, content-based: no beta cfg; beta whitelist identical to stable (suppressed even when behind); beta whitelist differs and beta diff > 0 (offered, including same-label different-content); on beta with new beta content (offered); version-only label bump (no offer; wizard shows the mismatch message). Tests use a local `HttpServer` fixture (no network-injectable seam exists; `fetchText` casts to `HttpURLConnection`, so `file:` URLs are out).
- `findBetaConfig` present/absent; apply-beta redirects downloads and version write-back to the beta module URL (the SGES install test fixture already ships both cfgs); the inspection split (content truth for cfg-bearing addons, version-based WARN preserved for `getLatestVersion`-overriding addons without a cfg).
- Update the Home dashboard (02) and updating add-ons (10) manual chapters in the same commit; run `./scripts/build-manual.sh` before committing.
