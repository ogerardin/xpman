package com.ogerardin.xplane.aircrafts;

import lombok.Data;

import java.nio.file.Path;

@Data
public class Livery {

    private final Aircraft aircraft;

    /** path to livery folder (relative to .acf file) */
    private final Path path;

    public Livery(Aircraft aircraft, Path path) {
        this.aircraft = aircraft;
        this.path = aircraft.getAcfFile().getFile().relativize(path);
    }

    public String getName() {
        return path.getFileName().toString();
    }

}
