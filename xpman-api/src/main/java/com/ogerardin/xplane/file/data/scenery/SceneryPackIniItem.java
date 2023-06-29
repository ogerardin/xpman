package com.ogerardin.xplane.file.data.scenery;

import java.nio.file.Paths;

public abstract sealed class SceneryPackIniItem permits PathSceneryPackIniItem, TokenSceneryPackIniItem {

    public static SceneryPackIniItem of(String folderOrToken) {
        if (folderOrToken.matches("^\\*.+\\*$")) {
            return new TokenSceneryPackIniItem(folderOrToken);
        }
        return new PathSceneryPackIniItem(Paths.get(folderOrToken));
    }
}
