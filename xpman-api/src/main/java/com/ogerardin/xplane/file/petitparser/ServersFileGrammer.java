package com.ogerardin.xplane.file.petitparser;

import org.petitparser.tools.GrammarDefinition;

import static org.petitparser.parser.primitive.CharacterParser.anyOf;
import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.CharacterParser.of;
import static org.petitparser.parser.primitive.StringParser.of;

@SuppressWarnings("DuplicateStringLiteralInspection")
public class ServersFileGrammer extends GrammarDefinition {

    static final String REQUIRED_TYPE = "SERVERS";

    public ServersFileGrammer() {
        def("start",
                ref("HEADER")
                        .seq(ref("NEWLINE").star())
                        .seq(ref("VERSIONDECL").plus())
                        .seq(ref("JUNKLINE").star())
        );
        def("HEADER",
                ref("ORIGIN").seq(ref("NEWLINE"))
                        .seq(ref("VERSIONSPEC").seq(ref("NEWLINE")))
                        .seq(ref("FILETYPE").seq(ref("NEWLINE")))
                );
        def("ORIGIN", anyOf("IA").flatten());
        def("VERSIONSPEC", digit().times(4).seq(of(" version").optional()).flatten());
        def("FILETYPE", of(REQUIRED_TYPE).flatten());

        def("VERSIONDECL",
            of("BETA X-Plane ").seq(ref("VALUE"))
                    .or(of("FINAL X-Plane ").seq(ref("VALUE")))
                    .seq(ref("JUNKLINE"))
                    .flatten()
                );

        def("JUNKLINE", noneOf("\r\n").star().seq(ref("NEWLINE")).flatten());
        def("NEWLINE", of('\r').optional().seq(of('\n')));
        def("VALUE", noneOf("\r\n").plus().flatten());
    }

}
