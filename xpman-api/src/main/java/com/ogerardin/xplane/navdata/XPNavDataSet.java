package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import lombok.ToString;

import java.nio.file.Path;

/**
 * A {@link NavDataSet} in the XPNAV1150/XPFIX1101/XPAWY1100/XPHOLD1140 formats
 */
@ToString(callSuper = true, includeFieldNames = false)
public class XPNavDataSet extends NavDataSet {

    protected static final String[] DEFAULT_FILES = {
            "earth_fix.dat",
            "earth_awy.dat",
            "earth_nav.dat",
            "earth_hold.dat",
            "earth_mora.dat",
            "earth_msa.dat"
            //TODO consider CIFP/$ICAO.dat files
    };


    public XPNavDataSet(String name, String description, XPlane xPlane, Path folder) {
        this(name, description, xPlane, folder, DEFAULT_FILES);
    }

    public XPNavDataSet(String name, String description, XPlane xPlane, Path folder, String... fileNames) {
        super(name, description, xPlane, folder, fileNames);
    }
}
