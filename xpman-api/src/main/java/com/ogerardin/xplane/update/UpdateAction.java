package com.ogerardin.xplane.update;

import com.ogerardin.xplane.util.progress.ProgressListener;
import java.io.IOException;
import java.net.URL;

/** Execution capabilities; browser and wizard presentation belong to the UI module. */
public sealed interface UpdateAction {
    @FunctionalInterface
    non-sealed interface Direct extends UpdateAction {
        void apply(ProgressListener progress) throws IOException;
    }

    @FunctionalInterface
    non-sealed interface DownloadPage extends UpdateAction {
        URL url() throws IOException;
    }
}
