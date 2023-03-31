package com.ogerardin.xplane.tools;

import com.google.gson.*;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Loads a {@link Manifest} from a JSON file.
 */
@Slf4j
@UtilityClass
public class JsonManifestLoader {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Platform.class, new PlatformAdapter())
            .registerTypeAdapter(Predicate.class, new PredicateDeserializer())
            .create();

    @SneakyThrows
    public Manifest loadManifest(Path jsonManifest) {
        try (BufferedReader reader = Files.newBufferedReader(jsonManifest)) {
            Manifest manifest = GSON.fromJson(reader, Manifest.class);
            return manifest;
        } catch (Exception e) {
            log.error("Failed to load tool manifest from {}", jsonManifest, e);
            return null;
        }
    }


    /**
     * Deserializer for {@link Platform}; expects a string that contains the name of one of the enum constants
     * from {@link Platforms}
     */
    private static class PlatformAdapter implements JsonDeserializer<Platform> {
        @Override
        public Platform deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String platformName = json.getAsString();
            Platform platform = Platforms.valueOf(platformName);
            return platform;
        }
    }

    /**
     * Deserializer for {@link Predicate}{@literal <T>}.
     * Supported T values are:
     * <ul>
     *     <li>{@link Path}: returns a {@literal Predicate<Path>} which matches a Path that meets <b>all</b> the conditions
     *     specified by the following JSON members:
     *     <ul>
     *         <li>{@code name} (string): the file designated by this path has the specified name</li>
     *         <li>{@code string} (string): the executable file designated by this path contains the specified string</li>
     *     </ul>
     * </ul>
     *
     * Throws IllegalArgumentException if the type of T is not supported.
     */
    private static class PredicateDeserializer implements JsonDeserializer<Predicate<?>> {
        @Override
        public Predicate<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
            Type paramType = paramTypes[0]; // Predicate has only one parameterized type T

            if (paramType == Path.class) {
                return getPathPredicate(json);
            }
            throw new IllegalArgumentException("Unsupported Predicate parameter type: " + paramType);
        }

        private Predicate<Path> getPathPredicate(JsonElement json) {
            if (!json.isJsonObject()) {
                throw new JsonParseException("Expected an object, got " + json);
            }

            JsonObject jsonObject = json.getAsJsonObject();
            Predicate<Path> predicate = path -> true;

            predicate = Optional.ofNullable(jsonObject.get("name"))
                    .map(JsonElement::getAsString)
                    .map(ToolUtils::hasName)
                    .map(predicate::and)
                    .orElse(predicate);

            predicate = Optional.ofNullable(jsonObject.get("string"))
                    .map(JsonElement::getAsString)
                    .map(ToolUtils::hasString)
                    .map(predicate::and)
                    .orElse(predicate);

            return predicate;
        }
    }

}
