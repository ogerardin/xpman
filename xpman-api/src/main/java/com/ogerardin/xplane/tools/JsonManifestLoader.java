package com.ogerardin.xplane.tools;

import com.google.gson.*;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
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
 * <p>Notes:
 * <ul>
 * <li>"platform" must be specified as the name of one of the enum values of {@link Platforms}</li>
 * <li>"xplaneVersion" must be specified as the name of one of the enum values of {@link XPlaneMajorVersion}</li>
 * <li> "installChecker" accepts the following JSON members to produce a {@code Predicate<Path>}:
 *   <ul>
 *   <li>"string": will check if the executable file contains the specified string</li>
 *   </ul>
 * </li>
 * <li>"items" is a list of {@link Manifest}s that will be unfolded recursively</li>
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

//    @SneakyThrows
    public Manifest loadManifest(Path jsonManifest) {
        try (BufferedReader reader = Files.newBufferedReader(jsonManifest)) {
            Manifest manifest = GSON.fromJson(reader, Manifest.class);
            // manifest ID is the file name without the ".json" extension
            String id = jsonManifest.getFileName().toString().replace(".json", "");
            return manifest.withId(id);
        } catch (Exception e) {
            log.error("Exception while loading manifest from " + jsonManifest, e);
            throw new RuntimeException(e);
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
