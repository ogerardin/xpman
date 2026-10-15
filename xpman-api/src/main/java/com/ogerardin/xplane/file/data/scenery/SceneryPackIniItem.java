package com.ogerardin.xplane.file.data.scenery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * One line of scenery_packs.ini: the value written after the {@code SCENERY_PACK} /
 * {@code SCENERY_PACK_DISABLED} directive (a folder path or a special token), and whether
 * the entry is disabled. Pure syntax: folder resolution is domain policy and lives on
 * {@code SceneryEntry.resolveFolder} / {@code SceneryIniToken}.
 */
@Getter
@ToString
@EqualsAndHashCode
public abstract sealed class SceneryPackIniItem permits PathSceneryPackIniItem, TokenSceneryPackIniItem {

    /** Shape of a special token value (e.g. {@code *GLOBAL_AIRPORTS*}); the only token knowledge of the file format. */
    private static final String TOKEN_PATTERN = "^\\*.+\\*$";

    /** Whether the entry is disabled in the ini (SCENERY_PACK_DISABLED instead of SCENERY_PACK). */
    @Setter
    private boolean disabled;

    protected SceneryPackIniItem(boolean disabled) {
        this.disabled = disabled;
    }

    public static SceneryPackIniItem of(String folderOrToken) {
        return of(folderOrToken, false);
    }

    public static SceneryPackIniItem of(String folderOrToken, boolean disabled) {
        if (folderOrToken.matches(TOKEN_PATTERN)) {
            return new TokenSceneryPackIniItem(folderOrToken, disabled);
        }
        // X-Plane writes folder values with trailing separators; drop them so parse→write round-trips stably
        return new PathSceneryPackIniItem(stripTrailingSeparator(folderOrToken), disabled);
    }

    private static String stripTrailingSeparator(String value) {
        while (value.endsWith("/") || value.endsWith("\\")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /** The value written after the scenery-pack directive. */
    public abstract String getIniValue();

    /** Whether the value is a special token (e.g. *GLOBAL_AIRPORTS*) rather than a folder path. */
    public abstract boolean isToken();
}
