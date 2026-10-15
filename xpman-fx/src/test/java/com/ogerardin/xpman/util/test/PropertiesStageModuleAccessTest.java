package com.ogerardin.xpman.util.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Reproduces the JPMS access failure the Properties dialog hits when invoking, from module
 * xpman.fx, getters declared on domain subclasses living in non-exported api packages
 * (e.g. XpfrAircraft.getId/getVersion).
 */
class PropertiesStageModuleAccessTest {

    @Test
    void invokesGettersDeclaredOnCustomAircraftSubclasses(@TempDir Path xplaneRoot) throws Exception {
        Path folder = xplaneRoot.resolve("Aircraft/Test");
        createAcf(folder);
        Files.createFile(folder.resolve("AlphaJet-AJET.v.17-345_(33)_179.txt"));

        Aircraft aircraft = IntrospectionHelper.getBestSubclassInstance(
                Aircraft.class, new XPlane(xplaneRoot), new AcfFile(folder.resolve("test.acf")));

        Map<String, Object> values = invokeAllGetters(aircraft);
        assertEquals("AJET.v.17-345_(33)_179", values.get("id"));
        assertEquals("17-345_(33)_179", values.get("version"));
    }

    @Test
    void invokesGettersDeclaredOnCustomScenerySubclasses(@TempDir Path folder) throws Exception {
        Files.createFile(folder.resolve("FCHALOI.v.25-360_(146)_179.txt"));

        SceneryPackage scenery = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);

        Map<String, Object> values = invokeAllGetters(scenery);
        assertEquals("FCHALOI.v.25-360_(146)_179", values.get("id"));
        assertEquals("25-360_(146)_179", values.get("version"));
    }

    /**
     * Invokes every readable bean property exactly like PropertiesStage.buildItems does;
     * throws IllegalAccessException today for getters declared in non-exported packages.
     */
    private static Map<String, Object> invokeAllGetters(Object target) throws Exception {
        Map<String, Object> values = new HashMap<>();
        for (var pd : Introspector.getBeanInfo(target.getClass(), Object.class).getPropertyDescriptors()) {
            Method readMethod = pd.getReadMethod();
            if (readMethod == null || "xPlane".equals(pd.getName()) || "class".equals(pd.getName())) {
                continue;
            }
            values.put(pd.getName(), assertDoesNotThrow(() -> readMethod.invoke(target)));
        }
        return values;
    }

    private static void createAcf(Path folder) throws IOException {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("test.acf"), """
                I
                1200 Version
                ACF
                PROPERTIES_BEGIN
                P acf/_studio Test Studio
                PROPERTIES_END
                """);
    }
}
