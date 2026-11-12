package com.ogerardin.xplane.test.plugins;

import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Flavor detection from binary content — the three PLUGIN_NAME strings FlyWithLua ships.
 */
class FlyWithLuaFlavorTest {

    @Test
    void ngPlusFlavorIsDetectedFirst() {
        // NG+ contains "FlyWithLua NG", so order matters.
        String binary = "junk #define PLUGIN_NAME \"FlyWithLua NG+\" more junk";
        assertThat(FlyWithLua.Flavor.of(binary), is(FlyWithLua.Flavor.NG_PLUS));
    }

    @Test
    void ngFlavorIsDetected() {
        String binary = "junk #define PLUGIN_NAME \"FlyWithLua NG\" more junk";
        assertThat(FlyWithLua.Flavor.of(binary), is(FlyWithLua.Flavor.NG));
    }

    @Test
    void legacyFlavorFallsThrough() {
        // Old binaries have PLUGIN_NAME "FlyWithLua" but not "FlyWithLua NG" / "FlyWithLua NG+".
        String binary = "junk #define PLUGIN_NAME \"FlyWithLua\" more junk";
        assertThat(FlyWithLua.Flavor.of(binary), is(FlyWithLua.Flavor.LEGACY));
    }

    @Test
    void emptyOrUnknownContentDefaultsToLegacy() {
        assertThat(FlyWithLua.Flavor.of(""), is(FlyWithLua.Flavor.LEGACY));
        assertThat(FlyWithLua.Flavor.of("not a FlyWithLua binary"), is(FlyWithLua.Flavor.LEGACY));
    }

    @Test
    void flavorLabelsMatchExpectedDisplayNames() {
        assertThat(FlyWithLua.Flavor.LEGACY.label(), is("FlyWithLua"));
        assertThat(FlyWithLua.Flavor.NG.label(), is("FlyWithLua NG"));
        assertThat(FlyWithLua.Flavor.NG_PLUS.label(), is("FlyWithLua NG+"));
    }
}
