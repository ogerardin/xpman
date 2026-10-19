package com.ogerardin.xpman.util.jfx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class JfxAppPrefs {

    boolean confirmQuit = true;

    WindowPosition lastPosition;

    /** Wizard windows always open centered (ControlsFX force-centers them on show), so only size is persisted. */
    public record WindowSize(double width, double height) {}

    @Data
    @AllArgsConstructor
    public static class WindowPosition {
        double x;
        double y;
        double width;
        double height;
    }
}
