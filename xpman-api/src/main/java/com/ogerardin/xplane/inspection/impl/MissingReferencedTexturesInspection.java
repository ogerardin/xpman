package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.file.ObjFile;
import com.ogerardin.xplane.file.data.obj.ObjTexture;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.FileUtils;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link Inspection} will check that all textures referenced in all .obj files of the examined scenery actually
 * exist.
 */
@Slf4j
public enum MissingReferencedTexturesInspection implements Inspection<SceneryPackage> {

    INSTANCE;

    @SneakyThrows
    @Override
    public @NonNull InspectionResult inspect(@NonNull SceneryPackage target) {
        final List<Path> objFiles = FileUtils.findFiles(target.getFolder(), path -> path.getFileName().toString().endsWith(".obj"));
        List<InspectionMessage> messages = new ArrayList<>();
        for (Path file : objFiles) {
            log.info("Inspecting {}", file);
            ObjFile objFile = new ObjFile(file);
            final List<InspectionMessage> inspectionMessages = objFile.getData().getAttributes().stream()
                    .filter(ObjTexture.class::isInstance)
                    .map(ObjTexture.class::cast)
                    .filter(texture -> ! Files.exists(file.resolveSibling(texture.getReference())))
                    .map(texture -> InspectionMessage.builder()
                            .severity(Severity.ERROR)
                            .object(file.getFileName().toString())
                            .message("Missing texture: " + texture.getReference())
                            .details("Texture " + texture.getReference() + " is referenced in " + file + ", but the file does not exist")
                            .build())
                    .toList();
            messages.addAll(inspectionMessages);
        }
        return InspectionResult.of(messages);
    }
}
