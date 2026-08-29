# Ini-driven Scenery Screen

**Date:** 2026-08-30
**Status:** Not started
**Design doc:** `docs/superpowers/specs/2026-08-30-scenery-ini-driven-design.md`

## Goal

Make the scenery screen reflect `scenery_packs.ini` exactly: one table row per ini entry in file order (Rank = 1-based ini position), on-disk folders not listed in the ini appended at the end as unranked rows, no user reordering. This fixes the current bugs where (a) a `SCENERY_PACK_DISABLED` line silently truncates the parsed list (entries after it, including `*GLOBAL_AIRPORTS*`, are dropped), and (b) disabled-at-ini entries are invisible.

Introduce `SceneryEntry` (ini item + optional on-disk package + rank) with a derived `SceneryEntryStatus`; rework `SceneryManager` to `Manager<SceneryEntry>`; rewire the FX panel accordingly.

**Out of scope:** rewriting the ini (rank ▲/▼ buttons stay non-functional), official enable/disable via `SCENERY_PACK_DISABLED` flag flipping (legacy folder-move stays, existing FIXMEs kept), `SceneryOrganizer` changes.

## File Map

| Action | File | Purpose |
|---|---|---|
| Create | `xpman-api/src/test/java/com/ogerardin/xplane/test/file/SceneryPackIniItemTest.java` | unit tests: classification, disabled flag, equality, resolveFolder |
| Modify | `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/SceneryPackIniItem.java` | `disabled` flag, `of(String, boolean)`, abstract `resolveFolder` |
| Modify | `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/PathSceneryPackIniItem.java` | ctors, `callSuper = true`, `resolveFolder` |
| Modify | `xpman-api/src/main/java/com/ogerardin/xplane/file/data/scenery/TokenSceneryPackIniItem.java` | ctors, `callSuper = true`, Global Airports constants, `resolveFolder` |
| Modify | `xpman-api/src/main/java/com/ogerardin/xplane/scenery/GlobalAirportsSceneryPackage.java` | use moved `GLOBAL_AIRPORTS_FOLDER` constant |
| Create | `xpman-api/src/test/resources/scenery_packs_disabled.ini` | fixture: junk lines + DISABLED entries mid-file |
| Create | `xpman-api/src/test/java/com/ogerardin/xplane/test/petitparser/SceneryIniFileDisabledParserTest.java` | ordered regression test (truncation bug) |
| Create | `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntryStatus.java` | status enum |
| Create | `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntry.java` | entry model with null-safe accessors |
| Create | `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryEntryTest.java` | unit tests |
| Modify | `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryManager.java` | ini-driven `Manager<SceneryEntry>` |
| Create | `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryManagerTest.java` | integration test (`@EnableOnLocalXPlane`) |
| Rename+Modify | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/UiScenery.java` → `UiSceneryEntry.java` | wraps `SceneryEntry`, null-safe SpEL guards |
| Create | `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/cell_factory/SceneryStatusCellFactory.java` | IN_INI → "Yes", IN_INI_DISABLED → "No", else blank |
| Modify | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/SceneryController.java` | drop `SortedList`, new generics, null-safe scenery class |
| Modify | `xpman-fx/src/main/resources/fxml/panels/scenery.fxml` | "Status" column replaces "Enabled?", `sceneryClassName`, `sortable="false"` |
| Modify | `xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/wizard/Page2Controller.java` | null-safe rank comparator |

Existing tests that must stay green throughout: `SceneryIniFile11ParserTest`, `SceneryIniFile12ParserTest`, `SceneryPacksIniTest` (asserts spec version "1000" + `hasItem`).

Key correctness points:

- **Parser order matters**: `SCENERY_PACK ` is a prefix of `SCENERY_PACK_DISABLED `, so the DISABLED alternative must be tried **first** in the `or()` chain.
- **`callSuper = true`**: the base class gains the `disabled` field; subclass `@EqualsAndHashCode`/`@ToString` must include it or the parser regression test will not catch flag mismatches.
- **`resolvedFolders`**: keyed by the ini-resolved path (`baseFolder.resolve(item path)`; ini paths are relative to the X-Plane base folder, **not** to `Custom Scenery`), so leftover detection works.
- **Two enabled notions** (kept separate on purpose): `SceneryPackage.enabled` = located in an authorized base (`Custom Scenery` or `Global Scenery`) — drives status + wizard filter + menu guards; manager's private `isEnabled(pkg)` = located in `Custom Scenery` — drives the enable/disable `IllegalOperation` checks (so disabling Global Airports on XP12 still throws "already disabled", per design).
- **Accepted edge**: XP12 Global Airports shows status IN_INI ("Yes"); its "Disable" menu item is visible but throws `IllegalOperation` when clicked (error dialog). Ini-disabled entries located in `Custom Scenery` show "Enable/Disable" items that may throw similarly. Proper ini-flag toggling is the existing FIXME, out of scope.

---

## Task 1: `disabled` flag on ini items

**Test first.** Create `xpman-api/src/test/java/com/ogerardin/xplane/test/file/SceneryPackIniItemTest.java`:

```java
package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryPackIniItemTest {

    @Test
    void of_shouldClassifyPathAndToken() {
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA"),
                is(new PathSceneryPackIniItem(Path.of("Custom Scenery/KSEA"))));
        assertThat(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*"),
                is(new TokenSceneryPackIniItem("*GLOBAL_AIRPORTS*")));
    }

    @Test
    void of_withDisabledFlag_shouldSetDisabled() {
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA", true),
                is(new PathSceneryPackIniItem(Path.of("Custom Scenery/KSEA"), true)));
        assertThat(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*", false),
                is(new TokenSceneryPackIniItem("*GLOBAL_AIRPORTS*")));
    }

    @Test
    void disabled_shouldParticipateInEquality() {
        assertNotEquals(SceneryPackIniItem.of("Custom Scenery/KSEA", false),
                SceneryPackIniItem.of("Custom Scenery/KSEA", true));
    }
}
```

Run: `mvn test -pl xpman-api -Dtest=SceneryPackIniItemTest` → must fail to compile (no two-arg ctors, no `of(String, boolean)`).

**Implement.** Replace `SceneryPackIniItem.java`:

```java
package com.ogerardin.xplane.file.data.scenery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Paths;

/** An entry of the scenery_packs.ini file: either a scenery folder path or a special token. */
@Getter
@ToString
@EqualsAndHashCode
public abstract sealed class SceneryPackIniItem permits PathSceneryPackIniItem, TokenSceneryPackIniItem {

    /** Whether the entry is disabled in the ini (SCENERY_PACK_DISABLED instead of SCENERY_PACK). */
    private final boolean disabled;

    protected SceneryPackIniItem(boolean disabled) {
        this.disabled = disabled;
    }

    public static SceneryPackIniItem of(String folderOrToken) {
        return of(folderOrToken, false);
    }

    public static SceneryPackIniItem of(String folderOrToken, boolean disabled) {
        if (folderOrToken.matches("^\\*.+\\*$")) {
            return new TokenSceneryPackIniItem(folderOrToken, disabled);
        }
        return new PathSceneryPackIniItem(Paths.get(folderOrToken), disabled);
    }
}
```

`PathSceneryPackIniItem.java`:

```java
package com.ogerardin.xplane.file.data.scenery;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public non-sealed class PathSceneryPackIniItem extends SceneryPackIniItem {

    private final Path folder;

    public PathSceneryPackIniItem(Path folder) {
        this(folder, false);
    }

    public PathSceneryPackIniItem(Path folder, boolean disabled) {
        super(disabled);
        this.folder = folder;
    }
}
```

`TokenSceneryPackIniItem.java` (same shape):

```java
package com.ogerardin.xplane.file.data.scenery;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public non-sealed class TokenSceneryPackIniItem extends SceneryPackIniItem {

    private final String token;

    public TokenSceneryPackIniItem(String token) {
        this(token, false);
    }

    public TokenSceneryPackIniItem(String token, boolean disabled) {
        super(disabled);
        this.token = token;
    }
}
```

Run `mvn test -pl xpman-api -Dtest=SceneryPackIniItemTest` (green), then `mvn test -pl xpman-api` (existing parser tests still green).

Commit: `feat(scenery): add disabled flag to scenery_packs.ini items`

---

## Task 2: `resolveFolder` + Global Airports constants

**Test first.** Add to `SceneryPackIniItemTest`:

```java
@Test
void resolveFolder_shouldResolvePathItemAgainstBaseFolder() {
    var item = new PathSceneryPackIniItem(Path.of("Custom Scenery/KSEA"));
    assertThat(item.resolveFolder(Path.of("/xplane"), Path.of("/xplane/Global Scenery/Global Airports")),
            is(Path.of("/xplane/Custom Scenery/KSEA")));
}

@Test
void resolveFolder_shouldResolveGlobalAirportsToken() {
    var item = new TokenSceneryPackIniItem(TokenSceneryPackIniItem.GLOBAL_AIRPORTS_MARKER);
    assertThat(item.resolveFolder(Path.of("/xplane"), Path.of("/xplane/Global Scenery/Global Airports")),
            is(Path.of("/xplane/Global Scenery/Global Airports")));
}

@Test
void resolveFolder_shouldReturnNullForUnknownToken() {
    var item = new TokenSceneryPackIniItem("*FOO*");
    assertNull(item.resolveFolder(Path.of("/xplane"), Path.of("/xplane/Global Scenery/Global Airports")));
}
```

(imports: `nullValue`/`assertNull` as needed)

**Implement.** Add to `SceneryPackIniItem`:

```java
import java.nio.file.Path;

/**
 * The folder designated by this entry, or null if it cannot be determined (unknown token).
 */
public abstract Path resolveFolder(Path baseFolder, Path globalAirportsFolder);
```

Add to `TokenSceneryPackIniItem`:

```java
import java.nio.file.Path;

/** The token designating the global airports entry in scenery_packs.ini. */
public static final String GLOBAL_AIRPORTS_MARKER = "*GLOBAL_AIRPORTS*";

/** The name of the Global Airports folder, resolved according to the X-Plane version. */
public static final String GLOBAL_AIRPORTS_FOLDER = "Global Airports";

@Override
public Path resolveFolder(Path baseFolder, Path globalAirportsFolder) {
    return GLOBAL_AIRPORTS_MARKER.equals(token) ? globalAirportsFolder : null;
}
```

Add to `PathSceneryPackIniItem`:

```java
@Override
public Path resolveFolder(Path baseFolder, Path globalAirportsFolder) {
    return baseFolder.resolve(folder);
}
```

Refactor `GlobalAirportsSceneryPackage.java` (drop its own marker constant — verified unreferenced elsewhere):

```java
package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.NonNull;

import java.nio.file.Path;

public class GlobalAirportsSceneryPackage extends SceneryPackage {

    public GlobalAirportsSceneryPackage(@NonNull Path folder) throws InstantiationException {
        super(folder);
        IntrospectionHelper.require(folder.getFileName().toString().equals(TokenSceneryPackIniItem.GLOBAL_AIRPORTS_FOLDER));
    }
}
```

Run `mvn test -pl xpman-api`. Commit: `feat(scenery): resolve ini items to actual folders`

---

## Task 3: parser — DISABLED support + junk-line tolerance (regression fix)

**Test first.** Create `xpman-api/src/test/resources/scenery_packs_disabled.ini`:

```ini
I
1000 Version
SCENERY

# comment lines must be ignored
SCENERY_PACK Custom Scenery/A/
SCENERY_PACK_DISABLED Custom Scenery/B/

SCENERY_PACK Custom Scenery/C/
SCENERY_PACK_DISABLED Custom Scenery/D/
SCENERY_PACK *GLOBAL_AIRPORTS*
```

Create `xpman-api/src/test/java/com/ogerardin/xplane/test/petitparser/SceneryIniFileDisabledParserTest.java`:

```java
package com.ogerardin.xplane.test.petitparser;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import com.ogerardin.xplane.file.petitparser.SceneryPacksIniParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.petitparser.parser.Parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryIniFileDisabledParserTest extends ParserTest<SceneryPackIniData> {

    @Test
    void testCanParseSceneryIniWithDisabledEntries() throws IOException, URISyntaxException {
        String fileContents = getResourceAsString("/scenery_packs_disabled.ini");
        Parser parser = new SceneryPacksIniParser().getParser();
        SceneryPackIniData result = runParser(fileContents, parser, false);

        // regression: entries after a SCENERY_PACK_DISABLED line used to be silently dropped
        assertThat(result.getItems(), contains(
                new PathSceneryPackIniItem(Path.of("Custom Scenery/A")),
                new PathSceneryPackIniItem(Path.of("Custom Scenery/B"), true),
                new PathSceneryPackIniItem(Path.of("Custom Scenery/C")),
                new PathSceneryPackIniItem(Path.of("Custom Scenery/D"), true),
                new TokenSceneryPackIniItem("*GLOBAL_AIRPORTS*")
        ));
    }
}
```

Run: `mvn test -pl xpman-api -Dtest=SceneryIniFileDisabledParserTest` → fails (list truncated after the DISABLED line).

**Implement.** In `SceneryPacksIniParser.java`: keep `XPlaneFile()` unchanged; update the grammar javadoc to

```
 * SceneryPacksIniFile = Header("SCENERY") SceneryPack*
 * SceneryPack = DisabledSceneryPack | EnabledSceneryPack | JunkLine
 * DisabledSceneryPack = "SCENERY_PACK_DISABLED " folderNameOrToken Newline
 * EnabledSceneryPack = "SCENERY_PACK " folderNameOrToken Newline
 * JunkLine = Newline | (nonNewline+ Newline)   -- ignored
```

and replace `SceneryPacks()` / `SceneryPack()` and add the three helpers:

```java
Parser SceneryPacks() {
    return SceneryPack().star()
            .map((List<SceneryPackIniItem> input) -> {
                final SceneryPackIniData.SceneryPackList items = new SceneryPackIniData.SceneryPackList();
                input.stream().filter(Objects::nonNull).forEach(items::add);
                return items;
            })
            ;
}

Parser SceneryPack() {
    return DisabledSceneryPack()
            .or(EnabledSceneryPack())
            .or(JunkLine());
}

Parser DisabledSceneryPack() {
    return of("SCENERY_PACK_DISABLED ")
            .seq(FolderNameOrToken())
            .seq(Newline())
            .map((List<Object> input) -> SceneryPackIniItem.of((String) input.get(1), true))
            ;
}

Parser EnabledSceneryPack() {
    return of("SCENERY_PACK ")
            .seq(FolderNameOrToken())
            .seq(Newline())
            .map((List<Object> input) -> SceneryPackIniItem.of((String) input.get(1)))
            ;
}

/** Any unrecognized line (comments, blank lines); maps to null so it can be filtered out. */
Parser<SceneryPackIniItem> JunkLine() {
    return Newline().map(ignored -> (SceneryPackIniItem) null)
            .or(noneOf("\r\n").plus().seq(Newline()).map(ignored -> (SceneryPackIniItem) null))
            ;
}
```

Add `import java.util.Objects;`. Note: DISABLED must come **first** in the `or()` chain since `"SCENERY_PACK "` is a prefix of `"SCENERY_PACK_DISABLED "`.

Run `mvn test -pl xpman-api` (new test green, 11/12/v5 tests green). Commit: `fix(scenery): parse SCENERY_PACK_DISABLED lines without truncating the list`

---

## Task 4: `SceneryEntryStatus` + `SceneryEntry`

**Test first.** Create `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryEntryTest.java`:

```java
package com.ogerardin.xplane.test.scenery;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryEntryTest {

    private static SceneryPackage pkg(String path, boolean enabled) {
        var pkg = new SceneryPackage(Path.of(path));
        pkg.setEnabled(enabled);
        return pkg;
    }

    @Test
    void notListedEntryShouldBeEnabledWithoutRank() {
        var entry = SceneryEntry.notListed(pkg("/xplane/Custom Scenery/Foo", true));
        assertEquals(SceneryEntryStatus.NOT_LISTED, entry.getStatus());
        assertTrue(entry.isEnabled());
        assertNull(entry.getRank());
        assertNull(entry.getIniItem());
    }

    @Test
    void listedEnabledEntryShouldBeInIni() {
        var entry = SceneryEntry.inIni(SceneryPackIniItem.of("Custom Scenery/Foo"),
                pkg("/xplane/Custom Scenery/Foo", true), 1);
        assertEquals(SceneryEntryStatus.IN_INI, entry.getStatus());
        assertTrue(entry.isEnabled());
        assertEquals(1, entry.getRank());
    }

    @Test
    void disabledInIniEntryShouldBeDisabled() {
        var entry = SceneryEntry.inIni(SceneryPackIniItem.of("Custom Scenery/Foo", true),
                pkg("/xplane/Custom Scenery/Foo", true), 2);
        assertEquals(SceneryEntryStatus.IN_INI_DISABLED, entry.getStatus());
        assertFalse(entry.isEnabled());
    }

    @Test
    void packageInDisabledFolderShouldBeDisabled() {
        // legacy enable/disable: ini says enabled but the package lives in the disabled folder
        var entry = SceneryEntry.inIni(SceneryPackIniItem.of("Custom Scenery/Foo"),
                pkg("/xplane/Custom Scenery (disabled)/Foo", false), 1);
        assertEquals(SceneryEntryStatus.IN_INI_DISABLED, entry.getStatus());
    }

    @Test
    void unresolvedPathEntryShouldBeFolderMissing() {
        var entry = SceneryEntry.unresolved(SceneryPackIniItem.of("Custom Scenery/Gone"), 3);
        assertEquals(SceneryEntryStatus.FOLDER_MISSING, entry.getStatus());
        assertFalse(entry.isEnabled());
        assertEquals("Custom Scenery/Gone", entry.getName());
    }

    @Test
    void unresolvedTokenEntryShouldBeToken() {
        var entry = SceneryEntry.unresolved(new TokenSceneryPackIniItem("*GLOBAL_AIRPORTS*"), 1);
        assertEquals(SceneryEntryStatus.TOKEN, entry.getStatus());
        assertEquals("*GLOBAL_AIRPORTS*", entry.getName());
    }

    @Test
    void unresolvedEntryAccessorsShouldBeNullSafe() {
        var entry = SceneryEntry.unresolved(SceneryPackIniItem.of("Custom Scenery/Gone"), 1);
        assertNull(entry.getVersion());
        assertNull(entry.getIconUrl());
        assertFalse(entry.getHasAirport());
        assertFalse(entry.isLibrary());
        assertEquals(0, entry.getTileCount());
        assertEquals(0, entry.getObjCount());
        assertTrue(entry.getLinks().isEmpty());
    }
}
```

Run: fails to compile (`SceneryEntry` doesn't exist).

**Implement.** Create `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntryStatus.java`:

```java
package com.ogerardin.xplane.scenery;

/** Status of a scenery with respect to the scenery_packs.ini file. */
public enum SceneryEntryStatus {
    /** Listed and enabled in scenery_packs.ini. */
    IN_INI,
    /** Listed but disabled in scenery_packs.ini. */
    IN_INI_DISABLED,
    /** Listed in scenery_packs.ini but the folder does not exist on disk. */
    FOLDER_MISSING,
    /** Special token entry (e.g. *GLOBAL_AIRPORTS*) that could not be resolved to a folder. */
    TOKEN,
    /** Folder exists on disk but is not listed in scenery_packs.ini. */
    NOT_LISTED
}
```

Create `xpman-api/src/main/java/com/ogerardin/xplane/scenery/SceneryEntry.java`:

```java
package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import lombok.Value;

import java.net.URL;
import java.util.Map;

/**
 * One row of the scenery list: an optional scenery_packs.ini item, an optional on-disk
 * {@link SceneryPackage}, and the rank (1-based position in the ini file, null if not listed).
 */
@Value
public class SceneryEntry {

    SceneryPackIniItem iniItem;
    SceneryPackage sceneryPackage;
    Integer rank;

    public static SceneryEntry inIni(SceneryPackIniItem iniItem, SceneryPackage sceneryPackage, int rank) {
        return new SceneryEntry(iniItem, sceneryPackage, rank);
    }

    public static SceneryEntry unresolved(SceneryPackIniItem iniItem, int rank) {
        return new SceneryEntry(iniItem, null, rank);
    }

    public static SceneryEntry notListed(SceneryPackage sceneryPackage) {
        return new SceneryEntry(null, sceneryPackage, null);
    }

    public SceneryEntryStatus getStatus() {
        if (iniItem == null) {
            return SceneryEntryStatus.NOT_LISTED;
        }
        if (sceneryPackage == null) {
            return iniItem instanceof TokenSceneryPackIniItem
                    ? SceneryEntryStatus.TOKEN
                    : SceneryEntryStatus.FOLDER_MISSING;
        }
        return iniItem.isDisabled() || !sceneryPackage.isEnabled()
                ? SceneryEntryStatus.IN_INI_DISABLED
                : SceneryEntryStatus.IN_INI;
    }

    /** Whether X-Plane will load this scenery: listed and enabled in the ini, or not listed. */
    public boolean isEnabled() {
        var status = getStatus();
        return status == SceneryEntryStatus.IN_INI || status == SceneryEntryStatus.NOT_LISTED;
    }

    // null-safe accessors so that unresolved entries (no on-disk package) can still be displayed

    public String getName() {
        return sceneryPackage != null ? sceneryPackage.getName() : iniItemText();
    }

    public String getVersion() {
        return sceneryPackage != null ? sceneryPackage.getVersion() : null;
    }

    public boolean getHasAirport() {
        return sceneryPackage != null && sceneryPackage.getHasAirport();
    }

    public boolean isLibrary() {
        return sceneryPackage != null && sceneryPackage.isLibrary();
    }

    public int getTileCount() {
        return sceneryPackage != null ? sceneryPackage.getTileCount() : 0;
    }

    public int getObjCount() {
        return sceneryPackage != null ? sceneryPackage.getObjCount() : 0;
    }

    public URL getIconUrl() {
        return sceneryPackage != null ? sceneryPackage.getIconUrl() : null;
    }

    public Map<String, URL> getLinks() {
        return sceneryPackage != null ? sceneryPackage.getLinks() : Map.of();
    }

    private String iniItemText() {
        if (iniItem instanceof PathSceneryPackIniItem pathItem) {
            return pathItem.getFolder().toString();
        }
        if (iniItem instanceof TokenSceneryPackIniItem tokenItem) {
            return tokenItem.getToken();
        }
        return "?";
    }
}
```

Run `mvn test -pl xpman-api -Dtest=SceneryEntryTest` (green). Commit: `feat(scenery): introduce SceneryEntry with ini-derived status`

---

## Task 5: ini-driven `SceneryManager`

**Test first.** Create `xpman-api/src/test/java/com/ogerardin/xplane/test/scenery/SceneryManagerTest.java`:

```java
package com.ogerardin.xplane.test.scenery;

import com.ogerardin.test.util.EnableOnLocalXPlane;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import com.ogerardin.xplane.scenery.SceneryManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(TimingExtension.class)
@EnableOnLocalXPlane
class SceneryManagerTest {

    @Test
    void entriesShouldFollowIniOrder() throws InvalidConfig {
        XPlane xPlane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());
        SceneryManager manager = xPlane.getSceneryManager();
        manager.loadPackages();

        List<SceneryEntry> entries = manager.getSceneryEntries();
        assertFalse(entries.isEmpty());

        // ini-listed entries come first, in file order, with consecutive 1-based ranks
        List<SceneryEntry> iniEntries = entries.stream()
                .filter(e -> e.getIniItem() != null)
                .toList();
        assertFalse(iniEntries.isEmpty());
        for (int i = 0; i < iniEntries.size(); i++) {
            assertEquals(i + 1, iniEntries.get(i).getRank(), "rank of ini entry #" + (i + 1));
        }

        // entries not listed in the ini come last, with null rank
        List<SceneryEntry> notListed = entries.stream()
                .filter(e -> e.getIniItem() == null)
                .toList();
        assertThat(notListed, everyItem(hasProperty("rank", nullValue())));

        // *GLOBAL_AIRPORTS* resolves to the Global Airports folder when it exists on disk
        List<SceneryEntry> tokenEntries = iniEntries.stream()
                .filter(e -> e.getIniItem() instanceof TokenSceneryPackIniItem)
                .toList();
        if (!tokenEntries.isEmpty() && Files.isDirectory(xPlane.getPaths().globalAirports())) {
            assertThat(tokenEntries, everyItem(hasProperty("status", is(SceneryEntryStatus.IN_INI))));
        }

        entries.forEach(e -> log.info("{}: {} [{}] pkg={}", e.getRank(), e.getName(), e.getStatus(),
                e.getSceneryPackage() != null));
    }

    @Test
    void sceneryPackagesShouldExcludeUnresolvedEntries() throws InvalidConfig {
        XPlane xPlane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());
        var packages = xPlane.getSceneryManager().getSceneryPackages();
        assertFalse(packages.isEmpty());
        assertThat(packages, everyItem(notNullValue()));
    }
}
```

Run: fails to compile (`getSceneryEntries` doesn't exist).

**Implement.** First `grep -r SCENERY_PACKAGE_COMPARATOR` to confirm it is only the definition + a commented-out usage, then rewrite `SceneryManager.java` (keep package/imports sensible; enable/disable/moveToTrash/install bodies unchanged except the `setEnabled` line in `moveSceneryPackage`):

```java
package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.exception.IllegalOperation;
import com.ogerardin.xplane.file.SceneryPacksIniFile;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADING;

/**
 * The scenery library: one {@link SceneryEntry} per scenery_packs.ini entry (in file order,
 * {@link SceneryEntry#getRank() rank} = 1-based position in the file), followed by on-disk
 * folders not listed in the ini.
 */
@Slf4j
@ToString
public class SceneryManager extends Manager<SceneryEntry> implements InstallTarget {

    @NonNull @Getter
    private final Path sceneryFolder;

    @NonNull @Getter
    private final Path disabledSceneryFolder;

    @NonNull @Getter
    private final Path globalSceneryFolder;

    public SceneryManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.sceneryFolder = xPlane.getPaths().customScenery();
        this.disabledSceneryFolder = xPlane.getPaths().disabledCustomScenery();
        this.globalSceneryFolder = xPlane.getPaths().globalScenery();
    }

    /**
     * Returns an unmodifiable list of all scenery entries (one per scenery_packs.ini entry in
     * file order, then on-disk folders not listed in the ini). Triggers a synchronous load if needed.
     */
    public List<SceneryEntry> getSceneryEntries() {
        if (items == null) {
            loadPackages();
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns an unmodifiable list of the {@link SceneryPackage} instances of all entries that
     * have one, in list order.
     */
    public List<SceneryPackage> getSceneryPackages() {
        return getSceneryEntries().stream()
                .map(SceneryEntry::getSceneryPackage)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Trigger an asynchronous reload of the scenery entries.
     */
    public void reload() {
        AsyncHelper.runAsync(this::loadPackages);
    }

    @SneakyThrows
    public void loadPackages() {
        log.info("Loading scenery entries...");
        fireEvent(ManagerEvent.<SceneryEntry>builder().type(LOADING).source(this).build());

        items = buildEntries();

        log.info("Loaded {} scenery entries", items.size());
        fireEvent(ManagerEvent.<SceneryEntry>builder().type(LOADED).source(this).items(items).build());
    }

    private List<SceneryEntry> buildEntries() {
        // on-disk packages by folder (global scenery first, then custom scenery, each sorted by name)
        Map<Path, SceneryPackage> packagesByFolder = new LinkedHashMap<>();
        collectPackages(globalSceneryFolder, packagesByFolder);
        collectPackages(sceneryFolder, packagesByFolder);

        SceneryPacksIniFile iniFile = getSceneryPacksIniFile();
        List<SceneryPackIniItem> iniItems = iniFile == null ? List.of() : iniFile.getSceneryPackList();

        List<SceneryEntry> entries = new ArrayList<>();
        Set<Path> resolvedFolders = new HashSet<>();

        for (int i = 0; i < iniItems.size(); i++) {
            SceneryPackIniItem item = iniItems.get(i);
            int rank = i + 1;
            Path resolvedFolder = item.resolveFolder(xPlane.getBaseFolder(), xPlane.getPaths().globalAirports());
            if (resolvedFolder != null) {
                resolvedFolders.add(resolvedFolder);
            }
            SceneryPackage sceneryPackage = resolvedFolder == null ? null : packagesByFolder.get(resolvedFolder);
            if (sceneryPackage == null && item instanceof PathSceneryPackIniItem pathItem) {
                // fallback: legacy enable/disable moves the folder into the disabled scenery
                // folder without touching the ini; look for it there
                Path disabledFolder = disabledSceneryFolder.resolve(pathItem.getFolder().getFileName());
                if (Files.isDirectory(disabledFolder)) {
                    sceneryPackage = createSceneryPackage(disabledFolder);
                }
            }
            if (sceneryPackage != null) {
                sceneryPackage.setRank(rank);
                entries.add(SceneryEntry.inIni(item, sceneryPackage, rank));
            } else {
                entries.add(SceneryEntry.unresolved(item, rank));
            }
        }

        // append on-disk packages that are not listed in the ini
        packagesByFolder.forEach((folder, sceneryPackage) -> {
            if (!resolvedFolders.contains(folder)) {
                entries.add(SceneryEntry.notListed(sceneryPackage));
            }
        });

        return entries;
    }

    private SceneryPacksIniFile getSceneryPacksIniFile() {
        final Path sceneryPacksIniFile = sceneryFolder.resolve("scenery_packs.ini");
        return Files.exists(sceneryPacksIniFile) ?
                new SceneryPacksIniFile(sceneryPacksIniFile) : null;
    }

    @SneakyThrows
    private void collectPackages(Path sceneryFolder, Map<Path, SceneryPackage> packagesByFolder) {
        if (!Files.isDirectory(sceneryFolder)) {
            return;
        }
        try (var stream = Files.list(sceneryFolder)) {
            stream.filter(Files::isDirectory)
                    .sorted()
                    .map(this::createSceneryPackage)
                    .forEach(pkg -> packagesByFolder.put(pkg.getFolder(), pkg));
        }
    }

    @SneakyThrows
    private SceneryPackage createSceneryPackage(Path folder) {
        SceneryPackage sceneryPackage = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);
        sceneryPackage.setEnabled(isLocatedInAuthorizedBase(sceneryPackage.getFolder()));
        return sceneryPackage;
    }

    private boolean isLocatedInAuthorizedBase(Path folder) {
        return folder.startsWith(sceneryFolder) || folder.startsWith(globalSceneryFolder);
    }

    private boolean isEnabled(SceneryPackage sceneryPackage) {
        return sceneryPackage.getFolder().startsWith(sceneryFolder);
    }

    @SneakyThrows
    public void enableSceneryPackage(SceneryPackage sceneryPackage) {
        if (isEnabled(sceneryPackage)) {
            throw new IllegalOperation("SceneryPackage already enabled");
        }
        moveSceneryPackage(sceneryPackage, sceneryFolder);
    }

    @SneakyThrows
    public void disableSceneryPackage(SceneryPackage sceneryPackage) {
        if (!isEnabled(sceneryPackage)) {
            throw new IllegalOperation("SceneryPackage already disabled");
        }
        moveSceneryPackage(sceneryPackage, disabledSceneryFolder);
    }

    @SneakyThrows
    private void moveSceneryPackage(SceneryPackage sceneryPackage, Path targetFolder) {
        // move the scenary folder
        Path sourceFolder = sceneryPackage.getFolder();
        // ...to the target folder, keeping the original folder name
        Files.createDirectories(targetFolder);
        Path target = targetFolder.resolve(sourceFolder.getFileName());
        Files.move(sourceFolder, target);

        // update scenery package
        sceneryPackage.setFolder(target);
        sceneryPackage.setEnabled(isLocatedInAuthorizedBase(sceneryPackage.getFolder()));
    }

    @SneakyThrows
    public void moveSceneryPackageToTrash(SceneryPackage sceneryPackage) {
        var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
        fileUtils.moveToTrash(sceneryPackage.getFolder().toFile());
    }

    @Override
    public void install(Archive archive, ProgressListener progressListener) throws IOException {
        archive.extract(getSceneryFolder(), progressListener);
        reload();
    }

}
```

Notes:
- ini paths resolve against `xPlane.getBaseFolder()` (ini entries like `Custom Scenery/X` are relative to the base folder).
- `setFolder` is package-private — `moveSceneryPackage` stays inside the same package, unchanged.
- `UsageCategory` keeps working (`getSceneryFolder()`/`getDisabledSceneryFolder()` kept); `HomeController` counts entries instead of packages (accepted); `Page2Controller`/`InstallType` keep compiling.
- **xpman-fx will NOT compile after this task** (`UiScenery`/`SceneryController` still reference the old API). That is expected; tasks are committed per-module (`mvn test -pl xpman-api`), and full-build verification happens in Tasks 6–7. Do NOT run a root build between Tasks 5 and 6.

Run `mvn test -pl xpman-api`. Commit (api only): `feat(scenery): ini-driven SceneryManager`

---

## Task 6: FX — ini-driven scenery screen

No fx test suite exists (UI); verification is compilation.

**6a.** `git mv xpman-fx/src/main/java/com/ogerardin/xpman/panels/scenery/UiScenery.java UiSceneryEntry.java`, then replace its content:

```java
package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.util.jfx.menu.annotation.*;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;

@SuppressWarnings({"unused"})
@Data
public class UiSceneryEntry {

    @Delegate
    private final SceneryEntry sceneryEntry;

    private final XPlane xPlane;

    private final SceneryClass sceneryClass;

    public String getSceneryClassName() {
        return sceneryClass.name();
    }

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    @EnabledIf("sceneryPackage != null")
    public void reveal() {
        Platforms.getCurrent().reveal(getSceneryPackage().getFolder());
    }

    // FIXME the official method for disabling a scenerypack is described in https://www.x-plane.com/kb/prioritization-scenery-packs/
    // it involves changing SCENERY_PACK to SCENERY_PACK_DISABLED, and not moving it to another folder
    @Label("'Enable Scenery Package'")
    @EnabledIf("sceneryPackage != null && ! sceneryPackage.enabled")
    @OnSuccess("reload()")
    public void enable() {
        xPlane.getSceneryManager().enableSceneryPackage(getSceneryPackage());
    }

    // FIXME the official method for disabling a scenerypack is described in https://www.x-plane.com/kb/prioritization-scenery-packs/
    // it involves changing SCENERY_PACK to SCENERY_PACK_DISABLED, and not moving it to another folder
    @Label("'Disable Scenery Package'")
    @EnabledIf("sceneryPackage != null && sceneryPackage.enabled")
    @Confirm("'The entire folder \"' + xPlane.baseFolder.relativize(sceneryPackage.folder) " +
            "+ '\" will be moved to \"' + xPlane.baseFolder.relativize(xPlane.sceneryManager.disabledSceneryFolder) " +
            "+ '\" \n" +
            "\n" +
            "Press OK to continue.'")
    @OnSuccess("reload()")
    public void disable() {
        xPlane.getSceneryManager().disableSceneryPackage(getSceneryPackage());
    }

    @Label("'Move to Trash'")
    @EnabledIf("sceneryPackage != null")
    @Confirm("'The entire folder \"' + xPlane.baseFolder.relativize(sceneryPackage.folder) " +
            "+ '\" will be moved to the trash.\n" +
            "\n" +
            "Press OK to continue.'")
    @OnSuccess("reload()")
    public void moveToTrash() {
        xPlane.getSceneryManager().moveSceneryPackageToTrash(getSceneryPackage());
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @EnabledIf("sceneryPackage != null")
    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return getSceneryPackage().inspect();
    }

}
```

(All SpEL guards null-check `sceneryPackage`, which resolves via the `@Delegate`d `getSceneryPackage()`; `links` resolves via the null-safe delegated `getLinks()`; the `@Confirm` expressions are unreachable for null packages because the `@EnabledIf` guards hide those items.)

**6b.** Create `xpman-fx/src/main/java/com/ogerardin/xpman/util/jfx/cell_factory/SceneryStatusCellFactory.java`:

```java
package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Factory for a {@code TableCell<S, SceneryEntryStatus>} that renders IN_INI as "Yes",
 * IN_INI_DISABLED as "No", and any other status as empty.
 */
public class SceneryStatusCellFactory<S> implements TableCellFactory<S, SceneryEntryStatus> {

    @Override
    public TableCell<S, SceneryEntryStatus> call(TableColumn<S, SceneryEntryStatus> param) {
        return new TableCell<S, SceneryEntryStatus>() {
            @Override
            protected void updateItem(SceneryEntryStatus value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(switch (value) {
                        case IN_INI -> "Yes";
                        case IN_INI_DISABLED -> "No";
                        default -> null;
                    });
                }
            }
        };
    }

}
```

**6c.** Update `SceneryController.java` — change generics and the items wiring; everything else (reload, installScenery, organize, openSceneryClasses, openSceneryPacksIni) unchanged:

```java
@FXML
private TableView<UiSceneryEntry> sceneryTable;

@FXML
private TableColumn<UiSceneryEntry, Integer> rankColumn;

private final IntrospectingContextMenuTableRowFactory<UiSceneryEntry> rowFactory =
        new IntrospectingContextMenuTableRowFactory<>(this);

private ManagerItemsObservableList<SceneryEntry, UiSceneryEntry> uiItems;
```

```java
@FXML
public void initialize() {
    // add context menu to table rows
    sceneryTable.setRowFactory(rowFactory);

    sceneryTable.setPlaceholder(new EmptyState("fth-map", "No scenery to show"));

    // the table shows the entries in manager order (ini order, then unlisted folders);
    // the rank column sort (ascending, nulls last) reflects that same order
    rankColumn.setSortType(TableColumn.SortType.ASCENDING);
    rankColumn.setComparator(Comparator.nullsLast(Comparator.naturalOrder()));
    sceneryTable.getSortOrder().setAll(Collections.singletonList(rankColumn));

    // set tooltip for "rank" column
    TableViewUtil.setColumnHeaderTooltip(sceneryTable, rankColumn, "The rank of this scenery in scenery_packs.ini");

    // disable the toolbar if we don't have a current X-Plane instance
    toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));

    uiItems = new ManagerItemsObservableList<>(
            this.xPlaneProperty,
            XPlane::getSceneryManager,
            sceneryEntry -> new UiSceneryEntry(
                    sceneryEntry,
                    xPlaneProperty.get(),
                    sceneryClassOf(sceneryEntry))
    );
    sceneryTable.setItems(uiItems);
}

/** Null-safe: unresolved entries (no on-disk package) fall back to the "Other" scenery class. */
private SceneryClass sceneryClassOf(SceneryEntry sceneryEntry) {
    return Optional.ofNullable(sceneryEntry.getSceneryPackage())
            .map(sceneryOrganizer::sceneryClass)
            .orElse(OtherSceneryClass.INSTANCE);
}
```

Imports: add `com.ogerardin.xplane.scenery.SceneryEntry`, `com.ogerardin.xpman.scenery_organizer.OtherSceneryClass`, `com.ogerardin.xpman.scenery_organizer.SceneryClass`, `java.util.Optional`; remove `javafx.collections.transformation.SortedList`, `com.ogerardin.xplane.scenery.SceneryPackage`, and the now-unused `java.util.List`. The `SortedList` wrapper lines are deleted (`sceneryTable.setItems(uiItems)` directly).

**6d.** Update `scenery.fxml`:

- Replace the "Enabled?" column (lines 68–75) with:

```xml
<TableColumn editable="false" prefWidth="70.0" sortable="false" text="Status">
    <cellValueFactory>
        <PropertyValueFactory property="status"/>
    </cellValueFactory>
    <cellFactory>
        <SceneryStatusCellFactory/>
    </cellFactory>
</TableColumn>
```

- Class column: `expression="sceneryClass.name"` → `expression="sceneryClassName"`, and add `sortable="false"`.
- Add `sortable="false"` to the Name, Version, "Has airport?", "Library?", "Tile count", "Obj count" columns (icon column already has it; rank column stays sortable).

**6e.** Update `Page2Controller.java` line 30 (rank may now be null for not-listed packages):

```java
.sorted(Comparator.comparing(SceneryPackage::getRank, Comparator.nullsLast(Comparator.naturalOrder())))
```

Verify: `mvn -B -DskipTests package -pl xpman-fx -am` (compiles api + fx).

Commit: `feat(scenery): ini-driven scenery screen`

---

## Task 7: full verification

```bash
mvn test                     # all module tests (xpman-api; fx has none)
mvn -B -DskipTests clean package   # full build as CI does (no installers by default)
```

Manual QA (optional, needs `--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls` VM flag per AGENTS.md): open the Scenery panel, confirm rows follow scenery_packs.ini order, disabled entries show "No", missing folders/token rows render with blank package columns, unlisted folders appear at the bottom without rank.

---

## Risks / notes

- `SceneryPackage.setFolder` is package-private (`@Setter(AccessLevel.PACKAGE)`) — `moveSceneryPackage` must stay in `com.ogerardin.xplane.scenery`.
- XP11 ini can list Global Airports both as path entry and token entry resolving to the same folder: both rows reference the same package, last rank wins on the package. Accepted.
- Folders in `Custom Scenery (disabled)` that are *not* referenced by the ini are no longer displayed (they were previously listed as disabled). Accepted consequence of the ini-driven design.
- The wizard now also sees XP12 Global Airports (`enabled` = true because Global Scenery is an authorized base). Accepted per design.
