package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.update.PublicationUpdateProvider;
import com.ogerardin.xplane.update.UpdateProvider;
import java.util.Optional;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
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

/**
 * A Plugin for which the main distribution channel is X-plane.org, which allows common implementation of
 * {@link Plugin#getLatestVersion()}
 */
@Slf4j
public class XPlaneOrgPlugin extends Plugin {

    @NonNull
    private final String xPlaneOrgDownloadPage;

    @Override
    protected Optional<UpdateProvider> loadUpdateProvider() {
        return super.loadUpdateProvider().or(() -> Optional.of(new XPlaneOrgPublicationUpdateProvider()));
    }

    public XPlaneOrgPlugin(XPlane xPlane, Path xplFile, String name, String description, String xPlaneOrgDownloadPage) {
        super(xPlane, xplFile, name, description);
        this.xPlaneOrgDownloadPage = xPlaneOrgDownloadPage;
    }

    private String loadLatestVersion() throws IOException {
        // not optimal, but works: we fetch the whole HTML page and regex the version out of it
        // TODO: improve this
        try {
            final URL url = Urls.url(xPlaneOrgDownloadPage);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
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
            log.warn("No softwareVersion found on page for {} ({})", getName(), xPlaneOrgDownloadPage);
        } catch (RuntimeException e) { throw new IOException("Could not read publication page", e); }
        return null;
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "Plugin page on X-Plane.org", Urls.url(xPlaneOrgDownloadPage)
        );
    }

    private class XPlaneOrgPublicationUpdateProvider extends PublicationUpdateProvider {
        public XPlaneOrgPublicationUpdateProvider() {
            super(XPlaneOrgPlugin.this::getVersion, XPlaneOrgPlugin.this::isEnabled);
        }

        public String getName() { return "X-Plane.org"; }

        protected String fetchLatestVersion() throws IOException { return loadLatestVersion(); }

        @SneakyThrows
        public URL getUrl() { return Urls.url(xPlaneOrgDownloadPage); }
    }
}
