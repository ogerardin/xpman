package com.ogerardin.xplane.test.aircraft;

import com.google.api.services.drive.model.File;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.aircraft.custom.ZiboMod738;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(TimingExtension.class)
class ZiboMod738Test {

    private static File file(String name) {
        return new File().setName(name);
    }

    @Test
    void extractLatestVersion_newXp12Layout() {
        List<File> files = List.of(
                file("B737-800X_XP12_4_05_full.zip"),
                file("B738X_XP12_4_05_01.zip"),
                file("B738X_XP12_4_05_35.zip"),
                file("B738X_XP12_4_05_07.zip"),
                file("ZIBOmod installation guide.pdf"),
                file("XP12"),
                file("XP11")
        );
        assertThat(ZiboMod738.GoogleDriveChannel.extractLatestVersion(files), is("4.05.35"));
    }

    @Test
    void extractLatestVersion_newXp11Layout() {
        List<File> files = List.of(
                file("B737-800X_XP11_4_00_rc2_0_full.zip"),
                file("B738X_XP11_4_00_rc5_7.zip"),
                file("XP12"),
                file("XP11")
        );
        // rc5_7 patch version-part ("4_00_rc5") doesn't match the full release version-part ("4_00_rc2_0"),
        // so the patch is not applied; latest = full version only.
        assertThat(ZiboMod738.GoogleDriveChannel.extractLatestVersion(files), is("4.00.rc2.0"));
    }

    @Test
    void extractLatestVersion_legacyLayout() {
        List<File> files = List.of(
                file("B737-800X_3_42_full.zip"),
                file("B738X_3_42_10.zip"),
                file("B738X_3_42_03.zip")
        );
        assertThat(ZiboMod738.GoogleDriveChannel.extractLatestVersion(files), is("3.42.10"));
    }

    @Test
    void extractLatestVersion_noFullFile_returnsNull() {
        List<File> files = List.of(
                file("B738X_XP12_4_05_35.zip"),
                file("ZIBOmod installation guide.pdf")
        );
        assertThat(ZiboMod738.GoogleDriveChannel.extractLatestVersion(files), is(nullValue()));
    }

    @Test
    void extractLatestVersion_fullOnly_noPatch() {
        List<File> files = List.of(
                file("B737-800X_XP12_4_05_full.zip"),
                file("ZIBOmod installation guide.pdf")
        );
        assertThat(ZiboMod738.GoogleDriveChannel.extractLatestVersion(files), is("4.05"));
    }
}