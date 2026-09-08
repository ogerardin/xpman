package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * Represents a FlyWithLua script (.lua file) in the Scripts folder.
 */
@Slf4j
@Getter
public class FlyWithLuaScript implements Inspectable {
    
    private final XPlane xPlane;
    private final Path luaFile;
    private final String name;
    private final String desc;
    private final String version;
    
    public FlyWithLuaScript(XPlane xPlane, Path luaFile) {
        this.xPlane = xPlane;
        this.luaFile = luaFile;
        
        LuaMetadata metadata = LuaHeaderParser.parse(luaFile);
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
}
