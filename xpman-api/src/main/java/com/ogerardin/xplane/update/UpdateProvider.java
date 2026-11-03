package com.ogerardin.xplane.update;

/** Discovers updates off the UI thread and exposes cached, eligibility-filtered results. */
public interface UpdateProvider {
    String getName();
    void checkForUpdates();
    UpdateStatus getStatus();
}
