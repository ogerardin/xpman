package com.ogerardin.xplane.test.update;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.test.skunkcrafts.SkunkcraftsServerFixture;
import com.ogerardin.xplane.update.UpdateStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Managers publish loaded items only after provider discovery has populated their caches. */
class UpdateLoadingTest {
    @TempDir Path root;

    @Test
    void loadedEventIncludesDiscoveredStatusAndRenderingUsesCache() throws Exception {
        var manager = new XPlane(root).getSceneryManager();
        var loaded = new AtomicReference<UpdateStatus.State>();
        try (var server = SkunkcraftsServerFixture.start()) {
            var folder = root.resolve("Custom Scenery/Test");
            Files.createDirectories(folder);
            Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "module|" + server.baseUrl() + "\nversion|1\n");
            server.serve("skunkcrafts_updater.cfg", "version|2\n")
                    .serve("skunkcrafts_updater_whitelist.txt", "missing.txt||3\n");
            manager.registerListener(event -> {
                if (event.getType() == ManagerEvent.Type.LOADED) loaded.set(
                        event.getItems().getFirst().getSceneryPackage().getUpdateStatus().state());
            });
            manager.loadPackages();
            assertEquals(UpdateStatus.State.AVAILABLE, loaded.get());
        }
        assertTrue(manager.getSceneryPackages().getFirst().isUpdateAvailable());
    }
}
