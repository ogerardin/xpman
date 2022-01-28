package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.acf.AcfFileData;
import com.ogerardin.xplane.file.data.acf.AcfProperty;
import lombok.extern.slf4j.Slf4j;
import org.petitparser.parser.Parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.petitparser.parser.primitive.CharacterParser.anyOf;
import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.CharacterParser.of;
import static org.petitparser.parser.primitive.StringParser.of;

@Slf4j
public class AcfFileParser extends XPlaneFileParserBase<AcfFileData> {

    static final String REQUIRED_TYPE = "ACF";

    /**
     * Matches a full X-Plane aircraft file.
     * Upon successful match, pushes an instance of {@link AcfFileData}
     */
    @Override
    public Parser XPlaneFile() {
        return Header(REQUIRED_TYPE)
                .seq(Newline().star())
                .seq(Properties())
                .map((List<Object> input) -> new AcfFileData((Header) input.get(0), (AcfFileData.AcfProperties) input.get(2)));
//                Junk(),
//                EOI
    }

    /**
     * Matches a "properties" section.
     * Upon successful match, pushes an instance of {{@link AcfFileData.AcfProperties}}
     */
    @SuppressWarnings({"unchecked"})
    Parser Properties() {
        return PropertiesBegin()
                .seq(Property().star())
                .seq(PropertiesEnd())

                .map((List<Object> input) -> {
                    List<AcfProperty> properties = (List<AcfProperty>) input.get(1);
                    Map<String, String> propertyMap = properties.stream().collect(Collectors.toMap(AcfProperty::getName, AcfProperty::getValue));
                    return new AcfFileData.AcfProperties(propertyMap);
                })
                ;
    }

    private Parser PropertiesEnd() {
        return of("PROPERTIES_END").seq(JunkLine()).flatten();
    }

    private Parser PropertiesBegin() {
        return of("PROPERTIES_BEGIN").seq(JunkLine()).flatten();
    }

    /**
     * Matches a line with property name and value.
     * Upon successful match, pushes a {@link AcfProperty} instance
     */
    Parser Property() {
        return of("P ")
                .seq(PropertyName())
                .seq(of(' '))
                .seq(PropertyValue())
                .seq(Newline())
                
                .map((List <String> input) -> new AcfProperty(input.get(1), input.get(3)));
    }

    /**
     * Matches a property value.
     * Upon successful match, pushes the value as a String.
     */
    Parser PropertyValue() {
        return noneOf("\r\n").plus().flatten();
    }

    /**
     * Matches a property name.
     * Upon successful match, pushes the name as a String.
     */
    Parser PropertyName() {
        return PropertyNameChar().plus().flatten();
    }

    Parser PropertyNameChar() {
        return Alphanumeric().or(anyOf("_,/"));
    }

}
