package com.ogerardin.xplane.file.data.obj;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

/** Parsing result for a .acf file */
@Getter
@Setter
@ToString(callSuper = true)
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
