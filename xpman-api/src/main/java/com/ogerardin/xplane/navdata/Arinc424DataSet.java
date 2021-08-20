package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;

import java.nio.file.Path;

/**
 * A {@link NavDataSet} in the ARINC424 format
 */
public class Arinc424DataSet extends NavDataSet {

    public Arinc424DataSet(String name, XPlane xPlane, Path folder, String filename) {
        super(name, xPlane, folder, filename);
    }

}
