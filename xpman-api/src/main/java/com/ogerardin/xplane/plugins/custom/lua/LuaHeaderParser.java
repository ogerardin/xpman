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

    private LuaHeaderParser() {}
    
    private static final int MAX_LINES = 50;
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^\\s*+--\\s*+@?name[:\\s]++(.++)$", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^\\s*+--\\s*+@?version[:\\s]++(.++)$", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern DESC_PATTERN = Pattern.compile(
        "^\\s*+--\\s*+@?description[:\\s]++(.++)$", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*+--\\s*+(.++)$");
    
    /**
     * Parses metadata from a Lua script file.
     * Reads the first 50 lines looking for metadata patterns.
     */
    public static LuaMetadata parse(Path luaFile) {
        HeaderMetadata metadata = new HeaderMetadata();
        
        try (Stream<String> lines = Files.lines(luaFile)) {
            int lineCount = 0;
            for (String line : lines.limit(MAX_LINES).toList()) {
                metadata.accept(line, ++lineCount);
            }
        } catch (IOException e) {
            log.warn("Failed to parse Lua header: {}", luaFile, e);
        }

        // Fallback: derive name from filename
        String name = metadata.name == null ? deriveNameFromFilename(luaFile) : metadata.name;
        return metadata.result(name);
    }

    private static class HeaderMetadata {
        private String name;
        private String version;
        private final StringBuilder description = new StringBuilder();

        private void accept(String line, int lineCount) {
            if (name == null && (name = firstGroup(NAME_PATTERN, line)) != null) return;
            if (version == null && (version = firstGroup(VERSION_PATTERN, line)) != null) return;
            if (description.isEmpty()) {
                String explicitDescription = firstGroup(DESC_PATTERN, line);
                if (explicitDescription != null) {
                    description.append(explicitDescription);
                    return;
                }
            }
            appendFallbackComment(description, line, lineCount);
        }

        private LuaMetadata result(String name) {
            return new LuaMetadata(name, description.isEmpty() ? null : description.toString(), version);
        }

        private static String firstGroup(Pattern pattern, String line) {
            Matcher matcher = pattern.matcher(line);
            return matcher.matches() ? matcher.group(1).trim() : null;
        }

        private static void appendFallbackComment(StringBuilder description, String line, int lineCount) {
            if (!description.isEmpty() || lineCount > 10) return;
            Matcher matcher = COMMENT_PATTERN.matcher(line);
            if (!matcher.matches()) return;
            String comment = matcher.group(1).trim();
            if (!comment.isEmpty() && !comment.startsWith("@") && !comment.contains(":")) {
                if (!description.isEmpty()) description.append(" ");
                description.append(comment);
            }
        }
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
