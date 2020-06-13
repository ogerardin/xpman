package com.ogerardin.xpman.config;

import com.google.gson.Gson;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public enum ConfigManager {
    INSTANCE;

    public static final String CONFIG_JSON = "config.json";

    @SneakyThrows
    public Config load() {
        return load(CONFIG_JSON);
    }

    @SneakyThrows
    public Config load(String fileName) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, Config.class);
        }
        catch (FileNotFoundException e) {
            return new Config();
        }
    }

    @SneakyThrows
    public void save(Config config) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(CONFIG_JSON)) {
            gson.toJson(config, writer);
        }
    }
}
