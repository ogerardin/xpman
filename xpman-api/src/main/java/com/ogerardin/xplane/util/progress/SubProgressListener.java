package com.ogerardin.xplane.util.progress;

/**
 * Translates the progress of a sub-task into progress of a parent task. Sub-task progress 0.0 is translated as
 * {@link #start}, sub-task progress 1.0 is translated as progress {@link #end}, and values in-between are translated
 * linearly. Negative or null progress values are passed unchanged. Messages are passed unchanged.
 */
public record SubProgressListener(ProgressListener parent, Double start, Double end) implements ProgressListener {

    @Override
    public void progress(Double ratio, String message) {
        parent.progress(mapValue(ratio), message);
    }

    private Double mapValue(Double ratio) {
        if (ratio == null || ratio < 0.0) {
            return ratio;
        }
        return start + (end - start) * ratio;
    }

    @Override
    public void progress(Double ratio) {
        parent.progress(mapValue(ratio));
    }

    @Override
    public void progress(String message) {
        parent.progress(message);
    }

    @Override
    public void output(String message) {
        parent.output(message);
    }
}
