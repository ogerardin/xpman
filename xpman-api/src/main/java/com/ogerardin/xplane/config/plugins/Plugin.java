package com.ogerardin.xplane.config.plugins;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@Data
@AllArgsConstructor
public class Plugin {

    private final Path folder;

    private final String name;

    public Plugin(Path folder) {
        this(folder, folder.getFileName().toString());
    }

    public String getVersion() {
        return null;
    }

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

}
