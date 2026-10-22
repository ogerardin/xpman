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
 * Supports single-line comments ({@code --}), block comments ({@code --[[ ... ]]}),
 * explicit {@code @tag} fields, and multi-line descriptions.
 */
@Slf4j
public class LuaHeaderParser {

    private LuaHeaderParser() {}

    private static final int MAX_LINES = 100;

    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^\\s*+@?name[:\\s]++(.++)$", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^\\s*+@?version[:\\s]++(.++)$", Pattern.CASE_INSENSITIVE
    );

    // Matches "V1.2" or "v1.2.3" shorthand version (e.g., "V1.2 by Carsten Lynker").
    private static final Pattern VERSION_V_PATTERN = Pattern.compile(
        "^\\s*+[Vv](\\d+(?:\\.\\d+)*)\\b.*$"
    );

    // Matches "Ver." or "Ver" followed by version number (e.g., "Ver. 1.02").
    private static final Pattern VERSION_VER_PATTERN = Pattern.compile(
        "^.*?[Vv]er\\.?\\s+(\\d+(?:\\.\\d+)*)\\b.*$"
    );

    private static final Pattern DESC_PATTERN = Pattern.compile(
        "^\\s*+@?description[:\\s]*+(.*)$", Pattern.CASE_INSENSITIVE
    );

    // Strips a leading "--" prefix (and optional whitespace) from a line.
    private static final Pattern COMMENT_PREFIX = Pattern.compile("^\\s*+--\\s?+(.*)$");

    // Block comment markers: --[[ ... ]]
    private static final Pattern BLOCK_OPEN = Pattern.compile("^\\s*+--\\[\\[(.*)$");
    private static final Pattern BLOCK_CLOSE = Pattern.compile("^(.*?)]]");

    // Any explicit @tag (used to detect end of a description continuation block).
    private static final Pattern TAG_PREFIX = Pattern.compile("^\\s*+@\\w+");

    // Matches "Key: Value" style lines that are probably metadata, not description text.
    private static final Pattern KEY_COLON_VALUE = Pattern.compile("^\\s*+[A-Za-z][\\w\\s]{0,30}:\\s+\\S");

    /**
     * Parses metadata from a Lua script file.
     * Reads up to {@value MAX_LINES} lines, handling both {@code --} line comments
     * and {@code --[[ ... ]]} block comments.
     */
    public static LuaMetadata parse(Path luaFile) {
        HeaderMetadata metadata = new HeaderMetadata();

        try (Stream<String> lines = Files.lines(luaFile)) {
            boolean inBlock = false;
            int lineCount = 0;
            for (String raw : lines.limit(MAX_LINES).toList()) {
                lineCount++;
                if (inBlock) {
                    Matcher close = BLOCK_CLOSE.matcher(raw);
                    if (close.find()) {
                        String text = close.group(1);
                        // Strip inner comment prefix "-- " from block comment lines
                        text = text.replaceFirst("^--\\s?", "");
                        if (!text.isEmpty()) {
                            metadata.acceptText(text, lineCount);
                        }
                        inBlock = false;
                    } else {
                        // Strip inner comment prefix "-- " from block comment lines
                        String text = raw.replaceFirst("^--\\s?", "");
                        metadata.acceptText(text, lineCount);
                    }
                } else {
                    Matcher open = BLOCK_OPEN.matcher(raw);
                    if (open.matches()) {
                        String afterOpen = open.group(1);
                        // Check if block closes on the same line: --[[ content ]]
                        Matcher close = BLOCK_CLOSE.matcher(afterOpen);
                        if (close.find()) {
                            String text = close.group(1);
                            if (!text.isEmpty()) {
                                metadata.acceptText(text, lineCount);
                            }
                        } else {
                            if (!afterOpen.isEmpty()) {
                                metadata.acceptText(afterOpen, lineCount);
                            }
                            inBlock = true;
                        }
                    } else {
                        // Regular -- line: strip prefix and trailing decorative "--", feed comment text
                        Matcher prefix = COMMENT_PREFIX.matcher(raw);
                        if (prefix.matches()) {
                            String text = prefix.group(1).replaceFirst("\\s*+--\\s*+$", "");
                            metadata.acceptText(text, lineCount);
                        } else {
                            // Non-comment line ends header scanning
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to parse Lua header: {}", luaFile, e);
        }

        // Fallback: derive name from filename
        String name = metadata.name == null ? deriveNameFromFilename(luaFile) : metadata.name;
        return metadata.result(name);
    }

    /** Parsed metadata from a FlyWithLua script header comment. */
    private static class HeaderMetadata {
        private String name;
        private String version;
        private final StringBuilder description = new StringBuilder();

        // True while we're accumulating continuation lines of an explicit @description.
        private boolean inDescription;

        // Number of non-empty comment lines seen (used to limit fallback description).
        private int commentLineCount;

        /**
         * Processes the text content of a comment line (with {@code --} already stripped).
         */
        private void acceptText(String text, int lineCount) {
            commentLineCount++;
            String trimmed = text.trim();

            // Empty comment line ends description continuation (acts as paragraph break).
            if (trimmed.isEmpty()) {
                inDescription = false;
                return;
            }

            // 1. Explicit @name
            if (name == null) {
                String n = firstGroup(NAME_PATTERN, trimmed);
                if (n != null) { name = n; inDescription = false; return; }
            }

            // 2. Explicit @version
            if (version == null) {
                String v = firstGroup(VERSION_PATTERN, trimmed);
                if (v != null) { version = v; inDescription = false; return; }
            }

            // 3. V<version> shorthand (e.g., "V1.2 by ...")
            if (version == null) {
                String v = firstGroup(VERSION_V_PATTERN, trimmed);
                if (v != null) { version = v; inDescription = false; return; }
            }

            // 4. Ver<version> shorthand (e.g., "Ver. 1.02")
            if (version == null) {
                String v = firstGroup(VERSION_VER_PATTERN, trimmed);
                if (v != null) { 
                    version = v; 
                    inDescription = false;
                    // Don't return - let the line continue to be captured as description if it has text before "Ver"
                }
            }

            // 3. Explicit @description (starts a new multi-line description)
            String d = firstGroup(DESC_PATTERN, trimmed);
            if (d != null) {
                if (description.isEmpty()) {
                    description.append(d);
                } else {
                    // A second @description line: start fresh (replace).
                    description.setLength(0);
                    description.append(d);
                }
                inDescription = true;
                return;
            }

            // 4. Any other @tag ends the description continuation block.
            if (TAG_PREFIX.matcher(trimmed).lookingAt()) {
                inDescription = false;
                return;
            }

            // 5. Continuation of an explicit @description.
            if (inDescription) {
                // Lines with no word characters (separators like "-- -- --") end the continuation.
                if (!trimmed.matches(".*\\w.*")) {
                    inDescription = false;
                    return;
                }
                if (!description.isEmpty()) description.append(' ');
                description.append(trimmed);
                return;
            }

            // 6. Fallback: use initial plain comment lines as description,
            //    but only within the first 10 non-empty comments, only if
            //    they don't look like "Key: Value" metadata, and only if
            //    they contain at least one word character (filters decoration lines like "---").
            if (description.isEmpty() && commentLineCount <= 10
                    && !KEY_COLON_VALUE.matcher(trimmed).lookingAt()
                    && trimmed.matches(".*\\w.*")) {
                if (!description.isEmpty()) description.append(' ');
                description.append(trimmed);
            }
        }

        private LuaMetadata result(String name) {
            String desc = description.isEmpty() ? null : description.toString();
            // Strip a single trailing period (descriptions are labels, not sentences).
            if (desc != null && desc.endsWith(".")) {
                desc = desc.substring(0, desc.length() - 1);
            }
            return new LuaMetadata(name, desc, version);
        }

        private static String firstGroup(Pattern pattern, String line) {
            Matcher matcher = pattern.matcher(line);
            return matcher.matches() ? matcher.group(1).trim() : null;
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
