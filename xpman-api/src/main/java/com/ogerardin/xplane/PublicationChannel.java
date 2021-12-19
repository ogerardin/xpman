package com.ogerardin.xplane;

import java.net.URL;

public interface PublicationChannel {

    String getLatestVersion() throws Exception;

    URL getUrl();

}
