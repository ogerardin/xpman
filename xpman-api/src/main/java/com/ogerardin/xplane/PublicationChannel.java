package com.ogerardin.xplane;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

public interface PublicationChannel {

    String getName();

    String getLatestVersion() throws IOException, GeneralSecurityException;

    URL getUrl();

}
