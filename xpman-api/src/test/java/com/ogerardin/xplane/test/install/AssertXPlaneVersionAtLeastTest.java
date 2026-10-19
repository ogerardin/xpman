package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.inspections.AssertXPlaneVersionAtLeast;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.zip.Archive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the AssertXPlaneVersionAtLeast inspection.
 */
class AssertXPlaneVersionAtLeastTest {

    @TempDir
    Path tempDir;

    @Test
    void passesWhenVersionMeetsMinimum() throws Exception {
        // Create a fake X-Plane installation
        Path xplaneDir = tempDir.resolve("X-Plane 12");
        Files.createDirectories(xplaneDir);
        
        XPlane xPlane = new XPlane(xplaneDir);
        XPlane mockXPlane = mock(XPlane.class);
        when(mockXPlane.getVersion()).thenReturn("12.4.4");
        
        Archive archive = mock(Archive.class);
        AssertXPlaneVersionAtLeast inspection = new AssertXPlaneVersionAtLeast(mockXPlane, "12.4.4");
        
        InspectionResult result = inspection.inspect(archive);
        
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void passesWhenVersionExceedsMinimum() throws Exception {
        XPlane mockXPlane = mock(XPlane.class);
        when(mockXPlane.getVersion()).thenReturn("12.5.0");
        
        Archive archive = mock(Archive.class);
        AssertXPlaneVersionAtLeast inspection = new AssertXPlaneVersionAtLeast(mockXPlane, "12.4.4");
        
        InspectionResult result = inspection.inspect(archive);
        
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void blocksWhenVersionBelowMinimum() throws Exception {
        XPlane mockXPlane = mock(XPlane.class);
        when(mockXPlane.getVersion()).thenReturn("12.4.3");
        
        Archive archive = mock(Archive.class);
        AssertXPlaneVersionAtLeast inspection = new AssertXPlaneVersionAtLeast(mockXPlane, "12.4.4");
        
        InspectionResult result = inspection.inspect(archive);
        
        assertThat(result.isEmpty(), is(false));
        assertThat(result.getMessages().size(), is(1));
        assertThat(result.getMessages().get(0).getSeverity(), is(com.ogerardin.xplane.inspection.Severity.ERROR));
        assertTrue(result.getMessages().get(0).isAbort());
    }

    @Test
    void passesWhenVersionUnknown() throws Exception {
        XPlane mockXPlane = mock(XPlane.class);
        when(mockXPlane.getVersion()).thenReturn(null);
        
        Archive archive = mock(Archive.class);
        AssertXPlaneVersionAtLeast inspection = new AssertXPlaneVersionAtLeast(mockXPlane, "12.4.4");
        
        InspectionResult result = inspection.inspect(archive);
        
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void handlesLegacyVersionFormat() throws Exception {
        XPlane mockXPlane = mock(XPlane.class);
        when(mockXPlane.getVersion()).thenReturn("12.04r3"); // Legacy format = 12.0.4
        
        Archive archive = mock(Archive.class);
        AssertXPlaneVersionAtLeast inspection = new AssertXPlaneVersionAtLeast(mockXPlane, "12.4.4");
        
        InspectionResult result = inspection.inspect(archive);
        
        // 12.0.4 < 12.4.4, should block
        assertThat(result.isEmpty(), is(false));
    }
}
