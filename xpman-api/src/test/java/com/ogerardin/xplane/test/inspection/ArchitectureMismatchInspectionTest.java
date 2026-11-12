package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.ArchitectureMismatchInspection;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.platform.Architecture;
import com.ogerardin.xplane.util.platform.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Warns only for the exact trigger case (x86_64-only plugin on Apple Silicon); permissive everywhere else.
 */
class ArchitectureMismatchInspectionTest {

    @TempDir
    Path tempDir;

    @Test
    void x86OnlyOnAppleSiliconProducesWarning() throws Exception {
        InspectionResult result = run(arches(Architecture.X86_64), true);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity().name(), is("WARN"));
        assertThat(result.get(0).getMessage(),
                containsString("plugin binary is for Intel Macs"));
        assertThat(result.get(0).getMessage(),
                containsString("unless started with Rosetta"));
    }

    @Test
    void arm64OnAppleSiliconIsSilent() throws Exception {
        assertThat(run(arches(Architecture.ARM64), true).isEmpty(), is(true));
    }

    @Test
    void universalOnAppleSiliconIsSilent() throws Exception {
        assertThat(run(arches(Architecture.X86_64, Architecture.ARM64), true)
                .isEmpty(), is(true));
    }

    @Test
    void x86OnIntelMacIsSilent() throws Exception {
        assertThat(run(arches(Architecture.X86_64), false).isEmpty(), is(true));
    }

    @Test
    void unknownBinaryIsSilent() throws Exception {
        // Permissive on doubt: an unrecognized file format produces no architectures → silent.
        assertThat(run(arches(), true).isEmpty(), is(true));
    }

    private InspectionResult run(List<Architecture> arches, boolean appleSilicon) throws Exception {
        Platform platform = mock(Platform.class);
        when(platform.getBinaryArchitectures(any())).thenReturn(arches);
        when(platform.isAppleSilicon()).thenReturn(appleSilicon);
        return ArchitectureMismatchInspection.INSTANCE.inspect(pluginWith("Foo"), platform);
    }

    private static List<Architecture> arches(Architecture... arches) {
        return List.of(arches);
    }

    private Plugin pluginWith(String folderName) throws Exception {
        Path folder = tempDir.resolve("Resources/plugins/" + folderName);
        Files.createDirectories(folder);
        Path xplFile = folder.resolve(folderName + ".xpl");
        Files.write(xplFile, new byte[0]);
        return new Plugin(new XPlane(tempDir), xplFile);
    }
}
