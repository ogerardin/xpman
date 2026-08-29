package com.ogerardin.xplane.scenery;

/** Status of a scenery with respect to the scenery_packs.ini file. */
public enum SceneryEntryStatus {
    /** Listed and enabled in scenery_packs.ini. */
    IN_INI,
    /** Listed but disabled in scenery_packs.ini. */
    IN_INI_DISABLED,
    /** Listed in scenery_packs.ini but the folder does not exist on disk. */
    FOLDER_MISSING,
    /** Special token entry (e.g. *GLOBAL_AIRPORTS*) that could not be resolved to a folder. */
    TOKEN,
    /** Folder exists on disk but is not listed in scenery_packs.ini. */
    NOT_LISTED
}
