package com.ogerardin.xplane;

import java.util.Optional;

/** Version and optional release notes URL for an X-Plane release. */
public record XPlaneReleaseInfo(String version, Optional<String> releaseNotesUrl) {
}
