package com.ogerardin.xpman.test;

import com.ogerardin.xplane.update.*;
import com.ogerardin.xpman.util.jfx.UpdateUtil;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Browser hand-off is not an installation; only successful direct execution refreshes items. */
class UpdateActionTest {
    @Test
    void downloadPageOpensWithoutReloadOrInstallationSuccess() throws Exception {
        var url = URI.create("https://example.com/download").toURL();
        var opened = new AtomicReference<java.net.URL>();
        var reloaded = new AtomicBoolean();
        UpdateAction.DownloadPage action = () -> url;
        UpdateUtil.execute(action, (p, m) -> {}, opened::set, () -> reloaded.set(true));
        assertEquals(url, opened.get());
        assertFalse(reloaded.get());
        assertEquals("Open download page", UpdateUtil.actionLabel(List.of(new UpdateOffer(UpdateChannel.RELEASE, "2", action))));
    }

    @Test
    void directExecutionReloadsOnlyOnSuccess() throws Exception {
        var installed = new AtomicBoolean();
        var reloaded = new AtomicBoolean();
        UpdateUtil.execute((UpdateAction.Direct) p -> installed.set(true), (p, m) -> {},
                u -> fail("Not a browser action"), () -> reloaded.set(installed.get()));
        assertTrue(reloaded.get());
        reloaded.set(false);
        assertThrows(IOException.class, () -> UpdateUtil.execute((UpdateAction.Direct) p -> { throw new IOException("failed"); },
                (p, m) -> {}, u -> fail(), () -> reloaded.set(true)));
        assertFalse(reloaded.get());
    }
}
