package com.ogerardin.xplane.navdata;

import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class NavDataFile implements NavDataItem {

    private final Path file;

    @Override
    public String getName() {
        return file.toString();
    }

    @Override
    public Boolean getExists() {
        return Files.exists(file);
    }
}
