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

/**
 * Parser for X-Plane ACF (aircraft configuration) files.
 *
 * <p>This parser handles the ACF file format which contains aircraft properties
 * in key-value pairs within a PROPERTIES_BEGIN/PROPERTIES_END section.</p>
 *
 * <h2>Grammar Overview</h2>
 * <pre>
 * ACFFile = Header("ACF") Newlines Properties
 * Properties = "PROPERTIES_BEGIN" Property* "PROPERTIES_END"
 * Property = "P " name " " value Newline
 * </pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * AcfFileParser parser = new AcfFileParser();
 * AcfFileData data = parser.parse(acfContents);
 * String version = data.getHeader().getSpecVersion();
 * }</pre>
 *
 * @author Olivier G.
 * @see AcfFileData
 */
@Slf4j
public class AcfFileParser extends XPlaneFileParserBase<AcfFileData> {

    /**
     * Required file type for ACF files.
     */
    static final String REQUIRED_TYPE = "ACF";

    /**
     * Matches a full X-Plane aircraft file.
     * Upon successful match, pushes an instance of {@link AcfFileData}
     *
     * @return parser for ACF files
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
     *
     * @return parser for properties section
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

    /**
     * Parse the PROPERTIES_END marker.
     *
     * @return parser for properties end marker
     */
    private Parser PropertiesEnd() {
        return of("PROPERTIES_END").seq(JunkLine()).flatten();
    }

    /**
     * Parse the PROPERTIES_BEGIN marker.
     *
     * @return parser for properties begin marker
     */
    private Parser PropertiesBegin() {
        return of("PROPERTIES_BEGIN").seq(JunkLine()).flatten();
    }

    /**
     * Matches a line with property name and value.
     * Upon successful match, pushes a {@link AcfProperty} instance
     *
     * @return parser for property lines
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
     *
     * @return parser for property values
     */
    Parser PropertyValue() {
        return noneOf("\r\n").plus().flatten();
    }

    /**
     * Matches a property name.
     * Upon successful match, pushes the name as a String.
     *
     * @return parser for property names
     */
    Parser PropertyName() {
        return PropertyNameChar().plus().flatten();
    }

    /**
     * Parse a property name character (alphanumeric, underscore, comma, slash).
     *
     * @return parser for property name characters
     */
    Parser PropertyNameChar() {
        return Alphanumeric().or(anyOf("_,/"));
    }

}
