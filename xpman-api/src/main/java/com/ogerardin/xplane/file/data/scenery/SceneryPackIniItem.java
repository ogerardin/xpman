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
