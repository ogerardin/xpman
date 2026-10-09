package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.inspections.WouldOverwriteInspection;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class WouldOverwriteInspectionTest {

    private final Archive archive = new ZipArchive(Path.of("unused.zip"));

    /** A target whose dry run yields the given result, or fails if {@code failure} is not null. */
    private static InstallTarget target(List<Path> overwrites, IOException failure) {
        return new InstallTarget() {
            @Override
            public void unpack(Archive archive, ProgressListener progressListener) {
                // Dry-run stub: this test only exercises overwrittenFiles().
            }

            @Override
            public void reload() {
                // Dry-run stub: this test only exercises overwrittenFiles().
            }

            @Override
            public List<Path> overwrittenFiles(Archive archive) throws IOException {
                if (failure != null) {
                    throw failure;
                }
                return overwrites;
            }
        };
    }

    @Test
    void noMessageWhenNothingWouldBeOverwritten() {
        InspectionResult result = new WouldOverwriteInspection(target(List.of(), null)).inspect(archive);

        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void warnsWithCountAndExample() {
        List<Path> overwrites = List.of(Path.of("/xp/Aircraft/A/a.acf"), Path.of("/xp/Aircraft/A/b.txt"));

        InspectionResult result = new WouldOverwriteInspection(target(overwrites, null)).inspect(archive);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.WARN));
        assertThat(result.get(0).getMessage(), containsString("2 existing file(s)"));
        assertThat(result.get(0).getMessage(), containsString("/xp/Aircraft/A/a.acf"));
    }

    @Test
    void reportsErrorWhenDryRunFails() {
        IOException failure = new IOException("Invalid plugin archive");

        InspectionResult result = new WouldOverwriteInspection(target(List.of(), failure)).inspect(archive);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.ERROR));
        assertThat(result.get(0).getMessage(), containsString("Invalid plugin archive"));
    }
}
