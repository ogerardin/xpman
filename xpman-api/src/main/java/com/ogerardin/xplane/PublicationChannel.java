package com.ogerardin.xplane;

import java.net.URL;

public interface PublicationChannel {

    String getName();

    String getLatestVersion() throws Exception;

    URL getUrl();

}
