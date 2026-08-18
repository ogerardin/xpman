/**
 * PetitParser-based parsers for X-Plane file formats.
 *
 * <h2>Overview</h2>
 * <p>This package contains grammar-based parsers built on the
 * <a href="https://github.com/petitparser/java-petitparser">PetitParser</a> library.
 * Each parser defines the grammar for a specific X-Plane file format and produces
 * corresponding {@link com.ogerardin.xplane.file.data.XPlaneFileData} instances.</p>
 *
 * <h2>Architecture</h2>
 * <p>All parsers extend {@link com.ogerardin.xplane.file.petitparser.XPlaneFileParserBase},
 * which provides:</p>
 * <ul>
 *   <li><b>Template Method pattern:</b> Subclasses implement {@code XPlaneFile()} to define grammar</li>
 *   <li><b>Lazy initialization:</b> Parsers are built once and cached via {@code @Getter(lazy=true)}</li>
 *   <li><b>Common utilities:</b> Built-in parsers for headers, numbers, comments, newlines</li>
 *   <li><b>Type validation:</b> {@code Header(requiredType)} validates file type during parsing</li>
 * </ul>
 *
 * <h2>Available Parsers</h2>
 * <table>
 *   <caption>Parsers by file format</caption>
 *   <tr><th>Parser</th><th>File Format</th><th>Produces</th></tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.petitparser.AcfFileParser}</td>
 *     <td>{@code .acf}</td>
 *     <td>{@link com.ogerardin.xplane.file.data.acf.AcfFileData}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.petitparser.DatFileParser}</td>
 *     <td>{@code .dat}</td>
 *     <td>{@link com.ogerardin.xplane.file.data.dat.DatFileData}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.petitparser.ObjFileParser}</td>
 *     <td>{@code .obj}</td>
 *     <td>{@link com.ogerardin.xplane.file.data.obj.ObjFileData}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.petitparser.SceneryPacksIniParser}</td>
 *     <td>{@code scenery_packs.ini}</td>
 *     <td>{@link com.ogerardin.xplane.file.data.scenery.SceneryPackIniData}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.petitparser.ServersFileParser}</td>
 *     <td>server_list</td>
 *     <td>{@link com.ogerardin.xplane.file.data.servers.ServersFileData}</td>
 *   </tr>
 * </table>
 *
 * <h2>Grammar Conventions</h2>
 * <p>Parser method names follow grammar conventions:</p>
 * <ul>
 *   <li><b>Uppercase names</b> for productions (e.g., {@code XPlaneFile()}, {@code Header()})</li>
 *   <li><b>Token names</b> match the syntax element (e.g., {@code Number()}, {@code Newline()})</li>
 *   <li><b>Composition</b> via {@code .seq()}, {@code .or()}, {@code .star()}, {@code .map()}</li>
 * </ul>
 *
 * <h2>Extension Guide</h2>
 * <p>To add a parser for a new file format:</p>
 * <ol>
 *   <li>Create data class in {@code com.ogerardin.xplane.file.data} sub-package</li>
 *   <li>Extend {@code XPlaneFileParserBase<YourDataType>}</li>
 *   <li>Implement {@code XPlaneFile()} with grammar rules</li>
 *   <li>Use {@code Header("YOUR_TYPE")} for type validation</li>
 *   <li>Compose with built-in parsers: {@code Number()}, {@code Comment()}, {@code Newline()}</li>
 * </ol>
 *
 * @see com.ogerardin.xplane.file.petitparser.XPlaneFileParserBase
 * @see com.ogerardin.xplane.file.StringParser
 * @see com.ogerardin.xplane.file.data.XPlaneFileData
 */
package com.ogerardin.xplane.file.petitparser;
