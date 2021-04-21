package com.ogerardin.xpman.scenery_organizer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SceneryClass {

    private String name;

    private String regex;

    public SceneryClass(String name) {
        this(name, null);
    }

    public boolean matches(String sceneryName) {
        return sceneryName.matches(regex);
    }
}
