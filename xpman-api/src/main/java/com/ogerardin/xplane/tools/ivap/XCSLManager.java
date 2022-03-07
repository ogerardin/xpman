package com.ogerardin.xplane.tools.ivap;

import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.util.IntrospectionHelper;

import java.nio.file.Path;

public class XCSLManager extends Tool {

    private static final String URL = "https://csl.x-air.ru/download/src/409";

    public XCSLManager(Path path) throws InstantiationException {
        super(path, "X-CSL updater");
        IntrospectionHelper.require(path.getFileName().toString().equals("X-CSL-Updater.app"));
    }
}
