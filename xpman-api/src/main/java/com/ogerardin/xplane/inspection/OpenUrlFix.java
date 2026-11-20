package com.ogerardin.xplane.inspection;

import com.ogerardin.xplane.util.platform.Platforms;

/**
 * A {@link Fix} that opens a URL in the default browser.
 *
 * <p>Used by inspections that want to direct the user to a web page that resolves the issue
 * (e.g. downloading a missing optional engine mod). The URL is not opened until the user
 * confirms in the {@link Fix} confirmation dialog.</p>
 */
public class OpenUrlFix implements Fix {

    private final String label;
    private final String url;

    public OpenUrlFix(String label, String url) {
        this.label = label;
        this.url = url;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public void apply() {
        Platforms.getCurrent().openUrl(url);
    }

    @Override
    public boolean needsConfirmation() {
        return false;
    }
}
