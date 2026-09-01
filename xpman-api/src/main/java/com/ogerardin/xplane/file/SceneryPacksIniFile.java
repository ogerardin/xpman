package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData.SceneryPackList;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.file.petitparser.SceneryPacksIniParser;
import lombok.ToString;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;

/**
 * Represents a parsed scenery_packs.ini file containing the prioritized list of scenery packages.
 *
 * <p>The scenery_packs.ini file controls the load order of scenery packages in X-Plane.
 * Packages listed earlier have higher priority. This file is typically located in the
 * {@code Custom Scenery} directory.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Path iniPath = Path.of("Custom Scenery/scenery_packs.ini");
 * SceneryPacksIniFile iniFile = new SceneryPacksIniFile(iniPath);
 *
 * // Get ordered list of scenery packs
 * SceneryPackList packs = iniFile.getSceneryPackList();
 * for (SceneryPackIniItem pack : packs) {
 *     System.out.println(pack);
 * }
 * }</pre>
 *
 * <h2>File Format Reference</h2>
 * <p>See <a href="https://developer.x-plane.com/2019/05/custom-scenery-order-in-11-33/">
 * Custom Scenery Order</a> for details on scenery priority.</p>
 *
 * @author Olivier G.
 * @see SceneryPackIniData
 * @see SceneryPacksIniParser
 */
@ToString(onlyExplicitlyIncluded = true)
public class SceneryPacksIniFile extends XPlaneFile<SceneryPackIniData> {

    /**
     * Create a SceneryPacksIniFile from a file path.
     *
     * @param file the path to the scenery_packs.ini file
     */
    public SceneryPacksIniFile(Path file) {
        super(file, new SceneryPacksIniParser());
    }

    /**
     * Get the ordered list of scenery packs.
     *
     * @return the scenery pack list in priority order
     */
    public SceneryPackList getSceneryPackList() {
        return getData().getItems();
    }

    /** Writes the supplied ordered entries without changing the parsed file data. */
    public void write(Path target, List<SceneryPackIniItem> items) throws IOException {
        var header = getData().getHeader();
        try (var writer = Files.newBufferedWriter(target)) {
            writer.write(header.getOrigin() + "\n");
            writer.write(header.getSpecVersion() + " Version\n");
            writer.write(header.getFileType() + "\n\n");
            for (var item : items) {
                writer.write(item.isDisabled() ? "SCENERY_PACK_DISABLED " : "SCENERY_PACK ");
                writer.write(item.getIniValue() + "\n");
            }
        }
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }

    /**
     * Returns the INI file specification version.
     *
     * @return version string
     */
    @Override
    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }
}
