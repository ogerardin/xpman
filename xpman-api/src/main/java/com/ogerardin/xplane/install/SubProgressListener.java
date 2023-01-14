package com.ogerardin.xplane.install;

/**
 * Translates the progress of a sub-task into progress of a parent task. Sub-task progress 0.0 is translated as
 * {@link #start}, sub-task progress 1.0 is translated as progress {@link #end}, and values in-between are translated
 * linearly. Negative or null progress values are passed unchanged. Messages are passed unchanged.
 */
public record SubProgressListener(ProgressListener parent, Double start, Double end) implements ProgressListener {

    @Override
    public void progress(Double percent, String message) {
        parent.progress(mapValue(percent), message);
    }

    private Double mapValue(Double percent) {
        if (percent == null || percent < 0.0) {
            return percent;
        }
        return start + (end - start) * percent;
    }

    @Override
    public void progress(Double percent) {
        parent.progress(mapValue(percent));
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
