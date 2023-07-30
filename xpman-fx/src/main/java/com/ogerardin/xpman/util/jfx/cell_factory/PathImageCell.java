package com.ogerardin.xpman.util.jfx.cell_factory;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Factory for a {@code Cell<?, Path>} that assumes the {@link Path} value is the path of an image file
 * and renders it as a thumbnail.
 * Contains two variants:
 * {@link TableCellFactory} to be used in a {@link javafx.scene.control.TableView}
 * {@link TreeTableCellFactory} to be used in a {@link javafx.scene.control.TreeTableView}
 */
@Slf4j
@Data
public abstract class PathImageCell<S> {

    protected Double fitHeight;
    protected Double fitWidth;
    protected Boolean preserveRatio;
    protected Boolean smooth;
    protected Boolean cache;

    protected void updateCell(Cell<Path> cell, Path imagePath) {
        ImageView imageView = null;
        if (imagePath != null) {
            try (InputStream inputStream = Files.newInputStream(imagePath)) {
                Image image = new Image(inputStream);
                imageView = new ImageView(image);
                if (fitWidth != null) {
                    imageView.setFitWidth(fitWidth);
                }
                if (fitHeight != null) {
                    imageView.setFitHeight(fitHeight);
                }
                if (preserveRatio != null) {
                    imageView.setPreserveRatio(preserveRatio);
                }
                if (smooth != null) {
                    imageView.setSmooth(smooth);
                }
                if (cache != null) {
                    imageView.setCache(cache);
                }
            } catch (IOException e) {
                log.warn("Failed to load thumbnail: {}", imagePath);
            }
        }
        cell.setGraphic(imageView);
    }

    public static class TableCellFactory<S> extends PathImageCell<S> implements com.ogerardin.xpman.util.jfx.cell_factory.TableCellFactory<S, Path> {
        @Override
        public TableCell<S, Path> call(TableColumn<S, Path> param) {
            return new TableCell<>() {
                @Override
                protected void updateItem(Path imagePath, boolean empty) {
                    updateCell(this, imagePath);
                }
            };
        }
    }

    public static class TreeTableCellFactory<S> extends PathImageCell<S> implements com.ogerardin.xpman.util.jfx.cell_factory.TreeTableCellFactory<S, Path> {
        @Override
        public TreeTableCell<S, Path> call(TreeTableColumn<S, Path> param) {
            return new TreeTableCell<>() {
                @Override
                protected void updateItem(Path imagePath, boolean empty) {
                    updateCell(this, imagePath);
                }
            };
        }
    }


}
