package com.ogerardin.xplane.config.navdata;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseNavDataSet extends NavDataSet {

    public BaseNavDataSet(Path folder, String name) {
        super(folder, name);
        List<NavDataFile> files = Stream.of(
                "earth_fix.dat",
                "earth_awy.dat",
                "earth_nav.dat",
                "earth_hold.dat",
                "earth_mora.dat",
                "earth_msa.dat"
        ).map(folder::resolve)
                .map(NavDataFile::new)
                .collect(Collectors.toList());
        setFiles(files);
    }
}
