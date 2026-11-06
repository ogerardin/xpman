package com.ogerardin.xplane.test.laminar;

import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xplane.laminar.UpdateInformation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
//@ExtendWith(TimingExtension.class)
class UpdateInformationTest {

    @Test
    void testGetLatestVersions() {

        for (XPlaneMajorVersion majorVersion : XPlaneMajorVersion.values()) {
            UpdateInformation updateInformation = new UpdateInformation(majorVersion);
            log.info("major version: {}", majorVersion);

            XPlaneReleaseInfo latestBeta = updateInformation.getLatestBeta();
            log.info("latest beta: {}", latestBeta);
            assertNotNull(latestBeta);

            XPlaneReleaseInfo latestRelease = updateInformation.getLatestFinal();
            log.info("latest release: {}", latestRelease);
            assertNotNull(latestRelease);
        }


    }

    @Test
    void noVersionHasEmptyReleaseNotesUrl() {
        XPlaneMajorVersion majorVersion = mock(XPlaneMajorVersion.class);
        when(majorVersion.getServerListUrl()).thenReturn(null); // fetch impossible -> NO_VERSION returned

        XPlaneReleaseInfo latest = new UpdateInformation(majorVersion).getLatestBeta();

        // must not NPE: NO_VERSION must hold an empty Optional, not null
        assertThat(latest.releaseNotesUrl().isEmpty(), is(true));
    }
}
