package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryPackIniItemTest {

    @Test
    void of_shouldCarryValueAndDisabledFlag() {
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA").getIniValue(), is("Custom Scenery/KSEA"));
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA", true).isDisabled(), is(true));
        assertThat(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*").isDisabled(), is(false));
    }

    @Test
    void of_shouldStripTrailingSeparators() {
        // X-Plane writes folder values with trailing slashes; round-trip stability depends on dropping them
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA/").getIniValue(), is("Custom Scenery/KSEA"));
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA//").getIniValue(), is("Custom Scenery/KSEA"));
        // Windows-spelled values use backslashes: strip those too, so the rewrite matches on all platforms
        assertThat(SceneryPackIniItem.of("Custom Scenery\\KSEA\\").getIniValue(), is("Custom Scenery\\KSEA"));
    }

    @Test
    void disabled_shouldParticipateInEquality() {
        assertNotEquals(SceneryPackIniItem.of("Custom Scenery/KSEA", false),
                SceneryPackIniItem.of("Custom Scenery/KSEA", true));
        assertEquals(SceneryPackIniItem.of("Custom Scenery/KSEA", false),
                SceneryPackIniItem.of("Custom Scenery/KSEA"));
    }

    @Test
    void isToken_shouldDetectTokens() {
        assertTrue(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*").isToken());
        assertFalse(SceneryPackIniItem.of("Custom Scenery/KSEA").isToken());
    }
}
