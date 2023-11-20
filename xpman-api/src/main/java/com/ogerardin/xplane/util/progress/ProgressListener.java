package com.ogerardin.xplane.util.progress;

/**
 * Allows a task to report its progress through the following indications:
 * <ul>
 *     <li>A completion ratio as a double value between 0.0 (0%) and 1.0 (100%)</li>
 *     <li>A current activity message</li>
 *     <li>A cumulative text output (similar to a console)</li>
 * </ul>
 */
public interface ProgressListener {
    /**
     * Signal a change in the progress.
     * @param ratio the completion ratio. Numbers between 0.0 and 1.0 (included) are interpreted as a new completion ratio value.
     *                Negative numbers are interpreted as "indeterminate progress" and should cause any visual
     *                representation to change to a "spinning wheel" or similar.
     *                A null value means "no change in progress".
     * @param message new "current activity" to be displayed along with the progress. A null value means "no change in current activity".
     */
    void progress(Double ratio, String message);

    default void progress(Double ratio) {
        progress(ratio, null);
    }

    default void progress(String message) {
        progress(null, message);
    }

    /**
     * Append a line of text to the cumulative output (console).
     */
    default void output(String message) {}
}
