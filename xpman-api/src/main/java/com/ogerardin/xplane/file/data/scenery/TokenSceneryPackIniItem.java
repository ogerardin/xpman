package com.ogerardin.xplane.file.data.scenery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** A scenery_packs.ini entry whose value is a special token (e.g. *GLOBAL_AIRPORTS*). */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public non-sealed class TokenSceneryPackIniItem extends SceneryPackIniItem {

    private final String token;

    public TokenSceneryPackIniItem(String token) {
        this(token, false);
    }

    public TokenSceneryPackIniItem(String token, boolean disabled) {
        super(disabled);
        this.token = token;
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
