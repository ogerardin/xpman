package com.ogerardin.xplane.plugins.custom;

/**
 * Metadata parsed from a Lua script header.
 */
public record LuaMetadata(String name, String description, String version) {
    
    public static LuaMetadata empty() {
        return new LuaMetadata(null, null, null);
    }
    
    public boolean isEmpty() {
        return name == null && description == null && version == null;
    }
}
