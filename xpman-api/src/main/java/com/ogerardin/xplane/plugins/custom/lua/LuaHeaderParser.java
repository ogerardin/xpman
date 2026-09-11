package com.ogerardin.xplane.plugins.custom.lua;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Parses metadata from Lua script header comments.
 */
@Slf4j
public class LuaHeaderParser {
    
    private static final int MAX_LINES = 50;
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^\\s*--\\s*(?:@name|name|Name)[:\\s]+(.+)$", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^\\s*--\\s*(?:@version|version|Version)[:\\s]+(.+)$", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern DESC_PATTERN = Pattern.compile(
        "^\\s*--\\s*(?:@description|description|Description)[:\\s]+(.+)$", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*--\\s*(.+)$");
    
    /**
     * Parses metadata from a Lua script file.
     * Reads the first 50 lines looking for metadata patterns.
     */
    public static LuaMetadata parse(Path luaFile) {
        String name = null;
        String version = null;
        StringBuilder description = new StringBuilder();
        
        try (Stream<String> lines = Files.lines(luaFile)) {
            int lineCount = 0;
            for (String line : lines.limit(MAX_LINES).toList()) {
                lineCount++;
                
                if (name == null) {
                    Matcher nameMatcher = NAME_PATTERN.matcher(line);
                    if (nameMatcher.matches()) {
                        name = nameMatcher.group(1).trim();
                        continue;
                    }
                }
                
                if (version == null) {
                    Matcher versionMatcher = VERSION_PATTERN.matcher(line);
                    if (versionMatcher.matches()) {
                        version = versionMatcher.group(1).trim();
                        continue;
                    }
                }
                
                if (description.isEmpty()) {
                    Matcher descMatcher = DESC_PATTERN.matcher(line);
                    if (descMatcher.matches()) {
                        description.append(descMatcher.group(1).trim());
                        continue;
                    }
                }
                
                // Collect general comment lines for fallback description
                if (description.isEmpty() && lineCount <= 10) {
                    Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
                    if (commentMatcher.matches()) {
                        String comment = commentMatcher.group(1).trim();
                        if (!comment.isEmpty() && !comment.startsWith("@") && !comment.contains(":")) {
                            if (description.length() > 0) {
                                description.append(" ");
                            }
                            description.append(comment);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to parse Lua header: {}", luaFile, e);
        }
        
        // Fallback: derive name from filename
        if (name == null) {
            name = deriveNameFromFilename(luaFile);
        }
        
        String desc = description.length() > 0 ? description.toString() : null;
        
        return new LuaMetadata(name, desc, version);
    }
    
    /**
     * Derives a script name from its filename.
     * Converts "SimLoadManager.lua" to "Sim Load Manager"
     */
    private static String deriveNameFromFilename(Path luaFile) {
        String filename = luaFile.getFileName().toString();
        String name = filename.replaceAll("\\.lua$", "");
        
        // Insert spaces before capital letters (camelCase to spaces)
        name = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        
        return name;
    }

    /**
     * Metadata parsed from a Lua script header.
     */
    public static record LuaMetadata(String name, String description, String version) {

        public static LuaMetadata empty() {
            return new LuaMetadata(null, null, null);
        }

        public boolean isEmpty() {
            return name == null && description == null && version == null;
        }
    }
}
