package com.ogerardin.xplane.file.grammar;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

public abstract class XPlaneFileParser extends BaseParser<Object> {

    public abstract Rule XPlaneFile();

}
