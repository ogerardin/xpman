package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.XPlane;
import lombok.ToString;

import java.nio.file.Path;

/**
 * A {@link NavDataSet} in the ARINC424 format
 */
@ToString(callSuper = true, includeFieldNames = false)
public class Arinc424DataSet extends NavDataSet {

    public Arinc424DataSet(String name, String description, XPlane xPlane, Path folder, String filename) {
        super(name, description, xPlane, folder, filename);
    }

}
