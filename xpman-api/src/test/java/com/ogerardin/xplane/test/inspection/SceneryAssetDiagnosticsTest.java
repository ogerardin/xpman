package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.inspection.impl.StartLogInspection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for scenery asset diagnostics (Task 3).
 */
class SceneryAssetDiagnosticsTest {

    @TempDir
    Path tempDir;

    private Path logFile;

    @BeforeEach
    void setup() throws IOException {
        logFile = tempDir.resolve("Log.txt");
    }

    private List<InspectionMessage> inspect() throws Exception {
        return new StartLogInspection().inspect(new XPlane(tempDir)).getMessages();
    }

    @Test
    void sceneryAssetsGroupByReferencingPack() throws Exception {
        String content = """
            Log.txt for X-Plane 12.1.1
            0:00:00.000 E/SCN: Failed to find resource: 'Custom Scenery/Example Airport/objects/terminal.obj'
            0:00:01.000 E/SCN: Missing object: "Custom Scenery/Example Airport/meshes/runway.dsf"
            0:00:02.000 E/SCN: Could not find object 'Custom Scenery/Example Airport/textures/surface.dds'
            ----- X-Plane has shut down -----
            """;
        Files.writeString(logFile, content);

        List<InspectionMessage> messages = inspect();

        List<InspectionMessage> scenery = messages.stream()
                .filter(m -> m.getObject() != null && m.getObject().equals("Example Airport"))
                .toList();

        assertThat(scenery, hasSize(1));
        assertThat(scenery.get(0).getSeverity(), is(Severity.ERROR));
        assertThat(scenery.get(0).getMessage(), containsString("3 missing assets"));

        String details = scenery.get(0).getDetails();
        assertThat(details, containsString("terminal.obj"));
        assertThat(details, containsString("runway.dsf"));
        assertThat(details, containsString("surface.dds"));
        assertThat(details, containsString("3D Object"));
        assertThat(details, containsString("Terrain Mesh"));
        assertThat(details, containsString("Texture"));
    }

    @Test
    void unresolvedVirtualResourceDoesNotInventLibraryOwner() throws Exception {
        String content = """
            Log.txt for X-Plane 12.1.1
            0:00:00.000 E/SCN: Failed to find resource: 'lib/airport/sign.obj'
            ----- X-Plane has shut down -----
            """;
        Files.writeString(logFile, content);

        List<InspectionMessage> messages = inspect();

        List<InspectionMessage> unknown = messages.stream()
                .filter(m -> m.getObject() != null && m.getObject().equals("Unknown scenery pack"))
                .toList();

        assertThat(unknown, hasSize(1));
        assertThat(unknown.get(0).getDetails(), containsString("sign.obj"));
    }

    @Test
    void sceneryLoadFailureDoesNotClaimMissingFile() throws Exception {
        String content = """
            Log.txt for X-Plane 12.1.1
            0:00:00.000 E/SCN: Unable to load 'Custom Scenery/Example Airport/bad.dsf': invalid format
            ----- X-Plane has shut down -----
            """;
        Files.writeString(logFile, content);

        List<InspectionMessage> messages = inspect();

        List<InspectionMessage> scenery = messages.stream()
                .filter(m -> m.getObject() != null && m.getObject().equals("Example Airport"))
                .toList();

        assertThat(scenery, hasSize(1));
        assertThat(scenery.get(0).getMessage(), containsString("could not be loaded"));
        assertThat(scenery.get(0).getMessage(), not(containsString("missing")));
    }

    @Test
    void repeatedSceneryAssetRetainsEvidenceWithoutGenericDuplicate() throws Exception {
        String content = """
            Log.txt for X-Plane 12.1.1
            0:00:00.000 E/SCN: Failed to find resource: 'Custom Scenery/Example Airport/objects/terminal.obj'
            0:00:01.000 W/APT: Airport has no runway definitions
            0:00:02.000 E/SCN: Failed to find resource: 'Custom Scenery/Example Airport/objects/terminal.obj'
            ----- X-Plane has shut down -----
            """;
        Files.writeString(logFile, content);

        List<InspectionMessage> messages = inspect();

        List<InspectionMessage> scenery = messages.stream()
                .filter(m -> m.getObject() != null && m.getObject().equals("Example Airport"))
                .toList();

        // Should be one grouped message with both occurrences
        assertThat(scenery, hasSize(1));
        assertThat(scenery.get(0).getMessage(), containsString("1 missing asset"));

        String details = scenery.get(0).getDetails();
        // Both line numbers should be present
        assertThat(details, containsString("line 2:"));
        assertThat(details, containsString("line 4:"));
        assertThat(details, containsString("terminal.obj"));

        // Unrelated warning should still be present
        List<InspectionMessage> warnings = messages.stream()
                .filter(m -> m.getSeverity() == Severity.WARN)
                .toList();
        assertThat(warnings, hasSize(1));
    }
}
