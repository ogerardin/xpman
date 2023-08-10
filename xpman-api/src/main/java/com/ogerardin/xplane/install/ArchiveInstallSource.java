package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.inspections.CheckInstallTypeIdentified;
import com.ogerardin.xplane.install.inspections.CheckIsValidArchive;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.ZipArchive;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An {@link InstallSource} that is an archive (zip, etc.)
 */
@Slf4j
@Data
@RequiredArgsConstructor
public class ArchiveInstallSource implements InstallSource, Archive {

    @NonNull
    @Delegate
    private final Archive archive;

    @Getter(lazy = true)
    private final Set<InstallType> candidateTypes = computeCandidateTypes();

    private Set<InstallType> computeCandidateTypes() {
        try {
            return Arrays.stream(InstallType.values())
                    .filter(it -> it.test(archive))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Exception while computing candidate types", e);
            return Collections.emptySet();
        }
    }

    public Optional<InstallType> getInstallType() {
        return getCandidateTypes().size() == 1 ?
                Optional.ofNullable(getCandidateTypes().iterator().next())
                : Optional.empty();
    }


    private Inspection<Archive> getTypeSpecificInspections() {
        return getInstallType()
                .map(InstallType::additionalInspections)
                .orElse(Inspection.empty());
    }

    public static ArchiveInstallSource ofZip(Path file) {
        return new ArchiveInstallSource(new ZipArchive(file));
    }

    @Override
    public InspectionResult inspect() {
        return CheckIsValidArchive.INSTANCE.inspectable(archive)
            .and(CheckInstallTypeIdentified.INSTANCE.inspectable(this))
            .and(getTypeSpecificInspections().inspectable(archive))
            .inspect();
    }

    @SneakyThrows
    @Override
    public void install(XPlane xPlane, ProgressListener progressListener) {
        InstallType installType = getInstallType().orElseThrow(() -> new IllegalStateException("No install type identified"));
        InstallTarget target = installType.target(xPlane);
        target.install(archive, progressListener);
    }

}
