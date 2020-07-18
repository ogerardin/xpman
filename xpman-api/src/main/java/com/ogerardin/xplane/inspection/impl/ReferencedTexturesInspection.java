package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.scenery.SceneryPackage;
import com.ogerardin.xplane.file.ObjFile;
import com.ogerardin.xplane.file.data.obj.ObjTexture;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ReferencedTexturesInspection implements Inspection<SceneryPackage> {

    public ReferencedTexturesInspection() {
    }

    @SneakyThrows
    @Override
    public List<InspectionMessage> apply(SceneryPackage target, XPlaneInstance xPlaneInstance) {
        final List<Path> objFiles = FileUtils.findFiles(target.getFolder(), path -> path.getFileName().toString().endsWith(".obj"));
        List<InspectionMessage> result = new ArrayList<>();
        for (Path file : objFiles) {
            log.info("Inspecting {}", file);
            ObjFile objFile = new ObjFile(file);
            final List<InspectionMessage> inspectionMessages = objFile.getData().getAttributes().stream()
                    .filter(ObjTexture.class::isInstance)
                    .map(ObjTexture.class::cast)
                    .filter(texture -> ! Files.exists(file.resolveSibling(texture.getReference())))
                    .map(texture -> new InspectionMessage(Severity.ERROR, file.toString(), "Missing texture: " + texture.getReference()))
                    .collect(Collectors.toList());
            result.addAll(inspectionMessages);
        }
        return result;
    }
}
