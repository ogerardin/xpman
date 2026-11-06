package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.petitparser.AcfFileParser;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Inspection that flags an aircraft archive whose ACF spec version does not match the target X-Plane version:
 * an aircraft built for a newer X-Plane is an error (it will not work), an older one a warning.
 */
@RequiredArgsConstructor
public class AircraftSpecVersionInspection implements Inspection<Archive> {

    private final XPlane xPlane;
    private final AcfFileParser parser = new AcfFileParser();

    private record AcfSpec(Path acf, int spec) {}

    @Override
    public InspectionResult inspect(Archive archive) {
        // shortcut: reads full ACF entries and parses headers synchronously on the FX thread; background it if fleet packs feel slow
        // house philosophy: missing probe skips the check
        if (xPlane.getVersion() == null) {
            return InspectionResult.empty();
        }
        int xPlaneSpec = Integer.parseInt(xPlane.getMajorVersion().specString());
        List<AcfSpec> acfSpecs = archive.getPaths().stream()
                .filter(path -> path.getFileName().toString().endsWith(".acf"))
                .map(path -> parseSpec(archive, path))
                .flatMap(Optional::stream)
                .toList();
        if (acfSpecs.isEmpty()) {
            return InspectionResult.empty();
        }
        String details = acfSpecs.stream()
                .map(s -> "%s: spec %d".formatted(s.acf().getFileName(), s.spec()))
                .collect(Collectors.joining("\n"));
        int maxSpec = acfSpecs.stream().mapToInt(AcfSpec::spec).max().orElseThrow();
        int minSpec = acfSpecs.stream().mapToInt(AcfSpec::spec).min().orElseThrow();
        if (maxSpec > xPlaneSpec) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("Aircraft is intended for X-Plane %d and will not work in X-Plane %d"
                            .formatted(maxSpec / 100, xPlane.getMajorVersion().getMajor()))
                    .details(details)
                    .build());
        }
        if (minSpec < xPlaneSpec) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.WARN)
                    .message("Aircraft is intended for X-Plane %d; it should still work in X-Plane %d, but a native version may be available"
                            .formatted(minSpec / 100, xPlane.getMajorVersion().getMajor()))
                    .details(details)
                    .build());
        }
        return InspectionResult.empty();
    }

    private Optional<AcfSpec> parseSpec(Archive archive, Path acf) {
        try {
            int spec = Integer.parseInt(parser.parseSpecVersion(archive.getAsText(acf)));
            return Optional.of(new AcfSpec(acf, spec));
        } catch (Exception _) {
            return Optional.empty(); // unreadable or unparseable ACF: skip
        }
    }
}
