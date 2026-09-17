# Extending XPman

XPman discovers custom aircraft, scenery, plugin, and FlyWithLua script classes
at runtime. No registry or service-loader entry is required. Classes must be
under `com.ogerardin.xplane` so ClassGraph can find them.

## Custom Aircraft Classes

Create a subclass under `com.ogerardin.xplane.aircraft.custom` with a
constructor accepting `(XPlane, AcfFile)`. The constructor must reject every
aircraft it does not handle by calling `require(...)`.

```java
package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
public class MyAircraft extends Aircraft {
    public MyAircraft(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile);
        require(getStudio().equals("My Studio"));
    }

    @Override
    public String getVersion() {
        return readVersionFromMyAircraftFiles();
    }
}
```

Use the 3-arg `super(xPlane, acfFile, "Display Name")` constructor to force a
specific display name instead of deriving it from the ACF file.

Use a condition that uniquely identifies the aircraft. Existing classes use
ACF metadata such as `getStudio()`, `getAcfName()`, and `getNotes()`, or files
present in the aircraft directory.

Useful methods to override include:

| Method | Purpose |
|---|---|
| `getName()` | Custom display name |
| `getVersion()` | Read the installed version |
| `getLatestVersion()` | Provide update information |
| `getLinks()` | Add project or download links |
| `getManuals()` | Provide PDF manual paths |
| `inspect()` | Add aircraft-specific checks |

## Custom Scenery Classes

Create a subclass under `com.ogerardin.xplane.scenery.custom` with a
constructor accepting `Path folder`. Reject folders that do not represent the
custom scenery type.

```java
package com.ogerardin.xplane.scenery.custom;

import com.ogerardin.xplane.scenery.SceneryPackage;

import java.nio.file.Path;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
public class MyScenery extends SceneryPackage {
    public MyScenery(Path folder) throws InstantiationException {
        super(folder);
        require(folder.getFileName().toString().equals("My Scenery"));
    }

    @Override
    public String getVersion() {
        return readVersionFromFolder();
    }
}
```

Useful methods to override include:

| Method | Purpose |
|---|---|
| `getName()` | Custom display name |
| `getVersion()` | Read the installed version |
| `getIconUrl()` | Provide a scenery icon |
| `getLinks()` | Add project or download links |
| `getHasAirport()` | Whether scenery contains an airport |
| `isLibrary()` | Whether scenery is a library |
| `inspect()` | Add scenery-specific checks |

## Custom Plugin Classes

Create a subclass under `com.ogerardin.xplane.plugins.custom` with a
constructor accepting `(XPlane, Path xplFile)`. The constructor must reject
every plugin it does not handle by calling `require(...)`.

Extend `Plugin` directly, or extend `XPlaneOrgPlugin` if the plugin is hosted
on x-plane.org (it provides automatic latest-version checking from the
download page).

```java
package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.Plugin;

import java.nio.file.Path;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
public class MyPlugin extends Plugin {
    public MyPlugin(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "My Plugin", "A short description");
        require(xplFile.getFileName().toString().equals("MyPlugin.xpl"));
    }

    @Override
    public String getVersion() {
        return readVersionFromPluginFiles();
    }
}
```

For x-plane.org hosted plugins, extend `XPlaneOrgPlugin` instead:

```java
public class MyXPlaneOrgPlugin extends XPlaneOrgPlugin {
    public MyXPlaneOrgPlugin(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "My Plugin", "Description",
              "https://forums.x-plane.org/index.php?/files/file/XXXXX/");
        require(matchesMyPlugin(xplFile));
    }
}
```

Useful methods to override include:

| Method | Purpose |
|---|---|
| `getVersion()` | Read the installed version |
| `getLatestVersion()` | Provide update information |
| `getLinks()` | Add project or download links |
| `getManuals()` | Provide PDF manual paths |
| `inspect()` | Add plugin-specific checks |
| `getSystem()` | Return `true` for system plugins (protected from deletion) |
| `delete()` | Custom deletion logic |

## Custom FlyWithLua Script Classes

Create a subclass under `com.ogerardin.xplane.plugins.custom.lua` with a
constructor accepting `(XPlane, Path luaFile)`. The constructor must reject
every script it does not handle by calling `require(...)`.

```java
package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.XPlane;

import java.nio.file.Path;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
public class MyScript extends FlyWithLuaScript {
    public MyScript(XPlane xPlane, Path luaFile) throws InstantiationException {
        super(xPlane, luaFile);
        require(luaFile.getFileName().toString().equals("my_script.lua"));
    }

    @Override
    public String getVersion() {
        return readVersionFromLuaFile();
    }
}
```

Script metadata (name, description, version) is automatically parsed from
Lua header comments by `LuaHeaderParser`. Override `getVersion()` only when
the header doesn't contain version info or it's stored differently.

Useful methods to override include:

| Method | Purpose |
|---|---|
| `getName()` | Custom display name |
| `getVersion()` | Read the installed version |
| `getManuals()` | Provide PDF manual paths |
| `delete()` | Custom deletion logic (e.g. remove associated data folders) |
| `inspect()` | Add script-specific checks |

## Custom Installable Types

`InstallableType` controls how archives are auto-detected and installed. Unlike
domain object subclasses, installable types use a **no-arg constructor** and
are discovered via `recognizes()`.

Implement `InstallableType` anywhere under `com.ogerardin.xplane`:

```java
package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

public class MyInstallableType implements InstallableType {

    @Override
    public String description() {
        return "my custom content";
    }

    @Override
    public boolean recognizes(Archive archive) {
        return archive.getPaths().stream()
            .anyMatch(path -> path.getFileName().toString().equals("marker.txt"));
    }

    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        return InspectionResult.empty();
    }

    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress)
            throws InstallationException {
        // extract archive contents to the appropriate location
    }
}
```

Installable types can also be `static` inner classes co-located with the domain
class they install (e.g. `FlyWithLuaScript.FlyWithLuaScriptInstallableType`).

**Deepest subclass wins.** When multiple types match an archive, the most
specialized (deepest in the inheritance chain) is selected. This means a
subtype can override its parent's `recognizes()` to handle a more specific case
without changing the parent.

| Method | Purpose |
|---|---|
| `description()` | Human-readable label shown in the UI |
| `recognizes(Archive)` | Check if archive contains matching content |
| `preconditions(XPlane, Archive)` | Check preconditions before install |
| `install(XPlane, Archive, ProgressListener)` | Perform installation |

## Declaring A New Tool

Add a JSON manifest under:

```text
xpman-api/src/main/resources/tools/
```

The filename, without `.json`, becomes the tool ID. No Java code is needed.

```json
{
  "name": "My Tool",
  "homepage": "https://example.com/my-tool",
  "description": "A tool for managing X-Plane data.",
  "icon": "fth-tool",
  "items": [
    {
      "platform": "MAC",
      "xplaneVersion": "12",
      "url": "https://example.com/my-tool.dmg",
      "file": "My Tool.app"
    },
    {
      "platform": "WINDOWS",
      "xplaneVersion": "12",
      "url": "https://example.com/my-tool.zip",
      "file": "My Tool\\My Tool.exe"
    }
  ]
}
```

Supported fields include:

| Field | Purpose |
|---|---|
| `name` | Display name |
| `homepage` | Project URL |
| `description` | Description shown in the Tools panel |
| `icon` | Icon-font literal, classpath resource, or HTTP URL |
| `platform` | `MAC`, `WINDOWS`, or `LINUX` |
| `xplaneVersion` | X-Plane major version, such as `11` or `12` |
| `url` | Download URL for a tool variant |
| `file` | Installed executable path relative to install directory |
| `installDir` | Custom install directory relative to X-Plane root; `null` = `Resources/tools`, `"."` = X-Plane root |
| `version` | Explicit version string |
| `installChecker` | Optional content check, for example `{ "string": "1.2.3" }` |
| `items` | Platform or version-specific variants |

Platform and X-Plane version variants are filtered automatically. A tool is
shown as installed when its declared `file` exists under the install directory
and its optional install checker passes.

Supported downloads are `.zip` archives and macOS `.dmg` files. ZIP files are
extracted into the install directory; DMG files must contain the application at
the volume root.

## Discovery Rules

Class-based extensions (aircraft, scenery, plugins, FlyWithLua scripts) are
found using ClassGraph. XPman tries discovered subclass constructors with the
object being inspected. The first constructor that succeeds is used; therefore
each `require(...)` condition must be specific and fail for unrelated objects.

Installable types use a different pattern: they are instantiated via a no-arg
constructor, asked `recognizes(archive)`, and the deepest matching subclass
wins.

Tool manifests are discovered from the classpath resource directory `/tools`.

## Skunkcrafts Updater Integration

Any addon (plugin, aircraft, or scenery) that implements `SkunkcraftsUpdatable`
can participate in the Skunkcrafts update protocol. The base classes `Plugin`,
`Aircraft`, and `SceneryPackage` already implement this interface.

An addon is Skunkcrafts-updatable when it contains a `skunkcrafts_updater.cfg`
file in its base folder. The configuration file uses pipe-delimited key|value
format:

```
zone|custom
module|https://example.com/addon-repo
name|My Addon
version|1.2.3
locked|false
disabled|false
liveries|true
```

| Field | Purpose |
|---|---|
| `module` | Base URL for the remote repository |
| `version` | Installed version |
| `locked` | If `true`, developer is uploading; updates temporarily unavailable |
| `disabled` | If `true`, skip this addon |
| `liveries` | If `false`, exclude `liveries/` folder from updates |

The remote repository must contain:
- `skunkcrafts_updater.cfg` - Remote version info
- `skunkcrafts_updater_whitelist.txt` - List of managed files with CRC32 checksums
- `skunkcrafts_updater_blacklist.txt` (optional) - Files to exclude from updates

Users can trigger updates via the context menu on any Skunkcrafts-updatable addon.
The update process downloads only changed files (differential update) and verifies
integrity using CRC32 checksums.
