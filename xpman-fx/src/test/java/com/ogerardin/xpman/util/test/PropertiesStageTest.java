package com.ogerardin.xpman.util.test;

import com.ogerardin.xpman.util.jfx.PropertiesStage;
import com.ogerardin.xpman.panels.aircraft.UiAircraft;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesStageTest {

    public static class SampleBean {
        private final AtomicInteger callCount = new AtomicInteger(0);

        public String getNormalValue() {
            callCount.incrementAndGet();
            return "hello";
        }

        public String getThrows() {
            callCount.incrementAndGet();
            throw new RuntimeException("boom");
        }

        public int getCallCount() {
            return callCount.get();
        }

        public String getCallsign() {
            return "OLD123";
        }
    }

    @Test
    void testPropertiesStageCreation() throws Exception {
        SampleBean bean = new SampleBean();
        
        // Access the internal ReadOnlyPropertyItem via reflection by creating stage and inspecting
        // Easier: just verify the stage can be created without errors
        // But we need to test the caching behavior
        Method getCachedMethod = PropertiesStage.class.getDeclaredMethod(
                "getCachedValue", 
                Object.class, 
                java.beans.PropertyDescriptor.class, 
                java.util.Map.class
        );
        getCachedMethod.setAccessible(true);
        
        PropertyDescriptor normalPd = new PropertyDescriptor("normalValue", SampleBean.class, "getNormalValue", null);
        Map<String, Object> cache = new HashMap<>();
        
        // First call
        Object result1 = getCachedMethod.invoke(null, bean, normalPd, cache);
        assertEquals("hello", result1);
        assertEquals(1, bean.getCallCount());
        
        // Second call should use cache
        Object result2 = getCachedMethod.invoke(null, bean, normalPd, cache);
        assertEquals("hello", result2);
        assertEquals(1, bean.getCallCount()); // Still 1 - cached
    }
    
    @Test
    void testThrowingGetter() throws Exception {
        SampleBean bean = new SampleBean();
        Method getCachedMethod = PropertiesStage.class.getDeclaredMethod(
                "getCachedValue", 
                Object.class, 
                java.beans.PropertyDescriptor.class, 
                java.util.Map.class
        );
        getCachedMethod.setAccessible(true);
        
        PropertyDescriptor throwsPd = new PropertyDescriptor("throws", SampleBean.class, "getThrows", null);
        Map<String, Object> cache = new HashMap<>();
        
        Object result = getCachedMethod.invoke(null, bean, throwsPd, cache);
        assertTrue(result != null);
        assertTrue(result.toString().length() > 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    void editableItemsCollectChangesUntilSave() throws Exception {
        Method buildItems = PropertiesStage.class.getDeclaredMethod(
                "buildItems", Object.class, Map.class, Map.class);
        buildItems.setAccessible(true);
        Map<String, String> pending = new HashMap<>();
        var items = (javafx.collections.ObservableList<org.controlsfx.control.PropertySheet.Item>)
                buildItems.invoke(null, new SampleBean(), Map.of("callsign", "acf/_callsign"), pending);
        var callsign = items.stream().filter(item -> item.getName().equals("Callsign")).findFirst().orElseThrow();

        assertTrue(callsign.isEditable());
        callsign.setValue("NEW123");
        assertEquals(Map.of("acf/_callsign", "NEW123"), pending);

        callsign.setValue("OLD123");
        assertTrue(pending.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void readOnlyItemsAreEnabledForSelectionButIgnoreValueChanges() throws Exception {
        Method buildItems = PropertiesStage.class.getDeclaredMethod(
                "buildItems", Object.class, Map.class, Map.class);
        buildItems.setAccessible(true);
        var items = (javafx.collections.ObservableList<org.controlsfx.control.PropertySheet.Item>)
                buildItems.invoke(null, new SampleBean(), Map.of(), new HashMap<>());
        var property = items.stream().filter(item -> item.getCategory().equals("normalValue")).findFirst().orElseThrow();

        assertTrue(property.isEditable());
        property.setValue("changed");
        assertEquals("hello", property.getValue());
    }

    @Test
    void editableItemsUseDefaultEditor() throws Exception {
        assertTrue(usesDefaultEditor("callsign", Map.of("callsign", "acf/_callsign")));
    }

    @Test
    void readOnlyItemsUseSelectableEditor() throws Exception {
        assertFalse(usesDefaultEditor("normalValue", Map.of()));
    }

    @SuppressWarnings("unchecked")
    private static boolean usesDefaultEditor(String propCategory, Map<String, String> editableProperties)
            throws Exception {
        Method buildItems = PropertiesStage.class.getDeclaredMethod(
                "buildItems", Object.class, Map.class, Map.class);
        buildItems.setAccessible(true);
        var items = (javafx.collections.ObservableList<org.controlsfx.control.PropertySheet.Item>)
                buildItems.invoke(null, new SampleBean(), editableProperties, new HashMap<>());
        var item = items.stream()
                .filter(i -> i.getCategory().equals(propCategory)).findFirst().orElseThrow();

        Method usesDefaultEditor = PropertiesStage.class.getDeclaredMethod(
                "usesDefaultEditor", org.controlsfx.control.PropertySheet.Item.class);
        usesDefaultEditor.setAccessible(true);
        return (boolean) usesDefaultEditor.invoke(null, item);
    }

    @Test
    void aircraftPropertiesActionReloadsAfterItReturns() throws Exception {
        var onSuccess = java.util.Arrays.stream(UiAircraft.class.getMethod("properties").getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().getName()
                        .equals("com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess"))
                .findFirst();

        assertTrue(onSuccess.isPresent());
        assertTrue(onSuccess.get().toString().contains("reload()"));
    }
}
