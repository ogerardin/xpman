package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.URL;
import java.util.Map;

/**
 * One row of the scenery list: an optional scenery_packs.ini item, an optional on-disk
 * {@link SceneryPackage}, and the rank (1-based position in the ini file, null if not listed).
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class SceneryEntry {

    SceneryPackIniItem iniItem;
    SceneryPackage sceneryPackage;
    @Setter
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
            return sceneryPackage.isSystem() ? SceneryEntryStatus.SYSTEM : SceneryEntryStatus.NOT_LISTED;
        }
        if (sceneryPackage == null) {
            return SceneryEntryStatus.FOLDER_MISSING;
        }
        return iniItem.isDisabled()
                ? SceneryEntryStatus.IN_INI_DISABLED
                : SceneryEntryStatus.IN_INI;
    }

    /** Whether X-Plane will load this scenery: listed and enabled in the ini, or not listed. */
    public boolean isEnabled() {
        var status = getStatus();
        return status == SceneryEntryStatus.IN_INI || status == SceneryEntryStatus.NOT_LISTED;
    }

    /** Whether this entry is a special token (e.g., *GLOBAL_AIRPORTS*). */
    public boolean isToken() {
        return iniItem != null && iniItem.isToken();
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

    public Integer getTileCount() {
        return sceneryPackage != null ? sceneryPackage.getTileCount() : null;
    }

    public Integer getObjCount() {
        return sceneryPackage != null ? sceneryPackage.getObjCount() : null;
    }

    public URL getIconUrl() {
        return sceneryPackage != null ? sceneryPackage.getIconUrl() : null;
    }

    public Map<String, URL> getLinks() {
        return sceneryPackage != null ? sceneryPackage.getLinks() : Map.of();
    }

    private String iniItemText() {
        return iniItem != null ? iniItem.getIniValue() : "?";
    }
}
