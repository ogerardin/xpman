package com.ogerardin.xplane.file.parboiled;

import com.ogerardin.xplane.file.data.acf.AcfFileData;
import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.acf.AcfProperty;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

@Slf4j
public class AcfFileParser extends XPlaneFileParserBase {

    static final String REQUIRED_TYPE = "ACF";

    /**
     * Matches a full X-Plane aircraft file.
     * Upon successful match, pushes an instance of {@link AcfFileData}
     */
    @Override
    public Rule XPlaneFile() {
        return Sequence(
                Header(REQUIRED_TYPE),
                ZeroOrMore(Newline()),
                Properties(),
                swap() && push(new AcfFileData((Header) pop(), (AcfFileData.AcfProperties) pop()))
//                Junk(),
//                EOI
        );
    }

    /**
     * Matches a "properties" section.
     * Upon successful match, pushes an instance of {{@link AcfFileData.AcfProperties}}
     */
    @SuppressWarnings({"Convert2Lambda", "rawtypes"})
    Rule Properties() {
        return Sequence(
                "PROPERTIES_BEGIN", Optional(WhiteSpace()), Newline(),
                push(new AcfFileData.AcfProperties()),
                ZeroOrMore(
                        Sequence(
                                Property(),
                                new Action() {
                                    @Override
                                    public boolean run(Context context) {
                                        AcfProperty property = (AcfProperty) pop();
//                                        log.debug("Adding property {}", property);
                                        AcfFileData.AcfProperties properties = (AcfFileData.AcfProperties) peek();
                                        properties.put(property.getName(), property.getValue());
                                        return true;
                                    }
                                }
                        )
                ),
                "PROPERTIES_END", Optional(WhiteSpace()), Newline()
        );
    }

    /**
     * Matches a line with property name and value.
     * Upon successful match, pushes a {@link AcfProperty} instance
     */
    Rule Property() {
        return Sequence('P', ' ',
                PropertyName(), ' ',
                PropertyValue(),
                swap() && push(new AcfProperty((String) pop(), (String) pop())),
                Newline());
    }

    /**
     * Matches a property value.
     * Upon successful match, pushes the value as a String.
     */
    @SuppressSubnodes
    Rule PropertyValue() {
        return Sequence(
                OneOrMore(NoneOf("\r\n")),
                push(match())
        );
    }

    /**
     * Matches a property name.
     * Upon successful match, pushes the name as a String.
     */
    @SuppressSubnodes
    Rule PropertyName() {
        return Sequence(
                OneOrMore(PropertyNameChar()),
                push(match())
        );
    }

    Rule PropertyNameChar() {
        return FirstOf(Alphanumeric(), '_', ',', '/');
    }

}
