package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpfrSceneryTest {

    @Test
    void recognizesMultiLetterIdWithTrailingSuffix(@TempDir Path folder) throws Exception {
        Files.createFile(folder.resolve("FCHALOI.v.25-360_(146)_179.txt"));

        var scenery = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);
        assertEquals("25-360_(146)_179", scenery.getVersion());
        assertTrue(scenery.getLinks().get("Scenery detailed sheet on xpfr.org").toString()
                .contains("seek=FCHALOI.v.25-360_"));
    }

    @Test
    void stillRecognizesFourLetterId(@TempDir Path folder) throws Exception {
        Files.createFile(folder.resolve("LFBD.v.12-345_(67).txt"));

        assertEquals("12-345_(67)",
                IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder).getVersion());
    }
}
