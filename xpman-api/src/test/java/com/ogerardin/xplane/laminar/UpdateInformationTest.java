package com.ogerardin.xplane.laminar;

import com.ogerardin.util.TimingExtension;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(TimingExtension.class)
public class UpdateInformationTest {

    @Test
    void testGetLatestVersions() {
        assertNotNull(UpdateInformation.getLatestBeta());
        assertNotNull(UpdateInformation.getLatestFinal());
    }
}
