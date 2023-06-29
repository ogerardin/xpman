package com.ogerardin.xplane.test.petitparser;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;
import org.petitparser.utils.Tracer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
public class ParserTest<T> {

    protected T runParser(String fileContents, Parser parser, boolean trace) {
        if (trace) {
            parser = Tracer.on(parser, traceEvent -> log.debug(traceEvent.toString()));
        }

        final Result result = parser.parse(fileContents);
        if (result.isFailure()) {
            log.error("Parse error: {}", result.getMessage());
        }
        assertTrue(result.isSuccess());

        log.debug("resultValue={}", (Object) result.get());
        return result.get();
    }

    protected String getFileContentsAsString(Path path) throws IOException {
        final byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, US_ASCII);
    }

    protected String getResourceAsString(String resource) throws IOException, URISyntaxException {
        final URL url = getClass().getResource(resource);
        final Path path = Paths.get(url.toURI());
        return getFileContentsAsString(path);
    }

    protected String getXPlaneFileContents(String other) throws IOException {
        Path acfFile = XPlane.getDefaultXPRootFolder().resolve(other);
        return getFileContentsAsString(acfFile);
    }
}
