package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link NavDataSet} in the ARINC424 format.
 */
@ToString(callSuper = true, includeFieldNames = false)
public class Arinc424DataSet extends NavDataSet {

    /**
     * True when this is the sim-wide override: X-Plane reads {@code earth_424.dat} at sim
     * start and then loads no other navdata text file, so every lower layer is ignored.
     * False for the legacy {@code FAACIFP18} set, which current X-Plane versions no longer read.
     */
    private final boolean overriding;

    public Arinc424DataSet(String name, String description, XPlane xPlane, Path folder, boolean overriding,
                           String filename) {
        super(name, description, xPlane, folder, filename);
        this.overriding = overriding;
    }

    @Override
    public boolean isOverriding() {
        return overriding;
    }

    @Override
    protected NavDataItem createFile(Path file) {
        return new Arinc424NavDataFile(this, file);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Only the FAA dataset is recognizable as partial: it is the one free source X-Plane's
     * users install, and it covers the US only. A commercial 424 master from any other
     * publisher says nothing about its coverage in its header, so we make no claim rather
     * than guess.</p>
     */
    @Override
    protected Optional<InspectionMessage> coverageMessage() {
        return getChildren().stream()
                .filter(NavDataItem::getExists)
                .map(NavDataItem::getDatasetName)
                .filter(Objects::nonNull)
                .filter(dataset -> dataset.startsWith("FAACIFP"))
                .findFirst()
                .map(dataset -> InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .message("US-only coverage (" + dataset + "): navaids, airways and procedures outside the "
                                + "United States are not in this dataset, and X-Plane ignores all other navdata "
                                + "layers once it is installed.")
                        .build());
    }
}