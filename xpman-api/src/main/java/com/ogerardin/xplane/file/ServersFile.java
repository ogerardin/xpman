package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.servers.ServersFileData;
import com.ogerardin.xplane.file.petitparser.ServersFileParser;

import java.net.URISyntaxException;
import java.net.URL;

/**
 * Represents a parsed X-Plane server list file containing version information.
 *
 * <p>Server list files are retrieved from X-Plane servers and contain information about
 * available versions (beta and final) for different platforms. This file is used to check
 * for updates.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * URL serverListUrl = new URL("https://lookup.x-plane.com/_lookup_11_/server_list_11.txt");
 * ServersFile serversFile = new ServersFile(serverListUrl);
 *
 * // Get version information
 * ServersFileData data = serversFile.getData();
 * String betaVersion = data.getBetaVersion();
 * String finalVersion = data.getFinalVersion();
 * }</pre>
 *
 * <h2>Source URL</h2>
 * <p>Example: <a href="https://lookup.x-plane.com/_lookup_11_/server_list_11.txt">
 * https://lookup.x-plane.com/_lookup_11_/server_list_11.txt</a></p>
 *
 * @author Olivier G.
 * @see ServersFileData
 * @see ServersFileParser
 */
public class ServersFile extends XPlaneFile<ServersFileData> {

    /**
     * Create a ServersFile from a URL.
     *
     * @param url the URL to the server list file
     * @throws URISyntaxException if the URL cannot be converted to a URI
     */
    public ServersFile(URL url) throws URISyntaxException {
        super(url, new ServersFileParser());
    }

    /**
     * Returns the server list file specification version.
     *
     * @return null (server list files have no version spec)
     */
    @Override
    public String getFileSpecVersion() {
        return null;
    }
}
