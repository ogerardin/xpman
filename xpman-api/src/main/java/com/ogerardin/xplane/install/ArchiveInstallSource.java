package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.inspections.CheckIsValidArchive;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.SevenZArchive;
import com.ogerardin.xplane.util.zip.ZipArchive;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * An {@link InstallSource} that is an archive (zip, etc.)
 */
@Slf4j
@Data
public class ArchiveInstallSource implements InstallSource, Archive {

    @NonNull
    @Delegate
    private final Archive archive;

    private final XPlane xPlane;

    @Getter(lazy = true)
    private final Optional<InstallableType> installableType = findInstallableType();

    private Optional<InstallableType> findInstallableType() {
        if (!archive.isValidArchive()) {
            log.debug("Not a valid archive, skipping install type detection");
            return Optional.empty();
        }
        
        try {
            List<Class<?>> allTypes = IntrospectionHelper.findAllSubclasses(InstallableType.class);
            log.debug("Found {} InstallableType implementations", allTypes.size());
            
            List<InstallableType> matches = allTypes.stream()
                .map(cls -> instantiate(cls))
                .filter(type -> type != null && type.recognizes(archive))
                .toList();
            
            log.debug("Found {} matching InstallableType(s)", matches.size());
            
            if (matches.isEmpty()) {
                return Optional.empty();
            }
            
            return matches.stream()
                .max(Comparator.comparingInt(this::getInheritanceDepth));
                
        } catch (Exception e) {
            log.error("Exception while finding installable type", e);
            return Optional.empty();
        }
    }
    
    private InstallableType instantiate(Class<?> cls) {
        try {
            // Try no-arg constructor first
            return (InstallableType) cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.debug("Could not instantiate {} with no-arg constructor", cls.getName());
            return null;
        }
    }
    
    private int getInheritanceDepth(Object obj) {
        int depth = 0;
        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) {
            depth++;
            cls = cls.getSuperclass();
        }
        return depth;
    }

    public static ArchiveInstallSource of(Path file) {
        return new ArchiveInstallSource(openArchive(file), null);
    }

    public static ArchiveInstallSource of(Path file, XPlane xPlane) {
        return new ArchiveInstallSource(openArchive(file), xPlane);
    }

    private static Archive openArchive(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".7z")) {
            return new SevenZArchive(file);
        }
        return new ZipArchive(file);
    }

    public static ArchiveInstallSource ofZip(Path file) {
        return of(file);
    }

    public static ArchiveInstallSource ofZip(Path file, XPlane xPlane) {
        return of(file, xPlane);
    }

    @Override
    public InspectionResult inspect() {
        InspectionResult result = CheckIsValidArchive.INSTANCE.inspect(archive);
        if (result.isNotAbort()) {
            result = result.append(inspectInstallableType());
        }
        return result;
    }
    
    private InspectionResult inspectInstallableType() {
        Optional<InstallableType> type = getInstallableType();
        
        if (type.isEmpty()) {
            return InspectionResult.of(
                com.ogerardin.xplane.inspection.InspectionMessage.builder()
                    .severity(com.ogerardin.xplane.inspection.Severity.ERROR)
                    .message("Archive type could not be identified. " +
                            "Either file is not an installable X-Plane add-on, or it is corrupt.")
                    .abort(true)
                    .build()
            );
        }
        
        InstallableType installableType = type.get();
        
        InspectionResult typeMessage = InspectionResult.of(
            com.ogerardin.xplane.inspection.InspectionMessage.builder()
                .severity(com.ogerardin.xplane.inspection.Severity.INFO)
                .message("Archive type identified as: " + installableType.getClass().getName())
                .build()
        );
        
        return typeMessage.append(installableType.preconditions(xPlane, archive));
    }

    @SneakyThrows
    @Override
    public void install(XPlane xPlane, ProgressListener progressListener) {
        InstallableType installableType = getInstallableType()
            .orElseThrow(() -> new IllegalStateException("No installable type identified"));
        installableType.install(xPlane, archive, progressListener);
    }

}
