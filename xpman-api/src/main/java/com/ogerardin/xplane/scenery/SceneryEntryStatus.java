package com.ogerardin.xplane.scenery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Status of a scenery with respect to the scenery_packs.ini file. */
@Getter
@RequiredArgsConstructor
public enum SceneryEntryStatus {
    /** Listed and enabled in scenery_packs.ini. */
    IN_INI("Enabled"),
    /** Listed but disabled in scenery_packs.ini. */
    IN_INI_DISABLED("Disabled"),
    /** Listed in scenery_packs.ini but the folder does not exist on disk. */
    FOLDER_MISSING("Folder missing"),
    /** Folder exists on disk but is not listed in scenery_packs.ini. */
    NOT_LISTED(null);

    private final String label;
}
