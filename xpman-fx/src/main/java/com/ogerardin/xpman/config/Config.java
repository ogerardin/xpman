package com.ogerardin.xpman.config;

import javafx.geometry.Rectangle2D;
import lombok.Data;

import java.util.HashSet;

@Data
public class Config {

    String lastXPlanePath;

    StringSet recentPaths = new StringSet();

    Rectangle2D lastPosition;

    public static class StringSet extends HashSet<String> {}
}
