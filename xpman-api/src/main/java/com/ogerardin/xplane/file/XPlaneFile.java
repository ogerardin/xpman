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
 * Abstract base class for all parsable X-Plane files.
 *
 * <p>This class implements the Template Method pattern, defining the file parsing lifecycle:</p>
 * <ol>
 *   <li>Read file content from URI</li>
 *   <li>Parse content using injected {@link StringParser}</li>
 *   <li>Return parsed result via lazy-initialized {@code data} field</li>
 * </ol>
 *
 * <h2>Type Parameters</h2>
 * <ul>
 *   <li>{@code R} - The type of result produced by parsing (e.g., {@code AcfFileData}, {@code ObjFileData})</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Parse from file path</h3>
 * <pre>{@code
 * Path acfPath = Path.of("/path/to/aircraft.acf");
 * AcfFile acfFile = new AcfFile(acfPath);
 * String version = acfFile.getFileSpecVersion();
 * Map<String, String> properties = acfFile.getProperties();
 * }</pre>
 *
 * <h3>Parse from URL</h3>
 * <pre>{@code
 * URL serverListUrl = new URL("https://lookup.x-plane.com/_lookup_11_/server_list_11.txt");
 * ServersFile serversFile = new ServersFile(serverListUrl);
 * String betaVersion = serversFile.getData().getBetaVersion();
 * }</pre>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li><b>Lazy parsing:</b> Content is only parsed when {@code getData()} is first called</li>
 *   <li><b>URI-based:</b> Supports file paths, URLs, and other URI schemes</li>
 *   <li><b>UTF-8 encoding:</b> All files are assumed to be UTF-8 encoded</li>
 * </ul>
 *
 * @param <R> the type of result produced by parsing
 * @author Olivier G.
 * @see StringParser
 * @see com.ogerardin.xplane.file.data.XPlaneFileData
 */
@Data
@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class XPlaneFile<R> implements StringParser<R> {

    /**
     * The URI of the file being parsed.
     * Used as the identity for equality and hashCode.
     */
    @Getter
    @EqualsAndHashCode.Include
    private final URI uri;

    /**
     * The parser used to transform file content into typed data.
     * Delegated via Lombok's {@code @Delegate} annotation.
     */
    @NonNull
    @Delegate
    private final StringParser<R> parser;

    /**
     * Create a file instance from a file system path.
     *
     * @param file the file path to parse
     * @param parser the parser to use
     * @throws NullPointerException if file or parser is null
     */
    public XPlaneFile(@NonNull Path file, StringParser<R> parser) {
        this(file.toUri(), parser);
    }

    /**
     * Create a file instance from a URL.
     *
     * @param url the URL to parse
     * @param parser the parser to use
     * @throws URISyntaxException if the URL cannot be converted to a URI
     * @throws NullPointerException if url or parser is null
     */
    public XPlaneFile(@NonNull URL url, StringParser<R> parser) throws URISyntaxException {
        this(url.toURI(), parser);
    }

    /**
     * The parsed result, lazily initialized on first access.
     * Parsing is deferred until this method is called.
     *
     * @return the parsed file data
     */
    @Getter(lazy = true)
    @ToString.Exclude
    private final R data = parse();

    /**
     * Returns the file specification version (e.g., "1100" for X-Plane 11).
     *
     * @return the version string, or null if not applicable
     */
    public abstract String getFileSpecVersion();

    /**
     * Read the file contents as a UTF-8 string.
     *
     * @return the file contents
     * @throws Exception if an I/O error occurs
     */
    @SneakyThrows
    private String getContentsAsString() {
        return IOUtils.toString(uri, UTF_8);
    }

    /**
     * Parse the file contents using the injected parser.
     *
     * @return the parsed result
     */
    @SneakyThrows
    private R parse()  {
        log.debug("Reading {}", uri);
        String fileContents = getContentsAsString();
        log.debug("Parsing {}", uri);
        return parse(fileContents);
    }

    /**
     * Returns the file as a Path, assuming the URI matches a file system provider.
     *
     * @return the file path
     * @throws IllegalArgumentException if the URI does not represent a file system path
     */
    public Path getFile() {
        return Path.of(uri);
    }
}
