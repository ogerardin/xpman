package com.ogerardin.xplane.tools;

import com.google.gson.*;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Loads a {@link Manifest} from a JSON file.
 * General structure of the JSON file is as follows:
 * <pre>
 *  {
 *   "name": "...",
 *   "homepage": "...",
 *   "description": "...",
 *   "version": "...",
 *   "platform": "...",
 *   "xplaneVersion": "...",
 *   "installChecker": {
 *      "string": "..."
 *   }
 *   "items": [
 *      ...
 *   ]
 * }
 * </pre>
 * <p>Notes:
 * <ul>
 * <li>"platform" must be specified as the name of one of the enum values of {@link Platforms}</li>
 * <li>"xplaneVersion" must be specified as the name of one of the enum values of {@link XPlaneMajorVersion}</li>
 * <li> "installChecker" accepts the following JSON members to produce a {@code Predicate<Path>}:
 *   <ul>
 *   <li>"string": will check if the executable file contains the specified string</li>
 *   </ul>
 * </li>
 * <li>"items" is a list of sub-manifests that will be unfolded recursively, using the parent manifest as default
 * values. This is useful to produce variants without duplicating manifests</li>
 * </ul>
 */
@Slf4j
@UtilityClass
public class JsonManifestLoader {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Path.class,
                    (JsonDeserializer<Path>) (json, __, ___) -> Path.of(json.getAsString()))
            .registerTypeAdapter(Platform.class,
                    (JsonDeserializer<Platform>) (json, __, ___) -> Platforms.valueOf(json.getAsString()))
            .registerTypeAdapter(XPlaneMajorVersion.class,
                    (JsonDeserializer<XPlaneMajorVersion>) (json, __, ___) -> XPlaneMajorVersion.of(json.getAsString()))
            .registerTypeAdapter(Predicate.class, PredicateAdapter.INSTANCE)
            .create();

    /**
     * Load a  {@link Manifest} from a JSON file.
     */
    @SneakyThrows
    public Manifest loadManifest(Path jsonManifest) {
        return loadManifest(Files.newInputStream(jsonManifest), jsonManifest.getFileName().toString());
    }

    /**
     * Load a {@link Manifest} from an {@link InputStream}
     * @param path original path (for logging only)
     */
    public static Manifest loadManifest(InputStream is, String path) throws IOException {
        // manifest ID is the file name without the ".json" extension
        String id = path.replace(".json", "");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            Manifest manifest = GSON.fromJson(reader, Manifest.class);
            return manifest.withId(id);
        }
    }


    /**
     * Deserializer for {@link Predicate}{@literal <T>}.
     * Supported T values are:
     * <ul>
     *     <li>{@link Path}: returns a {@literal Predicate<Path>} which matches a Path that meets <b>all</b> the conditions
     *     specified by the following JSON members:
     *     <ul>
     *         <li>{@code string} (string): the file file designated by this path contains the specified string</li>
     *     </ul>
     * </ul>
     *
     * Throws IllegalArgumentException if the type of T is not supported.
     */
    private enum PredicateAdapter implements JsonDeserializer<Predicate<?>> {
        INSTANCE {
            @Override
            public Predicate<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
                Type paramType = paramTypes[0]; // Predicate<T> has only one parameter type T

                if (paramType == Path.class) {
                    return getPathPredicate(json);
                }
                throw new IllegalArgumentException("Unsupported Predicate parameter type: " + paramType);
            }
        };

        private static Predicate<Path> getPathPredicate(JsonElement json) {
            if (!json.isJsonObject()) {
                throw new JsonParseException("Expected an object, got " + json);
            }

            JsonObject jsonObject = json.getAsJsonObject();
            Predicate<Path> predicate = path -> true;

            predicate = Optional.ofNullable(jsonObject.get("string"))
                    .map(JsonElement::getAsString)
                    .map(ToolUtils::hasString)
                    .map(predicate::and)
                    .orElse(predicate);

            return predicate;
        }
    }

}
