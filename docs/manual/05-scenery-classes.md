# Scenery Classes

Scenery classes have two functions:
- categorize the scenery packs 
- provide the ranking for automatic ordering when "Organize scenery_packs.ini" is activated. 

Each scenery pack is assigned a class in two steps: 
1) the name-matching (regex) rules are tried first, from top to bottom, and the first pattern that matches the pack's folder name wins; 
2) if none matches, the class is deduced from the pack's contents: 
   - **Airport** when the pack contains an `Earth nav data/apt.dat` file, 
   - otherwise **Library** when it contains a `library.txt` file. 
3) Packs that match nothing end up in **Other**.

When **Organize scenery_packs.ini** is activated, packs are sorted by the position of their class in the list: the higher a class sits in the table, the earlier its packs appear in `scenery_packs.ini`.

Open the Scenery classes window via the **Scenery classes...** button in the Scenery panel toolbar.

## Rules Table

| Column | Description |
|--------|-------------|
| **Priority** | Row number (determines rank) |
| **Name** | Class name (editable) |
| **RegEx** | Regular expression matched against folder names (editable, validated) |

### Toolbar

- **+** — Add a new regex rule.
- **-** — Delete the selected rule. Built-in rows are italic and cannot be deleted.
- **▲ / ▼** — Move the selected rule up or down.
- **Restore defaults** — Reset all rules to their defaults.

## Built-in Classes

These file-based classes are always present and cannot be edited:

- **Airport** — Pack contains `Earth nav data/apt.dat` (airport definition data).
- **Library** — Pack contains `library.txt` (library manifest).
- **Other** — Fallback for packages not matching any other class.

## Default Regex Classes

XPman ships with these sensible default classes and name-matching rules:

- **X-Plane Landmark**: matches default X-Plane landmarks 
- **Global Airports**: matches the special "Global Airports" folder
- **Overlay scenery**: matches overlays such as World2XPlane, ...
- **Mesh scenery**: matches mesh scenery such as Ortho4XP, ...

The default classes and their rank are based on best practices for ordering the scenery_pack.ini and are intended to 
yield a correct order in most of the configurations.
  

## Workflow

1. Edit the class rules (add, edit, reorder, delete).
2. Click **Apply** — rules are persisted to `~/.xpman` and the Scenery panel reloads.
3. Optionally go to the Scenery panel and click **Organize scenery_pack.ini** to re-sort by the new class ranks.
4. Review the result and click **Save**.

## Buttons

- **Apply** — Persist changes to `~/.xpman` and reload the Scenery panel.
- **Close** — Close the window.
