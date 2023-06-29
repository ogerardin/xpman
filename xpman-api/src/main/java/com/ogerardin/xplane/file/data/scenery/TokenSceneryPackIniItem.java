package com.ogerardin.xplane.file.data.scenery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public non-sealed class TokenSceneryPackIniItem extends SceneryPackIniItem {
    private final String token;
}
