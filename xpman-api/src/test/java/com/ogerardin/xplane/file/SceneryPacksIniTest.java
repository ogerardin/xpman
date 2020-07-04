package com.ogerardin.xplane.file;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.config.XPlaneInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class SceneryPacksIniTest {

    @Test
    public void testCanInstantiateAcfFile() {
        Path file = XPlaneInstance.getDefaultXPRootFolder().resolve("Custom Scenery/scenery_packs.ini");

        final SceneryPacksIniFile sceneryPacksIniFile = new SceneryPacksIniFile(file);
        assertThat(sceneryPacksIniFile.getFileSpecVersion(), is("1000"));
        assertThat(sceneryPacksIniFile.getSceneryPackList(), hasItem(Paths.get("Custom Scenery/Global Airports")));
    }

}