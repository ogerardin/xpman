# Priority 1: Crash Detection & Missing Scenery

## 1. Crash Detection with Backtrace & Plugin Inference

### Objective
Detect crashes in the log, extract backtraces, infer offending plugins, and present as ERROR-level findings.

### Implementation Plan

#### 1.1 Add Crash Detection Method

**Location:** `StartLogInspection.java`

**Method signature:**
```java
private static boolean detectCrashes(SessionLog log, List<InspectionMessage> messages)
```

**Logic:**
1. Search last 300 lines of log for crash signatures (search backward from end)
2. Detect crash categories:
   - **Plugin crash:** Line contains "This application has crashed because of the plugin:"
     - Extract plugin name after colon
     - Create ERROR message: "Crash caused by plugin: <name>"
     - Include backtrace in details
   - **Memory exhaustion:** Line contains any of:
     - "out of memory"
     - "vram allocation failed"
     - "metal device error"
     - "mtlcommandbuffer" + "failed"
     - Create ERROR message: "Crash due to memory/VRAM exhaustion"
     - Include backtrace in details
   - **Fatal signal:** Line contains any of:
     - "SIGSEGV"
     - "SIGBUS"
     - "SIGABRT"
     - "EXC_BAD_ACCESS"
     - Create ERROR message: "Fatal signal: <signal>"
     - Infer offending plugin from nearby .xpl references
     - Include backtrace in details
   - **Sim abort:** Line contains any of:
     - "assertion failed"
     - "sim abort"
     - "unhandled exception"
     - Create ERROR message: "Simulator abort: <reason>"
     - Include backtrace in details

3. For each crash:
   - Extract backtrace from surrounding lines (search ±10 lines from crash)
   - Look for lines matching backtrace patterns:
     - Contains hex addresses: `0x[0-9a-fA-F]+`
     - Contains stack frames: `[frame #\d+:|^\d+\s+0x]`
     - Contains .xpl references
   - Infer offending plugin:
     - Search ±15 lines for .xpl file references
     - Extract plugin name from path (remove .xpl extension, take last component)
   - Create InspectionMessage:
     - Severity: ERROR
     - Object: "Crash" (or plugin name if inferred)
     - Message: Crash category + reason
     - Details: Full backtrace (all lines)
   - Return true if any crash detected

#### 1.2 Call Crash Detection

**Location:** `StartLogInspection.inspect()` method

**Change:** Add `boolean crashDetected = detectCrashes(log, messages);` after `addTaggedDiagnostics()` and before the unclean exit check.

#### 1.3 Replace Unclean Exit Warning

**Current code:**
```java
if (!log.hasCleanExit()) {
    messages.add(InspectionMessage.builder().severity(Severity.WARN)
            .message("No clean shutdown marker — X-Plane crashed, is still running, or the session hung").build());
}
```

**New logic:**
```java
boolean crashDetected = detectCrashes(log, messages);
if (!crashDetected && !log.hasCleanExit()) {
    messages.add(InspectionMessage.builder().severity(Severity.WARN)
            .message("No clean shutdown marker — X-Plane may have crashed, is still running, or the session hung").build());
}
```

---

## 2. Missing Scenery Asset Detection

### Objective
Detect missing scenery assets (.obj, .dsf, .png, etc.) and group them by package.

### Implementation Plan

#### 2.1 Add Missing Scenery Detection Method

**Location:** `StartLogInspection.java`

**Method signature:**
```java
private static void detectMissingScenery(SessionLog log, List<InspectionMessage> messages)
```

**Logic:**
1. Scan all log entries for missing asset patterns:
   - Line contains any of:
     - "unable to load"
     - "failed to find resource"
     - "failed to open"
     - "missing object"
     - "could not find object"
     - "cannot find object"
   - AND line contains any of:
     - ".obj", ".dsf", ".ter", ".pol", ".fac", ".for", ".png", ".dds"
     - "Custom Scenery"

2. For each matching line, extract asset path:
   - Try to find path in quotes: '...' or "..."
   - Try to find path after colon: "object file:", "resource:", "failed to open"
   - Fall back to full message

3. Determine asset type from extension:
   - .obj → "3D Object"
   - .dsf → "Terrain Mesh"
   - .png/.dds → "Texture"
   - .ter → "Terrain"
   - .pol → "Polygon"
   - .fac → "Facade"
   - .for → "Forest"
   - Other → "Asset"

4. Infer package name:
   - Parse path for pattern: "Custom Scenery/<PackageName>/"
   - Fall back to first path component
   - Fall back to filename

5. Group by package:
   - Map: package name → list of (asset path, asset type, line number)

6. Create InspectionMessage per package:
   - Severity: ERROR
   - Object: Package name
   - Message: "Missing assets from <Package> (<count> items)"
   - Details: List all missing assets with types and line numbers:
     ```
     line 123: path/to/asset1.obj (3D Object)
     line 145: path/to/asset2.png (Texture)
     ```

#### 2.2 Call Missing Scenery Detection

**Location:** `StartLogInspection.inspect()` method

**Change:** Add `detectMissingScenery(log, messages);` after `detectCrashes()` and before the unclean exit check.

---

## 3. Test Plan

### 3.1 Crash Detection Tests

**Location:** `StartLogInspectionTest.java`

**Test cases:**
1. `pluginCrashIsDetected` - Create log with plugin crash message and backtrace
2. `memoryExhaustionIsDetected` - Create log with "out of memory" message
3. `fatalSignalIsDetected` - Create log with "SIGSEGV" and .xpl reference nearby
4. `uncleanExitNotReportedWhenCrashDetected` - Verify no unclean exit warning when crash is found
5. `uncleanExitReportedWhenNoCrash` - Verify unclean exit warning when no crash detected

### 3.2 Missing Scenery Tests

**Location:** `StartLogInspectionTest.java`

**Test cases:**
1. `missingObjIsDetected` - Create log with "failed to find resource: 'path.obj'"
2. `missingAssetsGroupedByPackage` - Create log with multiple missing assets from same package
3. `missingSceneryInferredFromPath` - Verify package name extracted from "Custom Scenery/PackName/"
4. `assetTypesCorrectlyClassified` - Verify .obj/.dsf/.png classified correctly

---

## 4. Implementation Order

1. Implement crash detection (Section 1.1)
2. Update call site (Section 1.2)
3. Update unclean exit logic (Section 1.3)
4. Write crash detection tests (Section 3.1)
5. Run tests, verify crash detection works
6. Implement missing scenery detection (Section 2.1)
7. Add call site (Section 2.2)
8. Write missing scenery tests (Section 3.2)
9. Run all tests, verify both features work

---

## 5. Edge Cases & Considerations

### Crash Detection
- **Multiple crashes:** If multiple crash signatures found, report all (one message each)
- **Partial backtraces:** If backtrace extraction finds only a few lines, include what's found
- **No backtrace:** If no backtrace found, details should be empty or say "No backtrace available"
- **Plugin name extraction:** Handle edge cases where plugin name contains spaces or special chars

### Missing Scenery
- **Duplicate assets:** If same asset appears multiple times in log, deduplicate
- **Empty package name:** If package inference fails, use "Unknown Package"
- **Path parsing:** Handle both forward slash (/) and backslash (\) in paths
- **Case sensitivity:** Pattern matching should be case-insensitive
- **Large asset lists:** If a package has 100+ missing assets, include all in details (full backtrace per user request)

### Performance
- Both features scan log entries once (O(n))
- Backtrace extraction scans limited range (±10 lines)
- Missing scenery grouping uses HashMap (O(n))
- No performance concerns expected
