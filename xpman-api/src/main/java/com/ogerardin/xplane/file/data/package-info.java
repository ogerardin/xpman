/**
 * Data model classes for parsed X-Plane file content.
 *
 * <h2>Overview</h2>
 * <p>This package contains the structured data representations produced by parsing
 * X-Plane files. Each file format has a corresponding data class that captures the
 * parsed content in a type-safe, immutable structure.</p>
 *
 * <h2>Base Class</h2>
 * <p>All data models extend {@link com.ogerardin.xplane.file.data.XPlaneFileData},
 * which provides common header information via Lombok's {@code @Delegate} pattern:</p>
 * <ul>
 *   <li>{@link com.ogerardin.xplane.file.data.Header} - File origin, version, and type</li>
 * </ul>
 *
 * <h2>Format-Specific Sub-packages</h2>
 * <table>
 *   <caption>Sub-packages by file format</caption>
 *   <tr><th>Package</th><th>File Format</th><th>Description</th></tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.data.acf}</td>
 *     <td>{@code .acf}</td>
 *     <td>Aircraft configuration - property maps and metadata</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.data.dat}</td>
 *     <td>{@code .dat}</td>
 *     <td>Navigation data - waypoints, airports, airways</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.data.obj}</td>
 *     <td>{@code .obj}</td>
 *     <td>3D objects - attributes, vertex data, render commands</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.data.scenery}</td>
 *     <td>{@code scenery_packs.ini}</td>
 *     <td>Scenery priority lists with path and token variants</td>
 *   </tr>
 *   <tr>
 *     <td>{@link com.ogerardin.xplane.file.data.servers}</td>
 *     <td>server_list</td>
 *     <td>Server version information for update checks</td>
 *   </tr>
 * </table>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>All data classes use Lombok annotations ({@code @Data}, {@code @Value}, {@code @Builder})</li>
 *   <li>Immutable by default - constructed by parsers, not modified afterward</li>
 *   <li>Header delegation allows uniform access to version/type across all data types</li>
 * </ul>
 *
 * @see com.ogerardin.xplane.file.data.XPlaneFileData
 * @see com.ogerardin.xplane.file.data.Header
 * @see com.ogerardin.xplane.file.petitparser
 */
package com.ogerardin.xplane.file.data;
