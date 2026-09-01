package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.scenery.UiSceneryEntry;
import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;
import java.net.URL;

/**
 * Factory for a {@code TableCell<UiSceneryEntry, URL>} that displays either:
 * - A token icon (fth-box) for token entries with tooltip
 * - An image from URL for regular entries
 * - Nothing for entries without icon
 */
public class SceneryIconCellFactory<S extends UiSceneryEntry> implements TableCellFactory<S, URL> {

    private static final double ICON_SIZE = 24.0;

    @Override
    public TableCell<S, URL> call(TableColumn<S, URL> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(URL url, boolean empty) {
                super.updateItem(url, empty);

                if (empty) {
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }

                // Get the entry from the table row
                TableRow<S> row = getTableRow();
                UiSceneryEntry entry = (row != null) ? row.getItem() : null;

                if (entry != null && entry.getStatus() == SceneryEntryStatus.SYSTEM) {
                    FontIcon icon = new FontIcon(Feather.SHIELD);
                    icon.setIconSize((int) ICON_SIZE);
                    setGraphic(icon);
                    setTooltip(new Tooltip("System folder managed by X-Plane"));
                } else if (entry != null && entry.isToken()) {
                    // Show token icon with tooltip
                    FontIcon icon = new FontIcon(Feather.BOX);
                    icon.setIconSize((int) ICON_SIZE);
                    setGraphic(icon);
                    setTooltip(new Tooltip("Special token: *GLOBAL_AIRPORTS*"));
                } else if (url != null) {
                    // Show image from URL
                    try (InputStream inputStream = url.openStream()) {
                        Image image = new Image(inputStream);
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(ICON_SIZE);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                        setTooltip(null);
                    } catch (Exception e) {
                        setGraphic(null);
                        setTooltip(null);
                    }
                } else {
                    setGraphic(null);
                    setTooltip(null);
                }
            }
        };
    }
}
