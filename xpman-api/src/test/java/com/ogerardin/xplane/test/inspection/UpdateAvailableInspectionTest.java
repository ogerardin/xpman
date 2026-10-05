package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.UpdateAvailableInspection;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class UpdateAvailableInspectionTest {

    private SkunkcraftsUpdatable updatable(String name, String version, String latestVersion) {
        return new SkunkcraftsUpdatable() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public Path getSkunkcraftsFolder() {
                return null;
            }

            @Override
            public SkunkcraftsConfig getSkunkcraftsConfig() {
                return null;
            }

            @Override
            public String getSkunkcraftsLatestVersion() {
                return latestVersion;
            }

            @Override
            public String getLatestVersion() {
                return latestVersion;
            }
        };
    }

    @Test
    void upToDate() {
        SkunkcraftsUpdatable target = updatable("MyAddon", "1.0.0", "1.0.0");
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void updateAvailable() {
        SkunkcraftsUpdatable target = updatable("MyAddon", "1.0.0", "2.0.0");
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).isError(), is(false));
        assertThat(result.get(0).getSeverity().name(), is("WARN"));
        assertThat(result.get(0).getObject(), is("MyAddon"));
        assertThat(result.get(0).getMessage(), is("Update available: 2.0.0"));
    }

    @Test
    void noLatestVersion() {
        SkunkcraftsUpdatable target = updatable("MyAddon", "1.0.0", null);
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.isEmpty(), is(true));
    }
}
