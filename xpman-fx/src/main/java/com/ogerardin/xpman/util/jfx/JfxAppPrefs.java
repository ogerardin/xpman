package com.ogerardin.xpman.util.jfx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class JfxAppPrefs {

    WindowPosition lastPosition;

    @Data
    @AllArgsConstructor
    static class WindowPosition {
        double x;
        double y;
        double width;
        double height;
    }
}
