package com.ogerardin.xplane.file;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Base class for a generic parsable X-Plane file.
 * @param <R> type of the result produced by the parsing.
 */
@Data
@Slf4j
@RequiredArgsConstructor
public abstract class XPlaneFile<R> implements StringParser<R> {

    // file may be null if loaded from a URL. Might change to URI.
    @Getter
    private final Path file;

    @NonNull
    @Delegate //delegate the parse() method implementation to the actual parser
    private final StringParser<R> parser;

    /** Result of the parsing */
    @Getter(lazy = true) //defer parsing until getData() is called
    @ToString.Exclude
    private final R data = parse();

    public abstract String getFileSpecVersion();

    @SneakyThrows
    private R parse()  {
        Objects.requireNonNull(file);
        log.debug("Parsing file {}", file);
        byte[] bytes = Files.readAllBytes(file);
        String fileContents = new String(bytes, UTF_8);
        return parse(fileContents);
    }

}
