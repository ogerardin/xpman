package com.ogerardin.xplane.file.data.obj;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ObjUnknownAttr extends ObjAttribute {
    private final String attr;
}
