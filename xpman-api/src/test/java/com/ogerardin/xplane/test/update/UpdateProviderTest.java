package com.ogerardin.xplane.test.update;

import com.ogerardin.xplane.update.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Provider discovery is explicit; presentation and execution honor live eligibility. */
class UpdateProviderTest {
    @Test
    void discoveryIsExplicitAndManualOffersAreGuarded() throws Exception {
        var requests = new AtomicInteger();
        var enabled = new AtomicBoolean(true);
        var provider = provider(requests, "2.0", enabled::get);
        assertEquals(UpdateStatus.State.UNKNOWN, provider.getStatus().state());
        assertEquals(0, requests.get());
        provider.checkForUpdates();
        var status = provider.getStatus();
        assertEquals(UpdateStatus.State.AVAILABLE, status.state());
        assertEquals(1, status.offers().size());
        assertEquals(UpdateChannel.RELEASE, status.offers().getFirst().channel());
        var action = (UpdateAction.DownloadPage) status.offers().getFirst().action();
        assertEquals("https://example.com/download", action.url().toString());
        assertEquals(1, requests.get());
        enabled.set(false);
        assertEquals(UpdateStatus.State.DISABLED, provider.getStatus().state());
        assertTrue(provider.getStatus().offers().isEmpty());
        assertThrows(IOException.class, action::url);
    }

    @Test
    void unavailableVersionIsNotReportedAsCurrent() {
        var provider = provider(new AtomicInteger(), null, () -> true);
        provider.checkForUpdates();
        assertEquals(UpdateStatus.State.UNKNOWN, provider.getStatus().state());
        assertTrue(provider.getStatus().offers().isEmpty());
    }

    @Test
    void equalVersionsAreCurrentAndFailuresAreDistinct() {
        var provider = provider(new AtomicInteger(), "1.0", () -> true);
        provider.checkForUpdates();
        assertEquals(UpdateStatus.State.CURRENT, provider.getStatus().state());
        var failing = new PublicationUpdateProvider(() -> "1.0", () -> true) {
            public String getName() { return "Broken"; }
            public URL getUrl() { return null; }
            protected String fetchLatestVersion() throws IOException { throw new IOException("offline"); }
        };
        failing.checkForUpdates();
        assertEquals(UpdateStatus.State.FAILED, failing.getStatus().state());
        assertTrue(failing.getStatus().offers().isEmpty());
        assertEquals("offline", failing.getStatus().message());
    }

    private PublicationUpdateProvider provider(AtomicInteger requests, String latest, java.util.function.BooleanSupplier enabled) {
        return new PublicationUpdateProvider(() -> "1.0", enabled) {
            public String getName() { return "Website"; }
            protected String fetchLatestVersion() { requests.incrementAndGet(); return latest; }
            public URL getUrl() {
                try { return URI.create("https://example.com/download").toURL(); }
                catch (Exception e) { throw new AssertionError(e); }
            }
        };
    }
}
