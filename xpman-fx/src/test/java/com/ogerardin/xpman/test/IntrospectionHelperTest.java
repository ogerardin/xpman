package com.ogerardin.xpman.test;

import com.ogerardin.xpman.panels.aircraft.UiAircraft;
import com.ogerardin.xpman.panels.plugins.UiPlugin;
import com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import com.ogerardin.xpman.util.jfx.menu.annotation.Order;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntrospectionHelperTest {

    @Test
    void labelExpressionReadsTheActionTarget() throws Exception {
        var target = new LabelFixture("Open download page");
        assertEquals("Open download page", IntrospectionHelper.getLabelForMethod(
                target.getClass().getMethod("update"), target));
        assertEquals("Update…", IntrospectionHelper.getLabelForMethod(
                target.getClass().getMethod("update"), new LabelFixture("Update…")));
    }

    @Test
    void quotedLabelRemainsLiteral() throws Exception {
        assertEquals("Uninstall", IntrospectionHelper.getLabelForMethod(
                LabelFixture.class.getMethod("uninstall"), new LabelFixture("Update…")));
    }

    @Test
    void missingLabelUsesHumanReadableMethodName() throws Exception {
        assertEquals("Open Manual", IntrospectionHelper.getLabelForMethod(
                LabelFixture.class.getMethod("openManual"), new LabelFixture("Update…")));
    }

    /** Actions with instance-based, literal, and inferred labels. */
    public record LabelFixture(String updateActionLabel) {
        public String getUpdateActionLabel() { return updateActionLabel; }

        @Label("updateActionLabel")
        public void update() { /* fixture action: only the @Label annotation is inspected */ }

        @Label("'Uninstall'")
        public void uninstall() { /* fixture action: only the @Label annotation is inspected */ }

        public void openManual() { /* fixture action: the method name supplies the inferred label */ }
    }

    @Test
    void relevantMethodsFollowExplicitOrder() {
        assertTrue(IntrospectionHelper.computeRelevantMethods(UiPlugin.class).stream()
                .allMatch(method -> method.isAnnotationPresent(Order.class)));
        assertEquals(
                List.of("reveal", "removeQuarantine", "enablePlugin", "disablePlugin", "uninstall",
                        "openLink", "openManual", "inspect", "update", "showUpdateLock", "properties"),
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
                        "details", "update", "showUpdateLock", "properties"),
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
        public void alpha() { /* fixture action: only the @Order annotation is inspected */ }

        @Order(20)
        public void beta() { /* fixture action: only the @Order annotation is inspected */ }

        @Order(10)
        public void zulu() { /* fixture action: only the @Order annotation is inspected */ }

        public void unannotatedZeta() { /* fixture action: unannotated methods sort after annotated ones */ }

        public void unannotatedAlpha() { /* fixture action: unannotated methods sort after annotated ones */ }
    }

}
