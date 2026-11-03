package com.ogerardin.xplane.update;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Version discovery with a guarded, manual download-page action. */
@RequiredArgsConstructor
public abstract class PublicationUpdateProvider implements UpdateProvider {
    private final Supplier<String> installedVersion;
    private final BooleanSupplier enabled;
    private final AtomicReference<UpdateStatus> status = new AtomicReference<>(
            new UpdateStatus(UpdateStatus.State.UNKNOWN, null, null, List.of(), null));

    protected abstract String fetchLatestVersion() throws IOException, GeneralSecurityException;

    public abstract URL getUrl();

    @Override
    public synchronized void checkForUpdates() {
        if (!enabled.getAsBoolean()) return;
        String installed = installedVersion.get();
        try {
            String latest = fetchLatestVersion();
            var state = latest == null ? UpdateStatus.State.UNKNOWN
                    : Objects.equals(installed, latest) ? UpdateStatus.State.CURRENT : UpdateStatus.State.AVAILABLE;
            UpdateAction.DownloadPage action = () -> {
                if (!enabled.getAsBoolean()) throw new IOException("Add-on is disabled");
        var url = getUrl();
                if (url == null) throw new IOException("Download page is unavailable");
                return url;
            };
            status.set(new UpdateStatus(state, installed, latest,
                    state == UpdateStatus.State.AVAILABLE
                            ? List.of(new UpdateOffer(UpdateChannel.RELEASE, latest, action)) : List.of(), null));
        } catch (Exception e) {
            status.set(new UpdateStatus(UpdateStatus.State.FAILED, installed, null, List.of(), e.getMessage()));
        }
    }

    @Override
    public synchronized UpdateStatus getStatus() {
        var cached = status.get();
        return new UpdateStatus(enabled.getAsBoolean() ? cached.state() : UpdateStatus.State.DISABLED,
                installedVersion.get(), cached.latestVersion(), cached.offers(), cached.message());
    }
}
