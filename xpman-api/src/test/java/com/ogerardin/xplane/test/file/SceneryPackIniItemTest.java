package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryPackIniItemTest {

    @Test
    void of_shouldClassifyFoldersAndTokens() {
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA"), is(new PathSceneryPackIniItem("Custom Scenery/KSEA")));
        assertThat(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*"), is(new TokenSceneryPackIniItem("*GLOBAL_AIRPORTS*")));
    }

    @Test
    void of_shouldCarryDisabledFlag() {
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA", true).isDisabled(), is(true));
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA"), is(new PathSceneryPackIniItem("Custom Scenery/KSEA")));
    }

    @Test
    void disabled_shouldParticipateInEquality() {
        assertNotEquals(new PathSceneryPackIniItem("Custom Scenery/KSEA", false),
                new PathSceneryPackIniItem("Custom Scenery/KSEA", true));
    }

    @Test
    void isToken_shouldFollowType() {
        assertTrue(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*").isToken());
        assertFalse(SceneryPackIniItem.of("Custom Scenery/KSEA").isToken());
    }

    @Test
    void of_shouldStripTrailingSeparators() {
        // X-Plane writes folder values with trailing separators; round-trip stability depends on dropping them
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA/").getIniValue(), is("Custom Scenery/KSEA"));
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA//").getIniValue(), is("Custom Scenery/KSEA"));
        // Windows-spelled values use backslashes: strip those too, so the rewrite matches on all platforms
        assertThat(SceneryPackIniItem.of("Custom Scenery\\KSEA\\").getIniValue(), is("Custom Scenery\\KSEA"));
    }
}
