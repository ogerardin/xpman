package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.util.platform.Platforms;

/**
 * A shared library that must be installed on the system for an add-on to run.
 * @param platform the platform the requirement applies to
 * @param soname the library name prefix to look for, e.g. {@code libopenal.so}
 * @param installHint how to install the library on that platform
 */
public record SystemLibraryRequirement(Platforms platform, String soname, String installHint) {}