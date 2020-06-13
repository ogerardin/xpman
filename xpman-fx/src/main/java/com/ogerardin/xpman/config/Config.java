package com.ogerardin.xpman.config;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Config {

    String lastXPlanePath;

    Set<String> recentPaths = new HashSet<>();
}
