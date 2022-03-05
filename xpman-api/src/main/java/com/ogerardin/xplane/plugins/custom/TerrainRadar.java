package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@Slf4j
public class TerrainRadar extends Plugin {

    private static final String XPLANE_FORUM_URL = "https://forums.x-plane.org/index.php?/files/file/37864-terrain-radar-vertical-situation-display/";

    @Getter(lazy = true)
    private final String latestVersion = loadlatestVersion();


    public TerrainRadar(Path xplFile) throws InstantiationException {
        super(xplFile, "Terrain Radar");
        IntrospectionHelper.require(isTerrainRadar(xplFile));
    }

    private boolean isTerrainRadar(Path xplFile) {
        return xplFile.getParent().endsWith("TerrainRadar/64");
    }

    @Override
    public String getVersion() {
        Path readme = getXplFile().getParent().resolveSibling("readme.txt");
        String firstLine;
        try (Stream<String> lines = Files.lines(readme)) {
            firstLine = lines.findFirst().orElse("");
        } catch (IOException e) {
            return super.getVersion();
        }
        Pattern pattern = Pattern.compile("Terrain radar plugin v([\\d\\.]+).*");
        Matcher matcher = pattern.matcher(firstLine);
        if (! matcher.matches()) {
            return super.getVersion();
        }
        return matcher.group(1);
    }

    private String loadlatestVersion() {
        // not optimal, but works: we fetch the whole HTML page and regex the version out of it
        // TODO: improve this
        try {
            final URL url = new URL(XPLANE_FORUM_URL);
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
                "Terrain radar X-Plane forum", new URL(XPLANE_FORUM_URL)
        );
    }

}
