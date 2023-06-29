package com.ogerardin.xplane.test.petitparser;

import com.ogerardin.test.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.xplane.file.data.obj.ObjFileData;
import com.ogerardin.xplane.file.data.obj.ObjTexture;
import com.ogerardin.xplane.file.petitparser.ObjFileParser;
import org.junit.jupiter.api.Test;
import org.petitparser.parser.Parser;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@DisabledIfNoXPlaneRootFolder
class ObjFileParserTest extends ParserTest<ObjFileData> {

    @Test
    void testCanParseObj1() throws IOException {
        String fileContents = getXPlaneFileContents("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/EDDF_ASR_North01.obj");
        Parser parser = new ObjFileParser().getParser();
        ObjFileData result = runParser(fileContents, parser, false);
        assertThat(result.getAttributes(), hasItem(new ObjTexture("TEXTURE", "EDDF_806.dds")));
        assertThat(result.getAttributes(), hasItem(new ObjTexture("TEXTURE_LIT", "EDDF_806_lm.dds")));
    }

    @Test
    void testCanParseObj2() throws IOException {
        String fileContents = getXPlaneFileContents("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj");
        Parser parser = new ObjFileParser().getParser();
        ObjFileData result = runParser(fileContents, parser, false);
        assertThat(result.getAttributes(), hasItem(new ObjTexture("TEXTURE", "EDDF-236a.dds")));
        assertThat(result.getAttributes(), hasItem(new ObjTexture("TEXTURE_LIT", "EDDF-236a_lm.dds")));

    }

}