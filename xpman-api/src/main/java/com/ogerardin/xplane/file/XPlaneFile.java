package com.ogerardin.xplane.file;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Base class for a generic parsable X-Plane file.
 * @param <R> type of the result produced by the parsing.
 */
@Data
@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class XPlaneFile<R> implements StringParser<R> {

    @Getter
    @EqualsAndHashCode.Include
    private final URI uri;

    @NonNull
    @Delegate //delegate the parse() method implementation to the actual parser
    private final StringParser<R> parser;

    public XPlaneFile(@NonNull Path file, StringParser<R> parser) {
        this(file.toUri(), parser);
    }

    public XPlaneFile(@NonNull URL url, StringParser<R> parser) throws URISyntaxException {
        this(url.toURI(), parser);
    }

    /** Result of the parsing */
    @Getter(lazy = true) //defer parsing until getData() is called
    @ToString.Exclude
    private final R data = parse();

    public abstract String getFileSpecVersion();

    @SneakyThrows
    private String getContentsAsString() {
        return IOUtils.toString(uri, UTF_8);
    }

    @SneakyThrows
    private R parse()  {
        log.debug("Reading {}", uri);
        String fileContents = getContentsAsString();
        log.debug("Parsing {}", uri);
        return parse(fileContents);
    }

    /** Returns the file correponding to the URI, assuming the URI matches a file system provider */
    public Path getFile() {
        return Path.of(uri);
    }
}
