package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua.FlyWithLuaInstallableType;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua.Flavor;
import com.ogerardin.xplane.util.zip.Archive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * The wizard's "archive identified as..." label reflects the edition embedded in the archive's .xpl.
 */
class FlyWithLuaInstallableTypeDescriptionTest {

    @TempDir
    Path tempDir;

    private final FlyWithLuaInstallableType type = new FlyWithLuaInstallableType();

    @Test
    void ngPlusArchiveDescribesItself() throws IOException {
        Archive archive = zipWith("FlyWithLua/64/FlyWithLua.xpl",
                binaryWith("FlyWithLua NG+"));
        assertThat(type.description(archive), is("FlyWithLua NG+ plugin"));
    }

    @Test
    void ngArchiveDescribesItself() throws IOException {
        Archive archive = zipWith("FlyWithLua/64/FlyWithLua.xpl",
                binaryWith("FlyWithLua NG"));
        assertThat(type.description(archive), is("FlyWithLua NG plugin"));
    }

    @Test
    void legacyArchiveDescribesItself() throws IOException {
        Archive archive = zipWith("FlyWithLua/64/FlyWithLua.xpl",
                binaryWith("FlyWithLua"));
        assertThat(type.description(archive), is("FlyWithLua plugin"));
    }

    @Test
    void archiveWithoutXplFallsBackToLegacy() throws IOException {
        Archive archive = zipWith("FlyWithLua/Scripts/something.lua", "-- empty");
        assertThat(type.description(archive), is("FlyWithLua plugin"));
    }

    /** Builds a zip archive with a single entry; returns an Archive view of it. */
    private Archive zipWith(String entryPath, String content) throws IOException {
        Path zipFile = tempDir.resolve("flywithlua.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zos.putNextEntry(new ZipEntry(entryPath));
            zos.write(content.getBytes());
            zos.closeEntry();
        }
        return new Archive() {
            @Override public boolean isValidArchive() { return true; }
            @Override public List<Path> getPaths() { return List.of(Path.of(entryPath)); }
            @Override public int entryCount() { return 1; }
            @Override public String getAsText(Path path) {
                if (path.equals(Path.of(entryPath))) return content;
                throw new RuntimeException("not found: " + path);
            }
            @Override public void extract(Path folder, java.util.function.Predicate<Path> filter,
                                          com.ogerardin.xplane.util.progress.ProgressListener progress) {}
            @Override public void extract(Path folder, Path subpath,
                                          com.ogerardin.xplane.util.progress.ProgressListener progress) {}
            @Override public void extract(Path folder,
                                          com.ogerardin.xplane.util.progress.ProgressListener progress) {}
            @Override public Path getSourcePath() { return zipFile; }
        };
    }

    private static String binaryWith(String pluginNameMarker) {
        return "binary junk " + pluginNameMarker + " more binary junk";
    }
}
