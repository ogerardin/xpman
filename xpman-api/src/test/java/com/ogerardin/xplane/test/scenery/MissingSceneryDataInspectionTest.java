package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.MissingSceneryDataInspection;
import com.ogerardin.xplane.scenery.SceneryPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("sceneryWithData")
    void sceneryWithDataIsNotFlagged(String name, String dataFile) throws IOException {
        Path folder = tempDir.resolve(name);
        Path file = folder.resolve(dataFile);
        Files.createDirectories(file.getParent());
        Files.createFile(file);
        assertThat(MissingSceneryDataInspection.INSTANCE.inspect(new SceneryPackage(folder)).isEmpty(), is(true));
    }

    static Stream<Arguments> sceneryWithData() {
        return Stream.of(
                Arguments.of("airport", "Earth nav data/apt.dat"),
                Arguments.of("library", "library.txt"),
                Arguments.of("objects", "Objects/foo.obj"),
                Arguments.of("mesh", "Earth nav data/area/tile.dsf")
        );
    }
}
