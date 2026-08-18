/**
 * X-Plane file parsing framework for reading and parsing various X-Plane file formats.
 *
 * <h2>Architecture</h2>
 * <p>This package implements a flexible file parsing framework using the <b>Template Method</b> pattern
 * and <b>Strategy</b> pattern for extensible parsing of X-Plane file formats.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link com.ogerardin.xplane.file.StringParser} - Functional interface for parsing strings</li>
 *   <li>{@link com.ogerardin.xplane.file.XPlaneFile} - Abstract base class for all file types</li>
 *   <li>{@link com.ogerardin.xplane.file.data.XPlaneFileData} - Base data model for parsing results</li>
 * </ul>
 *
 * <h2>Design Patterns</h2>
 * <ul>
 *   <li><b>Template Method:</b> {@code XPlaneFile<R>} defines the parsing lifecycle (read URI → parse → return data)</li>
 *   <li><b>Strategy:</b> Parsers are injected via {@link com.ogerardin.xplane.file.StringParser} interface</li>
 *   <li><b>Lazy Initialization:</b> Parsing is deferred until {@code getData()} is called</li>
 * </ul>
 *
 * <h2>Supported File Formats</h2>
 * <pre>
 * | Format     | Class                    | Description               |
 * |------------|--------------------------|---------------------------|
 * | .acf       | {@link com.ogerardin.xplane.file.AcfFile}            | Aircraft description files|
 * | .obj       | {@link com.ogerardin.xplane.file.ObjFile}            | 3D scenery objects        |
 * | .dat       | {@link com.ogerardin.xplane.file.DatFile}            | Navigation data files     |
 * | .ini       | {@link com.ogerardin.xplane.file.SceneryPacksIniFile}| Scenery priority lists    |
 * | server_list| {@link com.ogerardin.xplane.file.ServersFile}         | Server version lists      |
 * </pre>
 *
 * <h2>Extending the Framework</h2>
 * <p>To add support for a new X-Plane file format:</p>
 * <ol>
 *   <li>Create a data class extending {@link com.ogerardin.xplane.file.data.XPlaneFileData}</li>
 *   <li>Create a parser extending {@link com.ogerardin.xplane.file.petitparser.XPlaneFileParserBase}</li>
 *   <li>Create a file class extending {@link com.ogerardin.xplane.file.XPlaneFile}</li>
 * </ol>
 *
 * @see com.ogerardin.xplane.file.XPlaneFile
 * @see com.ogerardin.xplane.file.StringParser
 * @see com.ogerardin.xplane.file.data.XPlaneFileData
 */
package com.ogerardin.xplane.file;
