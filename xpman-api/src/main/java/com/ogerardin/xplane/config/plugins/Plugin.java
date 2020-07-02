package com.ogerardin.xplane.config.plugins;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public class Plugin {

    private final Path folder;

    private final String name;

    public Plugin(Path folder) {
        this(folder, folder.getFileName().toString());
    }

    public String getVersion() {
        return "unknown";
    }
}
