package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.inspections.AssertXPlaneVersionAtLeast;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.zip.Archive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the AssertXPlaneVersionAtLeast inspection.
 */
class AssertXPlaneVersionAtLeastTest {

    @ParameterizedTest(name = "{0} passes the {1} minimum")
    @CsvSource({
            "12.4.4, 12.4.4",   // meets minimum
            "12.5.0, 12.4.4",   // exceeds minimum
            ", 12.4.4"          // unknown version never blocks
    })
    void passesWhenVersionMeetsExceedsOrUnknownMinimum(String version, String minimum) {
        InspectionResult result = inspect(version, minimum);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void blocksWhenVersionBelowMinimum() {
        InspectionResult result = inspect("12.4.3", "12.4.4");

        assertThat(result.isEmpty(), is(false));
        assertThat(result.getMessages().size(), is(1));
        assertThat(result.getMessages().get(0).getSeverity(), is(com.ogerardin.xplane.inspection.Severity.ERROR));
        assertTrue(result.getMessages().get(0).isAbort());
    }

    @Test
    void handlesLegacyVersionFormat() {
        // Legacy format 12.04r3 = 12.0.4, below the 12.4.4 minimum
        InspectionResult result = inspect("12.04r3", "12.4.4");
        assertThat(result.isEmpty(), is(false));
    }

    private InspectionResult inspect(String version, String minimum) {
        XPlane mockXPlane = mock(XPlane.class);
        when(mockXPlane.getVersion()).thenReturn(version);

        Archive archive = mock(Archive.class);
        return new AssertXPlaneVersionAtLeast(mockXPlane, minimum).inspect(archive);
    }
}
