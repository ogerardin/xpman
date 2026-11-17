package com.ogerardin.xplane.scenery.custom;

import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialized SceneryPackage that handles The Handy Objects Library by Scottish-Wings.<br>
 * Version is extracted from the readme file "aaa_Permission Note & Read Me.txt" which contains
 * a marker line like "+++++ THE LATEST UPDATE IS 8.40 +++++".
 */
@Slf4j
@SuppressWarnings("unused")
public class HandyObjectsLibrary extends SceneryPackage {

    private static final String FOLDER_NAME = "The_Handy_Objects_Library";
    private static final String README_NAME = "aaa_Permission Note & Read Me.txt";
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "(?i)\\+{3,}\\s*THE LATEST UPDATE IS\\s+(\\S+)\\s*\\+{3,}");

    public HandyObjectsLibrary(Path folder) throws InstantiationException {
        super(folder);
        require(folder.getFileName().toString().equals(FOLDER_NAME));
    }

    @SneakyThrows
    @Override
    public String getVersion() {
        Path readme = getFolder().resolve(README_NAME);
        if (!Files.exists(readme)) {
            return super.getVersion();
        }
        try {
            String content = Files.readString(readme);
            Matcher matcher = VERSION_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IOException e) {
            log.debug("Failed to read Handy Objects readme: {}", e.getMessage());
        }
        return super.getVersion();
    }
}
