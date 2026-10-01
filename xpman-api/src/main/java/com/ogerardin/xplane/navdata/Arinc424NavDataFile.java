package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.file.data.dat.Arinc424Header;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

/**
 * A nav data file in ARINC 424 format.
 *
 * <p>Overrides the header accessors to read the ARINC 424 header directly. The inherited
 * ones would parse the file as an X-Plane XPNAV dataset, which means reading the whole
 * thing into a String to get at the header — tens of megabytes for a single file.</p>
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Arinc424NavDataFile extends NavDataFile {

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Arinc424Header header = Arinc424Header.read(getFullPath());

    public Arinc424NavDataFile(NavDataSet navDataSet, Path file) {
        super(navDataSet, file);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The ARINC 424 header carries its own cycle designator ({@code VOLUME 2610}), which
     * is the same AIRAC numbering the XPNAV {@code data cycle} header uses — so it compares
     * directly against the cycles reported by the other layers.</p>
     */
    @Override
    public String getAiracCycle() {
        return getHeader() == null ? null : NavDataItem.normalizeCycle(getHeader().cycle());
    }

    @Override
    public String getMetadata() {
        return getHeader() == null ? null : getHeader().originator();
    }

    @Override
    public String getBuild() {
        return null;
    }

    @Override
    public String getDatasetName() {
        return getHeader() == null ? null : getHeader().datasetName();
    }
}