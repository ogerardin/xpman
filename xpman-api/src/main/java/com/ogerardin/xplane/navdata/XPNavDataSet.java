package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;

import java.nio.file.Path;

/**
 * A {@link NavDataSet} in the XPNAV1150/XPFIX1101/XPAWY1100/XPHOLD1140 formats
 */
public class XPNavDataSet extends NavDataSet {

    public static final String[] DEFAULT_FILES = {
            "earth_fix.dat",
            "earth_awy.dat",
            "earth_nav.dat",
            "earth_hold.dat",
            "earth_mora.dat",
            "earth_msa.dat"
    };

    public XPNavDataSet(String name, XPlane xPlane, Path folder) {
        this(name, xPlane, folder, DEFAULT_FILES);
    }

    public XPNavDataSet(String name, XPlane xPlane, Path folder, String... fileNames) {
        super(name, xPlane, folder, fileNames);
    }
}
