package com.ogerardin.xplane.file.data.scenery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** A scenery_packs.ini entry whose value is a folder path, as spelled in the ini. */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public non-sealed class PathSceneryPackIniItem extends SceneryPackIniItem {

    private final String folder;

    public PathSceneryPackIniItem(String folder) {
        this(folder, false);
    }

    public PathSceneryPackIniItem(String folder, boolean disabled) {
        super(disabled);
        this.folder = folder;
    }

    @Override
    public String getIniValue() {
        return folder;
    }

    @Override
    public boolean isToken() {
        return false;
    }
}
