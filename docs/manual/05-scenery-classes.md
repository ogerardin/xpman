# Scenery classes

Scenery classes control how **Organize scenery_pack.ini** sorts packages. Packages are ordered by their class rank: classes higher in the list appear earlier in `scenery_packs.ini`.

Open the class editor with **Scenery classes** in the Scenery panel.

## How classification works

XPman checks custom folder-name rules from top to bottom. The first matching rule determines the package's class. If no rule matches, XPman classifies a package as **Airport** when it contains `Earth nav data/apt.dat`, **Library** when it contains `library.txt`, or **Other** when neither file is present.

Built-in defaults include classes for X-Plane landmarks, Global Airports, overlay scenery, and mesh scenery. Their order is intended to work for most installations.

## Edit rules and ordering

Use **+** to add a rule, edit its name or regular expression, and use **▲** or **▼** to set its priority. Built-in classes cannot be deleted. Choose **Restore defaults** to discard your custom rules and return to XPman's defaults.

Choose **Apply** to save class rules. This does not reorder scenery or save changes to `scenery_packs.ini`. To use the new rules:

1. Return to the Scenery panel and choose **Organize scenery_pack.ini**.
2. Review the proposed order.
3. Choose **Save** to write it to `scenery_packs.ini`.
