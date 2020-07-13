package com.ogerardin.xplane.file.data;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Parsing result for a .acf file */
@Getter
@Setter
@ToString
public class ObjFileData extends XPlaneFileData {

    ObjAttributes attributes = new ObjAttributes();

    ObjData data = new ObjData();

    ObjCommands commands = new ObjCommands();

    public ObjFileData(Header header) {
        super(header);
    }

    public static class ObjAttributes extends ArrayList<ObjAttribute> {}

    public static class ObjData extends ArrayList<ObjDatum> {}

    public static class ObjCommands extends ArrayList<ObjCommand> {}

}
