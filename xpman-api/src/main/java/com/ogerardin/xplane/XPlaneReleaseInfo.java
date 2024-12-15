package com.ogerardin.xplane;

import java.util.Optional;

public record XPlaneReleaseInfo(String version, Optional<String> releaseNotesUrl) {
}
