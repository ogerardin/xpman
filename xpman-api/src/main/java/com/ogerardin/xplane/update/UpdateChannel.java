package com.ogerardin.xplane.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UpdateChannel {
    RELEASE("Stable"), BETA("Beta");

    private final String label;
}
