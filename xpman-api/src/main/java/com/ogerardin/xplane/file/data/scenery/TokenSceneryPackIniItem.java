package com.ogerardin.xplane.file.data.scenery;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public non-sealed class TokenSceneryPackIniItem extends SceneryPackIniItem {

    private final String token;

    public TokenSceneryPackIniItem(String token) {
        this(token, false);
    }

    public TokenSceneryPackIniItem(String token, boolean disabled) {
        super(disabled);
        this.token = token;
    }
}
