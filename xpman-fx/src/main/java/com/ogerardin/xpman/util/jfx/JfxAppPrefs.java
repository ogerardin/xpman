package com.ogerardin.xpman.util.jfx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class JfxAppPrefs {

    boolean confirmQuit = true;

    WindowPosition lastPosition;

    @Data
    @AllArgsConstructor
    public static class WindowPosition {
        double x;
        double y;
        double width;
        double height;
    }
}
