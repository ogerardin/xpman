package com.ogerardin.xplane.laminar;

import com.ogerardin.xplane.XPlaneMajorVersion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
//@ExtendWith(TimingExtension.class)
class UpdateInformationTest {

    @Test
    void testGetLatestVersions() {

        for (XPlaneMajorVersion majorVersion : XPlaneMajorVersion.values()) {
            UpdateInformation updateInformation = new UpdateInformation(majorVersion);
            log.info("major version: {}", majorVersion);

            String latestBeta = updateInformation.getLatestBeta();
            log.info("latest beta: {}", latestBeta);
            assertNotNull(latestBeta);

            String latestRelease = updateInformation.getLatestFinal();
            log.info("latest release: {}", latestRelease);
            assertNotNull(latestRelease);
        }


    }
}
