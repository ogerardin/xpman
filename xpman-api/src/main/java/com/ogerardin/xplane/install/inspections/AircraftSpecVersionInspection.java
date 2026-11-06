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

import static java.util.Comparator.comparingInt;

/**
 * Inspection that flags an aircraft archive whose ACF spec version does not match the target X-Plane version:
 * an aircraft built for a newer X-Plane is an error (it will not work), an older one a warning.
 */
@RequiredArgsConstructor
public class AircraftSpecVersionInspection implements Inspection<Archive> {

    private final XPlane xPlane;
    private final AcfFileParser parser = new AcfFileParser();

    private record AcfSpec(Path acf, String spec) {
        int specValue() {
            return Integer.parseInt(spec);
        }
    }

    /** Display heuristic: an ACF spec's leading digits are the target X-Plane major, e.g. "1100" -> "11"; authoritative for known specs only. */
    private static String majorOf(String spec) {
        return spec.substring(0, spec.length() - 2);
    }

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
                .map(s -> "%s: spec %s".formatted(s.acf().getFileName(), s.spec()))
                .collect(Collectors.joining("\n"));
        AcfSpec newest = acfSpecs.stream().max(comparingInt(AcfSpec::specValue)).orElseThrow();
        AcfSpec oldest = acfSpecs.stream().min(comparingInt(AcfSpec::specValue)).orElseThrow();
        if (newest.specValue() > xPlaneSpec) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("Aircraft is intended for X-Plane %s and will not work in X-Plane %d"
                            .formatted(majorOf(newest.spec()), xPlane.getMajorVersion().getMajor()))
                    .details(details)
                    .build());
        }
        if (oldest.specValue() < xPlaneSpec) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.WARN)
                    .message("Aircraft is intended for X-Plane %s; it should still work in X-Plane %d, but a native version may be available"
                            .formatted(majorOf(oldest.spec()), xPlane.getMajorVersion().getMajor()))
                    .details(details)
                    .build());
        }
        return InspectionResult.empty();
    }

    private Optional<AcfSpec> parseSpec(Archive archive, Path acf) {
        try {
            return Optional.of(new AcfSpec(acf, parser.parseSpecVersion(archive.getAsText(acf))));
        } catch (Exception _) {
            return Optional.empty(); // unreadable or unparseable ACF: skip
        }
    }
}
