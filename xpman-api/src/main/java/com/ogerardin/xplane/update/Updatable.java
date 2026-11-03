package com.ogerardin.xplane.update;

import com.ogerardin.xplane.Versioned;
import java.util.Optional;

/** An add-on whose update mechanism is supplied by composition. */
public interface Updatable extends Versioned {
    String getName();
    Optional<UpdateProvider> getUpdateProvider();

    default UpdateStatus getUpdateStatus() {
        return getUpdateProvider().map(UpdateProvider::getStatus)
                .orElseGet(() -> new UpdateStatus(UpdateStatus.State.UNSUPPORTED, getVersion(), null, java.util.List.of(), null));
    }

    default String getLatestVersion() { return getUpdateStatus().latestVersion(); }
    default String getInstalledVersion() { return getUpdateStatus().installedVersion(); }
    default boolean isUpdateAvailable() { return !getUpdateStatus().offers().isEmpty(); }
    default boolean isUpdateLocked() { return getUpdateStatus().state() == UpdateStatus.State.LOCKED; }
}
