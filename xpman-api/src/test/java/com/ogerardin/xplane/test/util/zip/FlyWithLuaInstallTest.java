package com.ogerardin.xplane.test.util.zip;

import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.inspection.InspectionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test for installing FlyWithLua NG archive.
 */
class FlyWithLuaInstallTest {

    private static final String FLYWITHLUA_ZIP = "/Users/olivier/Downloads/FlyWithLua_NG_Lin_Mac_Win.zip";

    @Test
    @EnabledIf("flyWithLuaZipExists")
    void testFlyWithLuaArchiveRecognition() {
        Path zipPath = Paths.get(FLYWITHLUA_ZIP);
        ArchiveInstallSource source = ArchiveInstallSource.ofZip(zipPath);
        
        InspectionResult result = source.inspect();
        
        System.out.println("Inspection result: " + result);
        
        assertThat("Archive should be recognized as installable", 
            result.isNotAbort(), is(true));
    }

    static boolean flyWithLuaZipExists() {
        return Files.exists(Paths.get(FLYWITHLUA_ZIP));
    }
}
