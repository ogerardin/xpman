package com.ogerardin.xpman.util.jfx;

import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * A stage containing a {@link WebView} with a {@link WebEngine} that can be loaded with content.
 * The active theme (see {@link ThemeManager}) is applied to loaded content so that HTML popups
 * match the app's dark/light appearance instead of flashing white in dark mode.
 */
public class WebViewStage extends Stage {

    private final WebEngine webEngine;

    private final boolean dark;

    public WebViewStage() {
        dark = ThemeManager.isCurrentThemeDark();
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        Scene scene = new Scene(webView);
        scene.getStylesheets().add(getClass().getResource("/css/xpman.css").toExternalForm());
        this.setScene(scene);
    }

    public void loadContent(String content) {
        webEngine.loadContent(wrapThemed(content));
    }

    public void loadUrl(String url) {
        webEngine.load(url);
    }

    /**
     * Wraps HTML content in a document that applies the theme-appropriate default colors and font.
     */
    private String wrapThemed(String content) {
        String style = dark
                ? "background:#0d1117;color:#e6edf3;"
                : "background:#ffffff;color:#1f2328;";
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="utf-8">
                <style>
                  html,body{%s}
                  a{color:#58a6ff}
                </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(style, content);
    }
}
