package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class XPlaneOrgPlugin extends Plugin {

    @NonNull
    private final String xPlaneOrgDownloadPage;

    @Getter(lazy = true)
    private final String latestVersion = loadlatestVersion();

    public XPlaneOrgPlugin(XPlane xPlane, Path xplFile, String name, String description, String xPlaneOrgDownloadPage) {
        super(xPlane, xplFile, name, description);
        this.xPlaneOrgDownloadPage = xPlaneOrgDownloadPage;
    }

    private String loadlatestVersion() {
        // not optimal, but works: we fetch the whole HTML page and regex the version out of it
        // TODO: improve this
        try {
            final URL url = new URL(xPlaneOrgDownloadPage);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            final InputStream inputStream = connection.getInputStream();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, outputStream);
            connection.disconnect();
            String html = outputStream.toString(StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile(".*\"softwareVersion\": \"([0-9.]+)\".*", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(html);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        } catch (IOException e) {
            log.debug("Failed to fetch HTML page", e);
        }
        return null;
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "Plugin page on X-Plane.org", new URL(xPlaneOrgDownloadPage)
        );
    }
}
