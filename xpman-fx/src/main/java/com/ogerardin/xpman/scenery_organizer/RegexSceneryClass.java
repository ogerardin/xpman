package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegexSceneryClass implements SceneryClass {

    private String name;

    private String regex;

    public RegexSceneryClass(String name) {
        this(name, null);
    }

    @Override
    public boolean matches(SceneryPackage sceneryPackage) {
        return sceneryPackage.getName().matches(regex);
    }
}
