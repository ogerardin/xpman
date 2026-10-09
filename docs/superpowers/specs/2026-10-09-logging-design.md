# Logging Improvements Design

## Goal

Write XPman logs to platform-appropriate files by default, let users enable debug logging for the current session, provide a ZIP export for support, and bound disk usage through rotation.

## Design

Use Logback's `<define>` extension to resolve a `LOG_DIR` property while `logback.xml` is parsed. A `LogDirDefiner` delegates to a shared log utility, which asks `Platforms.getCurrent()` for the platform-specific directory and creates it. This avoids relying on application startup order to set a system property before SLF4J initializes.

The `Platform` interface gains `getLogDir(Path userHome)`, defaulting to `~/.xpman/logs`. macOS uses `~/Library/Logs/XPman`, Windows uses `%LOCALAPPDATA%/XPman/logs` with `~/AppData/Local/XPman/logs` as fallback, and Linux uses `$XDG_STATE_HOME/XPman/logs` with `~/.local/state/XPman/logs` as fallback.

Logback keeps the console appender and adds a rolling file appender at `xpman.log`. Size-and-time rotation caps individual files at 10 MB, keeps seven days, caps total archives at 50 MB, and cleans old history on startup.

The Help menu gains a session-only **Debug logging** check item that switches the root logger between INFO and DEBUG. It also gains **Export logs…**, which saves a ZIP containing regular files from the log directory. Export failures are shown to the user.

## Verification and documentation

Add path tests for platform log directories and a ZIP utility test using temporary files. Update the Troubleshooting manual with log locations, rotation limits, the debug toggle, and ZIP export instructions.
