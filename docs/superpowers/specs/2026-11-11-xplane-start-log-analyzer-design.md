# X-Plane Start Log Analyzer Design

## Intent

Give XPman users an easy way to review the latest X-Plane session for failures, errors, and inconsistencies that are easy to miss in the simulator, and to see a concise set of startup/session statistics.

## Goals

- Analyze the selected installation's `Log.txt` on demand from the Home screen.
- Surface actionable failures and group repetitive diagnostics so large logs remain readable.
- Summarize available session and startup facts, including X-Plane version, start time, clean/unclean exit, loaded/failed plugins, aircraft/flight start, and elapsed time to menu/flight where the log contains reliable markers.
- Reuse XPman's existing diagnostics dialog and `InspectionMessage` presentation.

## Non-goals

- A persistent log archive, multi-session comparisons, live tailing, or raw-log browsing.
- Automatic repair or modification of X-Plane files/configuration.
- Exhaustive interpretation of every X-Plane warning/error or full support for pre-XP11.3x log formats in v1.
- A new sidebar panel or a separate statistics dashboard.

## Constraints

- `XPlane.getLogPath()` already resolves the selected installation's `Log.txt`.
- XPman has a diagnostics dialog (`DiagUtil.displayInspectionMessages`) that accepts `InspectionMessage` rows and displays severity/details/fix actions.
- Keep parsing/domain analysis in `xpman-api`; JavaFX wiring stays in `xpman-fx`.
- Use Java 25 and existing dependencies/patterns; do not add a dependency for parsing text.
- The analyzer is read-only and must report missing/unreadable/empty logs clearly.
- X-Plane 11.3x+ and 12 logs share the tagged `H:MM:SS.mmm L/TAG:` grammar (verified against real 11.32r2 and 11.50b4 logs); only pre-XP11.3x/XP10-era untagged logs fall back to marker-only parsing. Header capitalization differs (`log.txt for…` vs `Log.txt for…`), so header matching is case-insensitive.
- No clean-shutdown marker always produces a WARN describing an unclean exit (crash, hung session, or still running); the analyzer does not inspect processes.
- Do not commit without an explicit user request.

## Approaches

1. **Home action + existing diagnostics dialog (chosen):** minimal UI change and reuses XPman's current inspection presentation. Stats appear as informational rows; no new navigation or view lifecycle.
2. **First-class Session Log panel:** gives statistics and findings more room, but adds a new section, view, and navigation complexity before the first analyzer rules are proven.
3. **Raw log browser:** useful for search but leaves interpretation and performance summaries to the user, so it under-serves both goals.

The user selected approach 1. The analyzer will be invoked on demand from Home and will display grouped findings and concise stats in the existing dialog.

## Proposed behavior

- Add an Analyze Log action beside the existing Home log-path affordance. Run file reading/analysis off the JavaFX thread and show the result through the existing dialog.
- Parse XP11.3x+/XP12 timestamped subsystem entries plus stable untagged markers for header, plugin load errors, plugin `Loaded:` records, main-menu/flight start, scenery list, DSF load times, and shutdown.
- Emit concise INFO rows for facts present in the log; do not invent unavailable timings or session facts.
- Detect explicit dynamic-library/plugin load failures, tagged `E/` and `W/` entries, and missing clean-shutdown markers. Group repeated messages by subsystem and normalized message while retaining occurrence counts and representative details.
- Where reliable installed-plugin inventory and log identity data are available, report enabled installed plugins absent from successful `Loaded:` records as possible load failures. Do not claim absence is a failure when the log format or identity mapping is ambiguous.
- Make clear that log-derived errors can include X-Plane's own data (for example global airport data) and are not automatically attributable to user add-ons.
- Include available performance facts such as time to main menu/flight, elapsed session time, and DSF scenery-tile load times; omit facts not present in a particular log.

## Validation and documentation

- Unit tests use a small anonymized/synthetic log fixture, including tagged and untagged lines, repeated diagnostics, plugin-load failure, phase markers, clean/unclean exit, and malformed or empty input.
- Update the relevant Home/manual documentation and verify it builds using the repository's documented manual command.
- Verify with `mvn test -pl xpman-api` and `mvn test -pl xpman-fx` (or the full `mvn test` if practical).

## Builder decisions

- Exact parser/result type names and package-private helpers are for the implementer, following existing XPman parser conventions.
- The precise normalization strategy should be conservative: group only messages that differ in obvious per-occurrence values (such as line numbers), preserving a representative original message and count.
- If an installed-plugin-to-log identity match cannot be done reliably from existing APIs, omit that cross-check in v1 rather than add a fragile heuristic.
