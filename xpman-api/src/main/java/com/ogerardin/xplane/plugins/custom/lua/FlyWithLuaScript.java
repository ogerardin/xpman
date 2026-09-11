package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.Deletable;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.CheckFlyWithLuaInstalled;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a FlyWithLuaPlugin script (.lua file) in the Scripts folder.
 */
@Slf4j
@Getter
public class FlyWithLuaScript implements Inspectable, Deletable {
    
    private final XPlane xPlane;
    private final Path luaFile;
    private final String name;
    private final String desc;
    private final String version;
    
    public FlyWithLuaScript(XPlane xPlane, Path luaFile) {
        this.xPlane = xPlane;
        this.luaFile = luaFile;
        
        LuaHeaderParser.LuaMetadata metadata = LuaHeaderParser.parse(luaFile);
        this.name = metadata.name() != null ? metadata.name() : luaFile.getFileName().toString();
        this.desc = metadata.description();
        this.version = metadata.version();
        
        log.debug("Created FlyWithLuaScript: {} from {}", name, luaFile);
    }
    
    /**
     * Returns the base folder containing this script (Scripts/).
     */
    public Path getBaseFolder() {
        return luaFile.getParent();
    }
    
    @Override
    public InspectionResult inspect() {
        return InspectionResult.empty();
    }

    @Override
    public void delete() throws IOException {
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(luaFile.toFile());
    }

    /**
     * Installable type for FlyWithLuaPlugin scripts.
     * Recognizes archives containing .lua files (but no .xpl files) and installs
     * to the FlyWithLuaPlugin/Scripts folder.
     */
    public static class InstallableType implements com.ogerardin.xplane.install.InstallableType {

        @Override
        public boolean recognizes(Archive archive) {
            return archive.getPaths().stream()
                .anyMatch(path -> path.getFileName().toString().endsWith(".lua"))
                && archive.getPaths().stream()
                .noneMatch(path -> path.getFileName().toString().endsWith(".xpl"));
        }

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            return new CheckFlyWithLuaInstalled(xPlane).inspectable(archive).inspect();
        }

        @Override
        public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
            try {
                xPlane.getPluginManager().install(archive, progress);
            } catch (IOException e) {
                throw new InstallationException(e);
            }
        }
    }
}
