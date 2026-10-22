package com.ogerardin.xplane.file.data.obj;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** An unrecognized or unsupported attribute line in an OBJ file. */
@EqualsAndHashCode(callSuper = true)
@Data
public class ObjUnknownAttr extends ObjAttribute {
    private final String attr;
}
