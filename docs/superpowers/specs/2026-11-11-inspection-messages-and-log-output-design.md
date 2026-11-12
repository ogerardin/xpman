# Inspection Messages and Log Output Design

## Intent

Make XPman's inspection results useful without forcing users to hover over an icon or understand raw X-Plane jargon. The feedback identified five problems: too many rows, important context hidden in a hover-only tooltip, unhelpful "N similar messages" labels, cryptic messages without consequences, and no general actions on a row.

## Goals

- Show details inline when a result is selected, with severity filters to reduce noise.
- Offer row actions using XPman's existing introspection/context-menu conventions: run fixes, search the web, and copy details.
- Explain common X-Plane log patterns in plain language, calibrate severity to user impact, and preserve the original lines (with line numbers) in the details pane.
- Group known diagnostics by recognized issue, pair adjacent related lines (e.g., snd-error + event description), and group unknown diagnostics only when their text is identical; show a meaningful representative message and occurrence count.
- Collapse cross-check findings (missing plugins, missing scenery) into a single row with a count and details listing each item.
- Condense session statistics to a single informational row.

## Non-goals

- Exhaustively interpret every X-Plane log message or offer speculative fixes.
- Automatically modify X-Plane files/settings from the knowledge base.
- Open an external editor at a specific line; source line numbers are provided in the details pane for manual reference.
- Stream multi-gigabyte logs; current XPman logs are expected to be small enough for in-memory viewing.

## Chosen approach

1. **Inspection action framework and display:** wrap `InspectionMessage` in an FX-only `UiInspectionMessage` that uses the existing method-introspection annotations and `IntrospectingContextMenuTableRowFactory`. Keep existing `Fix` objects and inline fix buttons. Replace hover-only details in the diagnostic dialog with a selected-row details pane and All/Errors/Warnings/Info filters. Details include line-number prefixes for manual reference.
2. **Log-specific output:** add a small curated `KnownLogIssue` enum that maps recognized patterns to explanation and consequence-based severity. Pair adjacent related lines (e.g., FMOD snd-error + event description) as a single occurrence. Group known patterns by issue, unknown patterns only by exact text, include occurrence counts (counting pairs not lines for paired diagnostics), and display one condensed session-statistics row.
3. **Cross-check consolidation:** emit a single row for all missing plugins and a single row for all missing scenery packs (with counts and details listing each item), rather than one row per missing item.
4. **Documentation:** update the Home Dashboard manual and verify the generated PDF.

## Alternatives considered

- **Action definitions directly on API messages:** rejected because API/domain types should not depend on JavaFX, clipboard, URL launching, or stage ownership. The FX wrapper keeps presentation and actions in the UI module and reuses the existing action framework.
- **Purely structural grouping/filtering without a knowledge base:** rejected because it would still leave the actual messages cryptic and their consequences unclear. The curated KB is intentionally small and allows raw-text fallback for unknown patterns.
- **External editor for open-at-line:** rejected because `Platform.openFile` cannot request a line number consistently across platforms. Source line numbers are provided in the details pane for users who want to manually locate the original log line.

## Constraints and behavior

- No new dependencies; use existing Lombok, JavaFX, `Fix`, `@Order`, `@Label`, `@EnabledIf`, and `IntrospectingContextMenuTableRowFactory`.
- Details appear in the dialog without hover. Context-menu actions are only available when their prerequisites exist (details present for copy, always available for web search).
- Detail lines include 1-based `Log.txt` line number prefixes (e.g., "line 388: 0:03:46.647 E/SOUN: ...") for manual reference.
- Adjacent snd-error + SOUN lines are paired and counted as one occurrence (not two).
- Recognized benign bundled-data issues may be INFO even if X-Plane logged them at WARN/ERROR; explanations must state likely consequence and whether the user needs to act.
- Unknown findings retain the original message, exact-text grouping, and line-number-prefixed details; no broad numeric normalization that could merge distinct paths.
- Cross-check rows (missing plugins/scenery) show counts and list items in details, avoiding duplicate rows for the same class of issue.
- Very large detail listings are accepted (no cap); the TextArea handles them, but if groups exceed thousands of lines, performance may need revisiting.
