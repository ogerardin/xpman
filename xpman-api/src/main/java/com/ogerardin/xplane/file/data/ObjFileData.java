package com.ogerardin.xplane.file.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** Parsing result for a .acf file */
@Getter
@Setter
@ToString
public class ObjFileData extends XPlaneFileData {

    final ObjAttributes attributes;

    final ObjData data;

    final ObjCommands commands;

    public ObjFileData(Header header, ObjAttributes attributes, ObjData data, ObjCommands commands) {
        super(header);
        this.attributes = attributes;
        this.data = data;
        this.commands = commands;
    }

    public static class ObjAttributes extends ArrayList<ObjAttribute> {}

    public static class ObjData extends ArrayList<ObjDatum> {}

    public static class ObjCommands extends ArrayList<ObjCommand> {}

}
