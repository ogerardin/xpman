package com.ogerardin.xplane.config.navdata;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Arinc424DataSet extends NavDataSet {

    public Arinc424DataSet(String name, Path folder, String filename) {
        super(name, folder);
        setFiles(Stream.of(filename)
                .map(folder::resolve)
                .map(NavDataFile::new)
                .collect(Collectors.toList()));
    }

}
