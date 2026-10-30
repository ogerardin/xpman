package com.ogerardin.xpman.test;

import com.ogerardin.xpman.panels.aircraft.UiAircraft;
import com.ogerardin.xpman.panels.plugins.UiPlugin;
import com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper;
import com.ogerardin.xpman.util.jfx.menu.annotation.Order;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntrospectionHelperTest {

    @Test
    void relevantMethodsFollowExplicitOrder() {
        assertTrue(IntrospectionHelper.computeRelevantMethods(UiPlugin.class).stream()
                .allMatch(method -> method.isAnnotationPresent(Order.class)));
        assertEquals(
                List.of("reveal", "removeQuarantine", "enablePlugin", "disablePlugin", "uninstall",
                        "openLink", "openManual", "inspect", "skunkcraftsUpdate", "skunkcraftsLocked", "properties"),
                IntrospectionHelper.computeRelevantMethods(UiPlugin.class).stream()
                        .map(Method::getName)
                        .toList());
    }

    @Test
    void uiAircraftMenuMethodsAreOnlyTheDeclaredActions() {
        assertTrue(IntrospectionHelper.computeRelevantMethods(UiAircraft.class).stream()
                .allMatch(method -> method.isAnnotationPresent(Order.class)));
        assertEquals(
                List.of("reveal", "uninstallVariant", "uninstall", "openLink", "openManual", "inspect",
                        "details", "skunkcraftsUpdate", "skunkcraftsLocked", "properties"),
                IntrospectionHelper.computeRelevantMethods(UiAircraft.class).stream()
                        .map(Method::getName)
                        .toList());
    }

    @Test
    void orderSortsMethodsAndUsesNameAsTieBreaker() {
        assertEquals(List.of("zulu", "alpha", "beta", "unannotatedAlpha", "unannotatedZeta"),
                IntrospectionHelper.computeRelevantMethods(OrderedFixture.class).stream()
                        .map(Method::getName)
                        .toList());
    }

    private static class OrderedFixture {
        @Order(20)
        public void alpha() {}

        @Order(20)
        public void beta() {}

        @Order(10)
        public void zulu() {}

        public void unannotatedZeta() {}

        public void unannotatedAlpha() {}
    }

}
