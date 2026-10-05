package com.ogerardin.xplane.test.util;

import com.ogerardin.xplane.util.platform.LinuxPlatform;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Versioned soname matching in {@link LinuxPlatform#isPresent}.
 */
class LinuxPlatformTest {

    private static final List<String> LDCACHE = List.of(
            "linux-vdso.so.1 (0x00007ffd8b5f9000)",
            "libc.so.6 (libc6,x86-64) => /lib/x86_64-linux-gnu/libc.so.6",
            "libglut.so.3 (libglut6) => /lib/x86_64-linux-gnu/libglut.so.3"
    );

    @Test
    void isPresentMatchesVersionedSoname() {
        assertThat(LinuxPlatform.isPresent(LDCACHE, "libglut.so"), is(true));
    }

    @Test
    void isPresentRejectsAbsentLibrary() {
        assertThat(LinuxPlatform.isPresent(LDCACHE, "libopenal.so"), is(false));
    }

    @Test
    void isPresentAssumesPresentWhenProbeUnavailable() {
        assertThat(LinuxPlatform.isPresent(null, "libopenal.so"), is(true));
    }
}