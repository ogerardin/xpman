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
 *
 * <p>X-Plane reads two different ARINC 424 files from {@code Custom Data}, and they are
 * not two names for one mechanism:</p>
 *
 * <ul>
 *   <li>{@code earth_424.dat} — the sim-wide override. It loads enroute airways and navaids
 *       and then no other navdata text file is read at all.</li>
 *   <li>{@code FAACIFP18} — the updated-approaches layer. It loads terminal data only,
 *       never enroute airways or navaids, and layers on top of the global layers per
 *       airport instead of replacing them.</li>
 * </ul>
 */
@ToString(callSuper = true, includeFieldNames = false)
public class Arinc424DataSet extends NavDataSet {

    /**
     * How X-Plane applies the file, which is what determines whether it suppresses the
     * lower layers and whether a matching global cycle is required.
     */
    public enum Role {
        /** {@code earth_424.dat}: overrides the whole global database, suppressing every other layer. */
        SIM_WIDE_OVERRIDE,
        /** {@code FAACIFP18}: overrides terminal data per airport, on top of the global layers. */
        FAA_APPROACHES
    }

    private final Role role;

    public Arinc424DataSet(String name, String description, XPlane xPlane, Path folder, Role role,
                           String filename) {
        super(name, description, xPlane, folder, filename);
        this.role = role;
    }

    @Override
    public boolean isOverriding() {
        return role == Role.SIM_WIDE_OVERRIDE;
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
                        .message("US-only coverage (" + dataset + "): approaches, terminal fixes and navaids "
                                + "outside the United States are not in this dataset.")
                        .build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Only the approaches layer has a documented cross-layer requirement: "for integrity
     * reasons, the cycle number of the FAA data must always match the cycle number of the
     * underlying layer", because terminal procedures reference waypoints outside the
     * terminal area. The sim-wide override has no such requirement — it replaces the global
     * database rather than composing with it — so it never warns here.</p>
     */
    @Override
    protected Optional<InspectionMessage> consistencyMessage() {
        if (role != Role.FAA_APPROACHES || !getExists().booleanValue()) {
            return Optional.empty();
        }
        NavDataManager navData = getXPlane().getNavDataManager();

        if (navData.getSimWideOverride().filter(NavDataSet::getExists).isPresent()) {
            return Optional.of(warn("This layer is not used: X-Plane reads no other navdata file while the "
                    + "sim-wide ARINC424 override is installed."));
        }

        Optional<String> mine = getConsistentCycle();
        Optional<String> underlying = navData.getEffectiveGlobalDataSet()
                .flatMap(NavDataSet::getConsistentCycle);
        if (mine.isEmpty() || underlying.isEmpty() || mine.get().equals(underlying.get())) {
            return Optional.empty();
        }
        return Optional.of(warn("AIRAC cycle " + underlying.get() + " does not match the cycle of the navdata "
                + "layer this one overrides. X-Plane requires both to be the same cycle, because terminal "
                + "procedures reference waypoints outside the terminal area; mixing cycles is not supported."));
    }

    private static InspectionMessage warn(String message) {
        return InspectionMessage.builder().severity(Severity.WARN).message(message).build();
    }
}
