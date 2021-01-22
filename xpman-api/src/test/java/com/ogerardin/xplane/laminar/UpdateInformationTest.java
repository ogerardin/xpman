package com.ogerardin.xplane.laminar;

import com.ogerardin.util.TimingExtension;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TimingExtension.class)
public class UpdateInformationTest {

    @Test
    void testGetLatestVersions() {
        MatcherAssert.assertThat(UpdateInformation.getLatestBeta(), Matchers.notNullValue());
        MatcherAssert.assertThat(UpdateInformation.getLatestFinal(), Matchers.notNullValue());
    }
}
