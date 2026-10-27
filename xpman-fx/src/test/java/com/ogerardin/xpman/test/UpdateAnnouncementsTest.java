package com.ogerardin.xpman.test;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xpman.panels.home.HomeController;
import com.ogerardin.xpman.util.jfx.SkunkcraftsUpdateUtil;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ogerardin.xpman.test.FxTestSupport.onFx;
import static org.junit.jupiter.api.Assertions.*;

/** Checks real dashboard rows and shared compact actions, not duplicate offer calculations. */
class UpdateAnnouncementsTest {
    @BeforeAll static void startFx() throws Exception { FxTestSupport.start(); }

    @Test
    void bothChannelsUseOneProductRowAndOneAction() throws Exception {
        onFx(() -> {
            VBox section = category(new UpdateTestAddon(true, true));
            assertEquals(1, section.lookupAll(".updates-item").size());
            assertEquals(1, section.lookupAll(".updates-item-button").size());
            assertTrue(text(section).contains("Stable 2.1 · Beta 2.2-b1"));
            assertTrue(text(section).contains("Installed: 2.0"));
            assertEquals("Update…", ((Button) section.lookup(".updates-item-button")).getText());
            return null;
        });
    }

    @Test
    void betaOnlyAndUnavailableNeverPretendToOfferStable() throws Exception {
        onFx(() -> {
            VBox section = category(new UpdateTestAddon(false, true, "2.1", null));
            assertEquals(1, section.lookupAll(".updates-item").size());
            assertTrue(text(section).contains("Beta ?"));
            assertFalse(text(section).contains("Stable"));
            assertEquals(0, category(new UpdateTestAddon(false, false)).lookupAll(".updates-item").size());
            return null;
        });
    }

    @Test
    void compactActionShowsBetaWithoutAddingAnotherAction() throws Exception {
        onFx(() -> {
            AtomicInteger calls = new AtomicInteger();
            var graphic = SkunkcraftsUpdateUtil.updateAction(true, "Beta update: 2.2-b1", calls::incrementAndGet);
            var button = (Button) graphic.getChildren().getFirst();
            assertEquals("Update…", button.getText());
            assertEquals("Beta update: 2.2-b1", button.getTooltip().getText());
            assertEquals("Beta available", ((Label) graphic.getChildren().get(1)).getText());
            assertEquals(1, graphic.getChildren().stream().filter(Button.class::isInstance).count());
            button.fire();
            assertEquals(1, calls.get());
            assertEquals(1, SkunkcraftsUpdateUtil.updateAction(false, "Stable update: 2.1", () -> {}).getChildren().size());
            return null;
        });
    }

    @Test
    void xplaneAlsoUsesOneNamedRowWithBothVersions() throws Exception {
        onFx(() -> {
            var method = HomeController.class.getDeclaredMethod("createXPlaneUpdateRow", String.class, String.class,
                    XPlaneReleaseInfo.class, XPlaneReleaseInfo.class, Runnable.class);
            method.setAccessible(true);
            var row = (Parent) method.invoke(new HomeController(null), "X-Plane 12", "12.4.0",
                    new XPlaneReleaseInfo("12.4.1", Optional.empty()),
                    new XPlaneReleaseInfo("12.5.0-b1", Optional.empty()), (Runnable) () -> {});
            assertTrue(text(row).contains("X-Plane 12"));
            assertTrue(text(row).contains("Stable 12.4.1 · Beta 12.5.0-b1"));
            assertEquals(1, row.lookupAll(".updates-item-button").size());
            assertEquals("Update…", ((Button) row.lookup(".updates-item-button")).getText());
            return null;
        });
    }

    private VBox category(SkunkcraftsUpdatable addon) throws Exception {
        var method = HomeController.class.getDeclaredMethod("createCategorySection", String.class, List.class);
        method.setAccessible(true);
        return (VBox) method.invoke(new HomeController(null), "Aircraft", List.of(addon));
    }

    private String text(Parent node) {
        return node.getChildrenUnmodifiable().stream()
                .map(child -> child instanceof Label label ? label.getText() : child instanceof Parent parent ? text(parent) : "")
                .reduce("", (a, b) -> a + " " + b);
    }
}
