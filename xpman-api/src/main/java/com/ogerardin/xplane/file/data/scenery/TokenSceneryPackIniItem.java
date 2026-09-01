package com.ogerardin.xplane.file.data.scenery;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public non-sealed class TokenSceneryPackIniItem extends SceneryPackIniItem {

    /** The token designating the global airports entry in scenery_packs.ini. */
    public static final String GLOBAL_AIRPORTS_MARKER = "*GLOBAL_AIRPORTS*";

    /** The name of the Global Airports folder, resolved according to the X-Plane version. */
    public static final String GLOBAL_AIRPORTS_FOLDER = "Global Airports";

    private final String token;

    public TokenSceneryPackIniItem(String token) {
        this(token, false);
    }

    public TokenSceneryPackIniItem(String token, boolean disabled) {
        super(disabled);
        this.token = token;
    }

    @Override
    public Path resolveFolder(Path baseFolder, Path globalAirportsFolder) {
        return GLOBAL_AIRPORTS_MARKER.equals(token) ? globalAirportsFolder : null;
    }

    @Override
    public String getIniValue() {
        return token;
    }

    @Override
    public boolean isToken() {
        return true;
    }
}
