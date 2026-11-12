package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.Fix;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import com.ogerardin.xpman.util.jfx.menu.annotation.Order;
import com.ogerardin.xpman.util.jfx.menu.annotation.Value;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Window;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** UI wrapper that adds context-menu actions to an inspection result. */
@Data
public class UiInspectionMessage {

    @Delegate
    @Getter(AccessLevel.NONE)
    private final InspectionMessage message;

    private final Window owner;

    private final boolean detailsPresent;

    public UiInspectionMessage(InspectionMessage message, Window owner) {
        this.message = message;
        this.owner = owner;
        this.detailsPresent = message.getDetails() != null && !message.getDetails().isBlank();
    }

    @Order(10)
    @Label("'Search the web for this message'")
    public void searchWeb() {
        Platforms.getCurrent().openUrl(getSearchUrl());
    }

    @Order(20)
    @EnabledIf("detailsPresent")
    @Label("'Copy details'")
    public void copyDetails() {
        ClipboardContent content = new ClipboardContent();
        content.putString(getDetailsText());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @Order(30)
    @EnabledIf("hasFix")
    @ForEach(group = "Fixes", iterable = "fixes", itemLabel = "#item.label()")
    public void applyFix(@Value("#item") Fix fix) {
        FixRunner.run(fix, owner);
    }

    public boolean isHasFix() {
        return !message.getFixes().isEmpty();
    }

    public String getSearchUrl() {
        String details = message.getDetails();
        String query = details != null && !details.isBlank()
                ? details.lines().findFirst().orElse(message.getMessage()) : message.getMessage();
        // Strip line number prefixes (e.g., "line 42: ") from the query
        query = query.replaceFirst("^line \\d+: ", "");
        return "https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    public String getDetailsText() {
        String details = message.getDetails();
        return details != null && !details.isBlank() ? details : message.getMessage();
    }
}
