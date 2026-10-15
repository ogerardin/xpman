package com.ogerardin.xplane.file.data.scenery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * One line of scenery_packs.ini: the value written after the {@code SCENERY_PACK} /
 * {@code SCENERY_PACK_DISABLED} directive (a folder path or a special token), and whether
 * the entry is disabled. Pure syntax: folder resolution is domain policy and lives on
 * {@code SceneryEntry.resolveFolder}.
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SceneryPackIniItem {

    /** The token designating the global airports entry in scenery_packs.ini. */
    public static final String GLOBAL_AIRPORTS_MARKER = "*GLOBAL_AIRPORTS*";

    /** The value written after the scenery-pack directive. */
    private final String iniValue;

    /** Whether the entry is disabled in the ini (SCENERY_PACK_DISABLED instead of SCENERY_PACK). */
    @Setter
    private boolean disabled;

    public static SceneryPackIniItem of(String folderOrToken) {
        return of(folderOrToken, false);
    }

    public static SceneryPackIniItem of(String folderOrToken, boolean disabled) {
        // X-Plane writes folder values with a trailing slash; drop it so parse→write round-trips stably
        return new SceneryPackIniItem(stripTrailingSeparator(folderOrToken), disabled);
    }

    private static String stripTrailingSeparator(String value) {
        while (value.endsWith("/") || value.endsWith("\\")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /** Whether the value is a special token (e.g., *GLOBAL_AIRPORTS*) rather than a folder path. */
    public boolean isToken() {
        return iniValue.matches("^\\*.+\\*$");
    }
}
