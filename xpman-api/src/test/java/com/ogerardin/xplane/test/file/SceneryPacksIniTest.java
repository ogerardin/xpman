package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.EnableOnLocalXPlane;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.file.SceneryPacksIniFile;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@ExtendWith(TimingExtension.class)
@EnableOnLocalXPlane
class SceneryPacksIniTest {

    @Test
    void testCanInstantiateIniFile() {
        Path file = XPlaneTestUtil.getDefaultXPRootFolder().resolve("Custom Scenery/scenery_packs.ini");

        final SceneryPacksIniFile sceneryPacksIniFile = new SceneryPacksIniFile(file);
        assertThat(sceneryPacksIniFile.getFileSpecVersion(), is("1000"));
        assertThat(sceneryPacksIniFile.getSceneryPackList(), hasItem(SceneryPackIniItem.of("Custom Scenery/X-Plane Landmarks - Chicago")));
    }

    @Test
    void writesEnabledDisabledAndTokenEntries() throws Exception {
        Path source = Path.of("src/test/resources/scenery_packs_disabled.ini");
        Path target = Files.createTempFile("scenery_packs", ".ini");
        try {
            SceneryPacksIniFile iniFile = new SceneryPacksIniFile(source);
            iniFile.write(target, iniFile.getSceneryPackList());

            assertThat(Files.readString(target), is("""
                    I
                    1000 Version
                    SCENERY

                    SCENERY_PACK Custom Scenery/A
                    SCENERY_PACK_DISABLED Custom Scenery/B
                    SCENERY_PACK Custom Scenery/C
                    SCENERY_PACK_DISABLED Custom Scenery/D
                    SCENERY_PACK *GLOBAL_AIRPORTS*
                    """));
        } finally {
            Files.deleteIfExists(target);
        }
    }

}
