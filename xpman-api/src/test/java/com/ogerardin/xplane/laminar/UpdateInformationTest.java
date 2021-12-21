package com.ogerardin.xplane.laminar;

import com.ogerardin.util.TimingExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(TimingExtension.class)
class UpdateInformationTest {

    @Test
    void testGetLatestVersions() {
        String latestBeta = UpdateInformation.getLatestBeta();
        log.info("latest beta: {}", latestBeta);
        assertNotNull(latestBeta);

        String latestRelease = UpdateInformation.getLatestFinal();
        log.info("latest release: {}", latestRelease);
        assertNotNull(latestRelease);
    }
}
