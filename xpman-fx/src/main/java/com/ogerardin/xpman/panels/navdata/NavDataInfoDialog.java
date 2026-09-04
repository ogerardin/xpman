package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.util.platform.Platforms;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native (WebKit-free) info dialog for a nav data item: description (HTML rendered as rich text),
 * folder, header metadata, and a link to the X-Plane nav data documentation.
 * Replaces the old WebViewStage HTML popup.
 */
public class NavDataInfoDialog extends Dialog<Void> {

    private static final String DOC_URL = "https://developer.x-plane.com/article/navdata/";

    public NavDataInfoDialog(UiNavDataItem item, Window owner) {
        initOwner(owner);
        setTitle("Nav data: " + item.getName());

        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.setPrefWidth(420);

        Node descriptionNode = renderDescription(item.getDescription());
        descriptionNode.getStyleClass().add("navdata-info-description");
        content.getChildren().add(descriptionNode);

        Optional.ofNullable(item.getPath()).ifPresent(path -> {
            Label folder = new Label(path.toString());
            folder.setWrapText(true);
            folder.getStyleClass().add("navdata-info-path");
            content.getChildren().add(folder);
        });

        VBox details = new VBox(2);
        addDetail(details, "AIRAC cycle", item.getAiracCycle());
        addDetail(details, "Metadata", item.getMetadata());
        addDetail(details, "Build", item.getBuild());
        if (!details.getChildren().isEmpty()) {
            content.getChildren().add(details);
        }

        Hyperlink docLink = new Hyperlink("X-Plane nav data documentation");
        docLink.setOnAction(__ -> openUrl(DOC_URL));
        content.getChildren().add(docLink);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getDialogPane().setContent(scrollPane);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getStylesheets().add(
                getClass().getResource("/css/xpman.css").toExternalForm());
        getDialogPane().setPrefSize(480, 520);
        setResizable(true);
    }

    public static void show(UiNavDataItem item, Node card) {
        Window owner = card.getScene() != null ? card.getScene().getWindow() : null;
        new NavDataInfoDialog(item, owner).showAndWait();
    }

    @SneakyThrows
    private static void openUrl(String urlString) {
        Platforms.getCurrent().openUrl(new URL(urlString));
    }

    private static void addDetail(VBox details, String label, String value) {
        if (value != null && !value.isBlank()) {
            details.getChildren().add(new Label(label + ": " + value));
        }
    }

    private static Node renderDescription(String html) {
        if (html == null || html.isBlank()) {
            return new Label("No description available.");
        }

        TextFlow flow = new TextFlow();
        flow.setLineSpacing(4);

        List<String> tokens = tokenizeHtml(html);

        boolean inBold = false;
        boolean inHeading = false;
        boolean needBullet = false;
        String pendingHref = null;

        for (String token : tokens) {
            if (token.startsWith("<")) {
                String tag = token.toLowerCase();
                if (tag.equals("<h3>") || tag.startsWith("<h3 ")) {
                    inHeading = true;
                } else if (tag.equals("</h3>")) {
                    inHeading = false;
                    flow.getChildren().add(new Text("\n\n"));
                } else if (tag.equals("<p>") || tag.startsWith("<p ")) {
                    flow.getChildren().add(new Text("\n\n"));
                } else if (tag.equals("<ul>") || tag.startsWith("<ul ")) {
                    // enter list
                } else if (tag.equals("</ul>")) {
                    flow.getChildren().add(new Text("\n"));
                } else if (tag.equals("<li>") || tag.startsWith("<li ")) {
                    needBullet = true;
                } else if (tag.equals("</li>")) {
                    flow.getChildren().add(new Text("\n"));
                } else if (tag.equals("<strong>") || tag.startsWith("<strong ")) {
                    inBold = true;
                } else if (tag.equals("</strong>")) {
                    inBold = false;
                } else if (tag.equals("<br/>") || tag.equals("<br>") || tag.startsWith("<br ")) {
                    flow.getChildren().add(new Text("\n"));
                } else if (tag.startsWith("<a ")) {
                    pendingHref = extractHref(tag);
                } else if (tag.equals("</a>")) {
                    pendingHref = null;
                }
            } else {
                String text = unescapeEntities(token).trim();
                if (text.isEmpty()) continue;

                if (needBullet) {
                    flow.getChildren().add(new Text("  • "));
                    needBullet = false;
                }

                if (pendingHref != null) {
                    String href = pendingHref;
                    Hyperlink link = new Hyperlink(text);
                    link.setPadding(Insets.EMPTY);
                    link.setOnAction(__ -> openUrl(href));
                    flow.getChildren().add(link);
                } else if (inHeading) {
                    Text heading = new Text(text);
                    heading.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em;");
                    flow.getChildren().add(heading);
                } else if (inBold) {
                    Text bold = new Text(text);
                    bold.setStyle("-fx-font-weight: bold;");
                    flow.getChildren().add(bold);
                } else {
                    flow.getChildren().add(new Text(text));
                }
            }
        }

        return flow;
    }

    private static List<String> tokenizeHtml(String html) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("<[^>]+>").matcher(html);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                tokens.add(html.substring(lastEnd, matcher.start()));
            }
            tokens.add(matcher.group());
            lastEnd = matcher.end();
        }
        if (lastEnd < html.length()) {
            tokens.add(html.substring(lastEnd));
        }
        return tokens;
    }

    private static String extractHref(String tag) {
        Matcher matcher = Pattern.compile("href=\"([^\"]*)\"").matcher(tag);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String unescapeEntities(String text) {
        return text
                .replaceAll("&#8211;", "–")
                .replaceAll("&#8220;", "“")
                .replaceAll("&#8221;", "”");
    }
}
