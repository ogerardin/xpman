package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.XPlane;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Known scenery_packs.ini tokens and the folder each designates for a given install.
 * Unknown tokens resolve to nothing. Adding a token is one constant here — no if-chain.
 */
@RequiredArgsConstructor
@Getter
public enum SceneryIniToken {

    GLOBAL_AIRPORTS("*GLOBAL_AIRPORTS*") {
        @Override
        public Path resolveFolder(XPlane xPlane) {
            return xPlane.getPaths().globalAirports();
        }
    };

    /** The token as written in scenery_packs.ini. */
    private final String marker;

    /** The folder this token designates for the given X-Plane install. */
    public abstract Path resolveFolder(XPlane xPlane);

    /** The known token whose marker matches, or empty for an unknown token. */
    public static Optional<SceneryIniToken> of(String token) {
        return Arrays.stream(values()).filter(t -> t.marker.equals(token)).findFirst();
    }
}
