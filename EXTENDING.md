# Extending XPman

XPman discovers custom aircraft and scenery classes at runtime. No registry or
service-loader entry is required. Classes must be under `com.ogerardin.xplane`
so ClassGraph can find them.

## Custom Aircraft Classes

Create a subclass under `com.ogerardin.xplane.aircraft.custom` with a
constructor accepting `(XPlane, AcfFile)`. The constructor must reject every
aircraft it does not handle by calling `IntrospectionHelper.require(...)`.

```java
package com.ogerardin.xplane.aircraft.custom;

public class MyAircraft extends Aircraft {
    public MyAircraft(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile);
        IntrospectionHelper.require(getStudio().equals("My Studio"));
    }

    @Override
    public String getVersion() {
        return readVersionFromMyAircraftFiles();
    }
}
```

Useful methods to override include:

| Method | Purpose |
|---|---|
| `getName()` | Custom display name |
| `getVersion()` | Read the installed version |
| `getLatestVersion()` | Provide update information |
| `getLinks()` | Add project or download links |
| `inspect()` | Add aircraft-specific checks |

Use a condition that uniquely identifies the aircraft. Existing classes use
ACF metadata such as `getStudio()`, `getAcfName()`, and `getNotes()`, or files
present in the aircraft directory.

## Custom Scenery Classes

Create a subclass under `com.ogerardin.xplane.scenery.custom` with a
constructor accepting `Path folder`. Reject folders that do not represent the
custom scenery type.

```java
package com.ogerardin.xplane.scenery.custom;

public class MyScenery extends SceneryPackage {
    public MyScenery(Path folder) throws InstantiationException {
        super(folder);
        IntrospectionHelper.require(
                folder.getFileName().toString().equals("My Scenery"));
    }

    @Override
    public String getVersion() {
        return Files.readString(getFolder().resolve("version.txt")).trim();
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
| `inspect()` | Add scenery-specific checks |

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
| `file` | Installed executable path relative to `Resources/tools` |
| `version` | Explicit version string |
| `installChecker` | Optional content check, for example `{ "string": "1.2.3" }` |
| `items` | Platform or version-specific variants |

Platform and X-Plane version variants are filtered automatically. A tool is
shown as installed when its declared `file` exists under the X-Plane
`Resources/tools` directory and its optional install checker passes.

Supported downloads are `.zip` archives and macOS `.dmg` files. ZIP files are
extracted into `Resources/tools`; DMG files must contain the application at the
volume root.

## Discovery Rules

Class-based extensions are found using ClassGraph. XPman tries discovered
subclass constructors with the object being inspected. The first constructor
that succeeds is used; therefore each `IntrospectionHelper.require(...)`
condition must be specific and fail for unrelated objects.

Tool manifests are discovered from the classpath resource directory `/tools`.
