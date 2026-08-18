package com.ogerardin.xplane.file.data.scenery;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

/**
 * Data model for parsed scenery_packs.ini files.
 *
 * <p>The scenery_packs.ini file contains a prioritized list of scenery packages.
 * Packages listed earlier in the file have higher priority.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * SceneryPackIniData data = iniFile.getData();
 * SceneryPackList items = data.getItems();
 * for (SceneryPackIniItem item : items) {
 *     System.out.println(item.getFolder());
 * }
 * }</pre>
 *
 * @author Olivier G.
 * @see com.ogerardin.xplane.file.SceneryPacksIniFile
 */
@Getter
@Setter
@ToString
public class SceneryPackIniData extends XPlaneFileData {

    /**
     * The ordered list of scenery packs.
     */
    final SceneryPackList items;

    /**
     * Create a SceneryPackIniData instance.
     *
     * @param header the file header
     * @param items the list of scenery packs
     */
    public SceneryPackIniData(Header header, SceneryPackList items) {
        super(header);
        this.items = items;
    }

    /**
     * List of scenery pack entries.
     */
    public static class SceneryPackList extends ArrayList<SceneryPackIniItem> {}
}
