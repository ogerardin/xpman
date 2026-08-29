package com.ogerardin.xplane.test.file;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryPackIniItemTest {

    @Test
    void of_shouldClassifyPathAndToken() {
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA"),
                is(new PathSceneryPackIniItem(Path.of("Custom Scenery/KSEA"))));
        assertThat(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*"),
                is(new TokenSceneryPackIniItem("*GLOBAL_AIRPORTS*")));
    }

    @Test
    void of_withDisabledFlag_shouldSetDisabled() {
        assertThat(SceneryPackIniItem.of("Custom Scenery/KSEA", true),
                is(new PathSceneryPackIniItem(Path.of("Custom Scenery/KSEA"), true)));
        assertThat(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*", false),
                is(new TokenSceneryPackIniItem("*GLOBAL_AIRPORTS*")));
    }

    @Test
    void disabled_shouldParticipateInEquality() {
        assertNotEquals(SceneryPackIniItem.of("Custom Scenery/KSEA", false),
                SceneryPackIniItem.of("Custom Scenery/KSEA", true));
    }
}
