package com.ogerardin.xplane.file.data.obj;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** A texture reference (type and path) parsed from an OBJ file. */
@EqualsAndHashCode(callSuper = false)
@Data
public class ObjTexture extends ObjAttribute {

    private final String type;

    private final String reference;

}
