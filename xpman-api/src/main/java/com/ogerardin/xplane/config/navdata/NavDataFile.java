package com.ogerardin.xplane.config.navdata;

import lombok.Data;

import java.nio.file.Path;

@Data
public class NavDataFile implements NavDataItem {

    private final Path file;

    @Override
    public String getName() {
        return file.toString();
    }
}
