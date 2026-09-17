package com.ogerardin.xplane.test.skunkcrafts;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.inspections.SkunkcraftsUpdatableInspection;
import com.ogerardin.xplane.util.zip.Archive;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

class SkunkcraftsUpdatableInspectionTest {

    @Test
    void testInspectReturnsInfoWhenConfigPresent() {
        Archive mockArchive = Mockito.mock(Archive.class);
        when(mockArchive.getPaths()).thenReturn(List.of(
                Path.of("root/"),
                Path.of("root/skunkcrafts_updater.cfg"),
                Path.of("root/plugin.xpl")
        ));

        InspectionResult result = SkunkcraftsUpdatableInspection.INSTANCE.inspect(mockArchive);

        assertThat(result.getMessages(), hasSize(1));
        assertThat(result.getMessages().get(0).getSeverity(), is(Severity.INFO));
        assertThat(result.getMessages().get(0).getMessage(), containsString("Skunkcrafts Updater"));
    }

    @Test
    void testInspectReturnsEmptyWhenNoConfig() {
        Archive mockArchive = Mockito.mock(Archive.class);
        when(mockArchive.getPaths()).thenReturn(List.of(
                Path.of("root/"),
                Path.of("root/plugin.xpl")
        ));

        InspectionResult result = SkunkcraftsUpdatableInspection.INSTANCE.inspect(mockArchive);

        assertThat(result.getMessages(), is(empty()));
    }

    @Test
    void testInspectReturnsInfoWhenConfigInSubdirectory() {
        Archive mockArchive = Mockito.mock(Archive.class);
        when(mockArchive.getPaths()).thenReturn(List.of(
                Path.of("root/"),
                Path.of("root/subdir/"),
                Path.of("root/subdir/skunkcrafts_updater.cfg"),
                Path.of("root/subdir/plugin.xpl")
        ));

        InspectionResult result = SkunkcraftsUpdatableInspection.INSTANCE.inspect(mockArchive);

        assertThat(result.getMessages(), hasSize(1));
        assertThat(result.getMessages().get(0).getSeverity(), is(Severity.INFO));
    }

    @Test
    void testInspectDoesNotMatchPartialFilename() {
        Archive mockArchive = Mockito.mock(Archive.class);
        when(mockArchive.getPaths()).thenReturn(List.of(
                Path.of("root/"),
                Path.of("root/skunkcrafts_updater.cfg.bak"),
                Path.of("root/plugin.xpl")
        ));

        InspectionResult result = SkunkcraftsUpdatableInspection.INSTANCE.inspect(mockArchive);

        assertThat(result.getMessages(), is(empty()));
    }
}
