package com.ogerardin.xplane;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

/** A channel (e.g. GitHub, website) through which X-Plane releases are published and version info can be queried. */
public interface PublicationChannel {

    String getName();

    String getLatestVersion() throws IOException, GeneralSecurityException;

    URL getUrl();

}
