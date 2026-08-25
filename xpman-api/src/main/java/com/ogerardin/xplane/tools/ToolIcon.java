package com.ogerardin.xplane.tools;

import java.net.URL;

/**
 * Represents an icon for a tool. Can be a URL, a classpath resource, or an icon font literal.
 */
public sealed interface ToolIcon permits ToolIcon.Url, ToolIcon.Resource, ToolIcon.IconFont {

    record Url(URL url) implements ToolIcon {}

    record Resource(String path) implements ToolIcon {}

    record IconFont(String literal) implements ToolIcon {}
}
