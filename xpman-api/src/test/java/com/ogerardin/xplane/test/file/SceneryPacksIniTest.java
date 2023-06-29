package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.SceneryPacksIniFile;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class SceneryPacksIniTest {

    @Test
    void testCanInstantiateIniFile() {
        Path file = XPlane.getDefaultXPRootFolder().resolve("Custom Scenery/scenery_packs.ini");

        final SceneryPacksIniFile sceneryPacksIniFile = new SceneryPacksIniFile(file);
        assertThat(sceneryPacksIniFile.getFileSpecVersion(), is("1000"));
        assertThat(sceneryPacksIniFile.getSceneryPackList(), hasItem(SceneryPackIniItem.of("Custom Scenery/X-Plane Landmarks - Chicago")));
    }

}