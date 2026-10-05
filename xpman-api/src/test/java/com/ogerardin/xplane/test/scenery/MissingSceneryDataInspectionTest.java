package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.MissingSceneryDataInspection;
import com.ogerardin.xplane.scenery.SceneryPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class MissingSceneryDataInspectionTest {

    @TempDir
    Path tempDir;

    private SceneryPackage pkg(String name) throws IOException {
        Path folder = tempDir.resolve(name);
        Files.createDirectories(folder);
        return new SceneryPackage(folder);
    }

    @Test
    void emptyFolderIsFlagged() throws IOException {
        SceneryPackage pkg = pkg("empty");
        InspectionResult result = MissingSceneryDataInspection.INSTANCE.inspect(pkg);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).isError(), is(true));
        assertThat(result.get(0).getMessage(), containsString("No scenery data"));
    }

    @Test
    void folderWithOnlyEmptyObjectsIsFlagged() throws IOException {
        Path folder = tempDir.resolve("husk");
        Files.createDirectories(folder);
        Files.createDirectory(folder.resolve("Objects"));
        SceneryPackage pkg = new SceneryPackage(folder);
        InspectionResult result = MissingSceneryDataInspection.INSTANCE.inspect(pkg);
        assertThat(result.size(), is(1));
    }

    @Test
    void airportIsNotFlagged() throws IOException {
        Path folder = tempDir.resolve("airport");
        Files.createDirectories(folder.resolve("Earth nav data"));
        Files.createFile(folder.resolve("Earth nav data/apt.dat"));
        SceneryPackage pkg = new SceneryPackage(folder);
        assertThat(MissingSceneryDataInspection.INSTANCE.inspect(pkg).isEmpty(), is(true));
    }

    @Test
    void libraryIsNotFlagged() throws IOException {
        Path folder = tempDir.resolve("library");
        Files.createDirectories(folder);
        Files.createFile(folder.resolve("library.txt"));
        SceneryPackage pkg = new SceneryPackage(folder);
        assertThat(MissingSceneryDataInspection.INSTANCE.inspect(pkg).isEmpty(), is(true));
    }

    @Test
    void objFileIsNotFlagged() throws IOException {
        Path folder = tempDir.resolve("objects");
        Files.createDirectories(folder.resolve("Objects"));
        Files.createFile(folder.resolve("Objects/foo.obj"));
        SceneryPackage pkg = new SceneryPackage(folder);
        assertThat(MissingSceneryDataInspection.INSTANCE.inspect(pkg).isEmpty(), is(true));
    }

    @Test
    void dsfTileIsNotFlagged() throws IOException {
        Path folder = tempDir.resolve("mesh");
        Files.createDirectories(folder.resolve("Earth nav data/area"));
        Files.createFile(folder.resolve("Earth nav data/area/tile.dsf"));
        SceneryPackage pkg = new SceneryPackage(folder);
        assertThat(MissingSceneryDataInspection.INSTANCE.inspect(pkg).isEmpty(), is(true));
    }
}