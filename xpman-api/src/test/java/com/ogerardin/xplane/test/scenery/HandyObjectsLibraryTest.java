package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandyObjectsLibraryTest {

    @Test
    void recognizesHandyObjectsFolder(@TempDir Path folder) throws Exception {
        // Create the expected folder structure
        Path handyFolder = folder.resolve("The_Handy_Objects_Library");
        Files.createDirectories(handyFolder);
        Files.writeString(handyFolder.resolve("library.txt"), "EXPORT dummy.obj");
        
        // Create readme with version marker
        String readme = """
                The Handy Objects Library 1.0 was originally created by Scottish-Wings
                
                +++++ THE LATEST UPDATE IS 8.40 +++++
                
                Some other content here
                """;
        Files.writeString(handyFolder.resolve("aaa_Permission Note & Read Me.txt"), readme);

        var scenery = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, handyFolder);
        
        assertEquals("8.40", scenery.getVersion());
        assertTrue(scenery.getClass().getSimpleName().equals("HandyObjectsLibrary"));
    }

    @Test
    void fallsBackWhenReadmeMissing(@TempDir Path folder) throws Exception {
        Path handyFolder = folder.resolve("The_Handy_Objects_Library");
        Files.createDirectories(handyFolder);
        Files.writeString(handyFolder.resolve("library.txt"), "EXPORT dummy.obj");
        // No readme file

        var scenery = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, handyFolder);
        
        // Should fallback to super.getVersion() which returns null (no Skunkcrafts config)
        assertEquals(null, scenery.getVersion());
        assertTrue(scenery.getClass().getSimpleName().equals("HandyObjectsLibrary"));
    }

    @Test
    void fallsBackWhenVersionMarkerMissing(@TempDir Path folder) throws Exception {
        Path handyFolder = folder.resolve("The_Handy_Objects_Library");
        Files.createDirectories(handyFolder);
        Files.writeString(handyFolder.resolve("library.txt"), "EXPORT dummy.obj");
        
        // Readme without the version marker
        String readme = """
                The Handy Objects Library 1.0
                Some other content without the version marker
                """;
        Files.writeString(handyFolder.resolve("aaa_Permission Note & Read Me.txt"), readme);

        var scenery = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, handyFolder);
        
        assertEquals(null, scenery.getVersion());
    }

    @Test
    void doesNotRecognizeOtherFolders(@TempDir Path folder) throws Exception {
        Path otherFolder = folder.resolve("Some_Other_Scenery");
        Files.createDirectories(otherFolder);
        Files.writeString(otherFolder.resolve("library.txt"), "EXPORT dummy.obj");

        var scenery = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, otherFolder);
        
        // Should NOT be HandyObjectsLibrary
        assertTrue(!scenery.getClass().getSimpleName().equals("HandyObjectsLibrary"));
    }
}
