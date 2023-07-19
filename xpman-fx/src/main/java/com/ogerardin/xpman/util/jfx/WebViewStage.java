package com.ogerardin.xpman.util.jfx;

import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * A stage containing a {@link WebView} with a {@link WebEngine} that can be loaded with content.
 */
public class WebViewStage extends Stage {

    private final WebEngine webEngine;

    public WebViewStage() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        this.setScene(new Scene(webView));
    }

    public void loadContent(String content) {
        webEngine.loadContent(content);
    }

    public void loadUrl(String url) {
        webEngine.load(url);
    }
}
