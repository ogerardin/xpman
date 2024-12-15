package com.ogerardin.xplane.test;

import com.ogerardin.xplane.XPlaneMajorVersion;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class XPlaneMajorVersionTest {

    public static final String RELEASE_NOTES_BASE = "https://www.x-plane.com/kb/";

    @Test
    void testReleaseNotesBuilder() {
        assertVersion("12.1.3-b5-f4487de7", "x-plane-12-1-3-release-notes");
        assertVersion("12.04r3", "x-plane-12-00-release-notes/#1204r3");
    }

    private void assertVersion(String version, String expectedUrl) {
        XPlaneMajorVersion v = XPlaneMajorVersion.of(version);
        String url = v.getReleaseNotesUrlBuilder().apply(version).get();
        String fullExpectedUrl = RELEASE_NOTES_BASE + expectedUrl;
        assertThat(url, is(fullExpectedUrl));
    }
}