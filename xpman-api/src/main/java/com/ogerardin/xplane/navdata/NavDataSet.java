package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A nav data folder containing a set of {@link NavDataItem}s
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class NavDataSet extends XPlaneObject implements Inspectable, NavDataItem {

    private final String name;

    @ToString.Exclude
    private final String description;

    private final Path folder;

    @Override
    public Path getPath() {
        return getFolder();
    }

    @EqualsAndHashCode.Exclude
    private List<NavDataItem> files = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    private List<NavDataItem> extraChildren = new ArrayList<>();

    protected NavDataSet(String name, String description, XPlane xPlane, Path folder, String... fileNames) {
        super(xPlane);
        this.name = name;
        this.description = description;
        this.folder = folder;
        this.files = Arrays.stream(fileNames)
//                .map(folder::resolve)
                .map(Paths::get)
                .<NavDataItem>map(this::createFile)
                .toList();
    }

    /**
     * Creates the file item for one of this set's data files. Overridden by sets whose
     * format needs its own file implementation.
     */
    protected NavDataItem createFile(Path file) {
        return new NavDataFile(this, file);
    }

    /**
     * Adds an extra file to this data set after construction.
     * Used for version-specific files (e.g. XP12 airspaces/atc data).
     */
    protected void addExtraFile(NavDataItem file) {
        files = new ArrayList<>(files);
        files.add(file);
    }

    /**
     * Adds an extra child item (not a file) to this data set after construction.
     * Used for summary nodes like CIFPSummary that don't map to individual files.
     */
    protected void addExtraChild(NavDataItem child) {
        extraChildren = new ArrayList<>(extraChildren);
        extraChildren.add(child);
    }


    /**
     * True when the presence of this data set makes X-Plane ignore every lower-priority
     * layer. Only meaningful when {@link #getExists()} is true.
     *
     * <p>The sim-wide ARINC 424 override is the only such layer: X-Plane does not load any
     * other navdata text file once it has read {@code earth_424.dat}.</p>
     */
    public boolean isOverriding() {
        return false;
    }

    /**
     * An extra message describing the geographic coverage of this data set, when the format
     * makes it detectable. Overridden by formats that carry coverage in their file header.
     */
    protected Optional<InspectionMessage> coverageMessage() {
        return Optional.empty();
    }

    /**
     * Inspects the data files of this set: reports missing files as errors, and warns
     * when the existing files carry inconsistent AIRAC cycles.
     */
    @Override
    public InspectionResult inspect() {
        List<InspectionMessage> messages = getChildren().stream()
                .filter(item -> !item.getExists())
                .map(item -> InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .message("File not found: " + item.getName())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        List<String> cycles = getChildren().stream()
                .filter(NavDataItem::getExists)
                .map(NavDataItem::getAiracCycle)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        Severity severity = Severity.INFO;
        String message = switch (cycles.size()) {
            case 1 -> "OK — cycle " + cycles.get(0);
            // files are present but carry no header we can read a cycle from, e.g. ARINC 424
            case 0 -> getExists() ? "Present (AIRAC cycle unknown)" : "No data present";
            default -> {
                severity = Severity.WARN;
                yield "Mixed AIRAC cycles: " + String.join(", ", cycles);
            }
        };
        messages.add(InspectionMessage.builder().severity(severity).message(message).build());
        coverageMessage().ifPresent(messages::add);

        return InspectionResult.of(messages);
    }


    @Override
    public List<? extends NavDataItem> getChildren() {
        if (extraChildren.isEmpty()) {
            return files;
        }
        List<NavDataItem> all = new ArrayList<>(files);
        all.addAll(extraChildren);
        return all;
    }

    @Override
    public Boolean getExists() {
        return files.stream().map(NavDataItem::getPath).anyMatch(Files::exists);
    }
}
