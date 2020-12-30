package com.ogerardin.xplane.navdata;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class XPNavDataSet extends NavDataSet {

    public static final String[] DEFAULT_FILES = {
            "earth_fix.dat",
            "earth_awy.dat",
            "earth_nav.dat",
            "earth_hold.dat",
            "earth_mora.dat",
            "earth_msa.dat"
    };

    public XPNavDataSet(String name, Path folder) {
        this(name, folder, DEFAULT_FILES);
    }

    public XPNavDataSet(String name, Path folder, String[] fileNames) {
        super(name, folder);
        List<NavDataFile> files = Arrays.stream(fileNames)
                .map(folder::resolve)
                .map(NavDataFile::new)
                .collect(Collectors.toList());
        setFiles(files);
    }
}
