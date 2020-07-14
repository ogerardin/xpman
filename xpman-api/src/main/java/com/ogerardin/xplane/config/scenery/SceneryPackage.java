package com.ogerardin.xplane.config.scenery;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.diag.CheckResult;
import com.ogerardin.xplane.diag.Checkable;
import com.ogerardin.xplane.diag.Severity;
import com.ogerardin.xplane.file.ObjFile;
import com.ogerardin.xplane.file.data.obj.ObjAttribute;
import com.ogerardin.xplane.file.data.obj.ObjFileData;
import com.ogerardin.xplane.file.data.obj.ObjTexture;
import com.ogerardin.xplane.util.FileUtils;
import com.sun.prism.Texture;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class SceneryPackage implements Checkable {

    public static final String EARTH_NAV_DATA = "Earth nav data";

    @NonNull
    @Setter(AccessLevel.PACKAGE)
    private Path folder;

    @Getter(lazy = true)
    private final int tileCount = countTiles();

    private boolean enabled = false;

    private Integer rank = null;


    @SneakyThrows
    private int countTiles() {
        return FileUtils.findFiles(getEarthNavDataFolder(), path -> path.getFileName().toString().endsWith(".dsf")).size();
    }

    public String getName() {
        return folder.getFileName().toString();
    }

    @SuppressWarnings("unused")
    public boolean isAirport() {
        return Files.exists(getEarthNavDataFolder().resolve("apt.dat"));
    }

    public Path getEarthNavDataFolder() {
        return folder.resolve(EARTH_NAV_DATA);
    }

    @SuppressWarnings("unused")
    public boolean isLibrary() {
        return Files.exists(folder.resolve("library.txt"));
    }

    public String getVersion() {
        return null;
    }


    @SneakyThrows
    @Override
    public List<CheckResult> check(XPlaneInstance xPlaneInstance) {
        final List<Path> objFiles = FileUtils.findFiles(folder, path -> path.getFileName().toString().endsWith(".obj"));
        List<CheckResult> result = new ArrayList<>();
        for (Path file : objFiles) {
            log.info("Inspecting {}", file);
            ObjFile objFile = new ObjFile(file);
            ObjFileData data = objFile.getData();
            for (ObjAttribute attribute : data.getAttributes()) {
                if (attribute instanceof ObjTexture) {
                    String reference = ((ObjTexture) attribute).getReference();
                    if (! Files.exists(file.resolveSibling(reference))) {
                        result.add(new CheckResult(Severity.ERROR, "Missing texture: " + reference));
                    }
                }
            }
        }

        return result;
    }

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }
}
