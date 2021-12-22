package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a generic X-Plane file.
 * @param <R> type of the result produced by the parser
 */
@Data
@Slf4j
@RequiredArgsConstructor
public abstract class XPlaneFile<R extends XPlaneFileData> implements StringParser<R> {

    @Getter
    private final Path file;

    @NonNull
    @Delegate
    private final StringParser<R> parser;

    /** Result of the parsing */
    @Getter(lazy = true)
    @ToString.Exclude
    private final R data = parse();

    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }

    @SneakyThrows
    private R parse()  {
        Objects.requireNonNull(file);
        log.debug("Parsing file {}", file);
        byte[] bytes = Files.readAllBytes(file);
        String fileContents = new String(bytes, UTF_8);
        return parse(fileContents);
    }

}
