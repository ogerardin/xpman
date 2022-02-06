package com.ogerardin.xpman.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@RequiredArgsConstructor
@Slf4j
public class JsonFileConfigPersister<C> {

    private final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @NonNull
    private final Class<C> configClass;

    @NonNull
    private final Path file;

    @Getter(lazy = true)
    private final C config = loadConfig();

    public JsonFileConfigPersister(Class<C> configClass) {
        this(configClass, configClass.getSimpleName() + ".json");
    }

    public JsonFileConfigPersister(Class<C> configClass, String filename) {
        this(configClass, getHomeDir().resolve(filename));
    }

    private static Path getHomeDir() {
        return Paths.get(System.getProperty("user.home"));
    }

    @SneakyThrows
    private C loadConfig() {
        log.debug("Loading prefs for {} from file {}", configClass, file);
        try {
            //noinspection ConstantConditions
            String json = new String(Files.readAllBytes(file));
            return GSON.fromJson(json, configClass);
        } catch (NoSuchFileException e) {
            //noinspection ConstantConditions
            return configClass.getDeclaredConstructor().newInstance();
        }
    }

    @SneakyThrows
    public void save() {
        // we can't use the field config directly because Lombok's lazy mechanism messes up GSON's introspection
        C confToSave = getConfig();
        log.debug("Storing prefs for {} with value: {} to file {}", configClass, confToSave, file);
        final String json = GSON.toJson(confToSave);
        Files.write(file, json.getBytes());
    }


    @SneakyThrows
    public void clean() {
        Files.deleteIfExists(getFile());
    }
}
