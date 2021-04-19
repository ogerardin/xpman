package com.ogerardin.xpman.scenery_organizer;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class SceneryClass {

    private String name;

    private List<String> regexes;

    public SceneryClass(String name, String... regexes) {
        this.name = name;
        this.regexes = Arrays.asList(regexes);
    }

    public boolean matches(String sceneryName) {
        return getRegexes().stream()
                .anyMatch(sceneryName::matches);
    }
}
