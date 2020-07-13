package com.ogerardin.xplane.file.grammar;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

/**
 * Base class for all X-Plane file format parsers; uses <a href="https://github.com/sirthias/parboiled">Parboiled</a>.
 * The main rule is always {@link #XPlaneFile()}.
 * Type of values is {@link #Object} so we can push anything.
 */
public abstract class XPlaneFileParser extends BaseParser<Object> {

    public abstract Rule XPlaneFile();

}
