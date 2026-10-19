package com.ogerardin.xplane.test.util;

import com.ogerardin.xplane.util.Versions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the Versions utility class.
 */
class VersionsTest {

    @Test
    void comparesEqualVersions() {
        assertThat(Versions.compare("12.4.4", "12.4.4"), is(0));
        assertThat(Versions.compare("12.4.0", "12.4.0"), is(0));
    }

    @Test
    void comparesDifferentVersions() {
        assertThat(Versions.compare("12.4.5", "12.4.4"), is(1));
        assertThat(Versions.compare("12.4.3", "12.4.4"), is(-1));
        assertThat(Versions.compare("12.5.0", "12.4.4"), is(1));
        assertThat(Versions.compare("12.3.9", "12.4.4"), is(-1));
    }

    @Test
    void handlesMajorVersionDifference() {
        assertThat(Versions.compare("13.0.0", "12.4.4"), is(1));
        assertThat(Versions.compare("11.0.0", "12.4.4"), is(-1));
    }

    @Test
    void handlesLegacyVersionFormat() {
        // Legacy format "12.04r3" = 12.0.4
        assertThat(Versions.compare("12.04r3", "12.0.4"), is(0));
        assertThat(Versions.compare("12.04r3", "12.0.3"), is(1));
        assertThat(Versions.compare("12.04r3", "12.0.5"), is(-1));
    }

    @Test
    void handlesMissingTrailingComponents() {
        // "12.4" should be treated as "12.4.0"
        assertThat(Versions.compare("12.4", "12.4.0"), is(0));
        assertThat(Versions.compare("12.4.1", "12.4"), is(1));
        assertThat(Versions.compare("12.4", "12.4.1"), is(-1));
    }

    @Test
    void comparesNumericComponents() {
        // "12.10" should be greater than "12.9" (numeric comparison)
        assertThat(Versions.compare("12.10", "12.9"), is(1));
        assertThat(Versions.compare("12.9", "12.10"), is(-1));
    }

    @Test
    void handlesComplexVersionStrings() {
        // Beta/suffix versions
        assertThat(Versions.compare("12.4.4-b1", "12.4.4"), is(0));
        assertThat(Versions.compare("12.4.4", "12.4.4-rc1"), is(0));
    }
}
