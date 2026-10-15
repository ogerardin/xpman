package com.ogerardin.xplane.test.scenery;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.scenery.SceneryIniToken;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryIniTokenTest {

    @Test
    void of_shouldRecognizeKnownMarkers() {
        assertThat(SceneryIniToken.of(SceneryIniToken.GLOBAL_AIRPORTS.getMarker()),
                is(Optional.of(SceneryIniToken.GLOBAL_AIRPORTS)));
        assertThat(SceneryIniToken.of("*GLOBAL_AIRPORTS*"), is(Optional.of(SceneryIniToken.GLOBAL_AIRPORTS)));
    }

    @Test
    void of_shouldReturnEmptyForUnknownTokens() {
        assertThat(SceneryIniToken.of("*FOO*"), is(Optional.empty()));
    }

    @Test
    void marker_shouldBeTheIniSpelling() {
        assertThat(SceneryIniToken.GLOBAL_AIRPORTS.getMarker(), is("*GLOBAL_AIRPORTS*"));
    }
}
