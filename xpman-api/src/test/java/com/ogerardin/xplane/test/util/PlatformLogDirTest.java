package com.ogerardin.xplane.test.util;

import com.ogerardin.xplane.util.platform.LinuxPlatform;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.ogerardin.xplane.util.platform.UnknownPlatform;
import com.ogerardin.xplane.util.platform.WindowsPlatform;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PlatformLogDirTest {

    private final Path userHome = Path.of("test-home");

    @Test
    void macLogsUseLibraryLogs() {
        assertThat(new MacPlatform().getLogDir(userHome), is(userHome.resolve("Library/Logs/XPman")));
    }

    @Test
    void windowsLogsUseLocalAppDataOrHomeFallback() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = localAppData == null || localAppData.isBlank()
                ? userHome.resolve("AppData/Local")
                : Path.of(localAppData);
        assertThat(new WindowsPlatform().getLogDir(userHome), is(base.resolve("XPman/logs")));
    }

    @Test
    void linuxLogsUseXdgStateHomeOrHomeFallback() {
        String stateHome = System.getenv("XDG_STATE_HOME");
        Path base = stateHome == null || stateHome.isBlank()
                ? userHome.resolve(".local/state")
                : Path.of(stateHome);
        assertThat(new LinuxPlatform().getLogDir(userHome), is(base.resolve("XPman/logs")));
    }

    @Test
    void unknownPlatformUsesDotDirectoryFallback() {
        assertThat(new UnknownPlatform().getLogDir(userHome), is(userHome.resolve(".xpman/logs")));
    }
}
