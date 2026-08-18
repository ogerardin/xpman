package com.ogerardin.xplane.file;

/**
 * Functional interface for parsing string content into a typed result.
 *
 * <p>This is the core parsing contract used throughout the X-Plane file parsing framework.
 * Implementations transform raw file content (as a String) into structured data objects.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Lambda implementation
 * StringParser<AcfFileData> parser = contents -> {
 *     // parsing logic
 *     return new AcfFileData(header, properties);
 * };
 *
 * // Method reference
 * AcfFileParser parser = AcfFileParser::new;
 * }</pre>
 *
 * <h2>Implementation Notes</h2>
 * <ul>
 *   <li>Implementations should be stateless and thread-safe</li>
 *   <li>Parsing exceptions should be propagated to the caller</li>
 *   <li>Use {@link org.petitparser.parser.Parser} for grammar-based parsing</li>
 * </ul>
 *
 * @param <R> the type of result produced by parsing
 * @author Olivier G.
 * @see com.ogerardin.xplane.file.XPlaneFile
 * @see com.ogerardin.xplane.file.petitparser.XPlaneFileParserBase
 */
@FunctionalInterface
public interface StringParser<R> {

    /**
     * Parse the given string content into an instance of type R.
     *
     * @param contents the raw string content to parse
     * @return the parsed result
     * @throws Exception if parsing fails
     */
    R parse(String contents) throws Exception;

}
