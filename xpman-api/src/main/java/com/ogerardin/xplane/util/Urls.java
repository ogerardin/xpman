package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * URL construction helper that converts a string spec to a {@link URL} without checked exceptions.
 */
@UtilityClass
public class Urls {
    public URL url(String spec) throws MalformedURLException {
        return URI.create(spec).toURL();
    }
}
