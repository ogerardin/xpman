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
     * <p>ARINC 424 carries no XPNAV AIRAC cycle marker; the status line falls back to
     * reporting the file as present with an unknown cycle.</p>
     */
    @Override
    public String getAiracCycle() {
        return null;
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