package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Factory for a {@code TableCell<?, Path>} that assumes the {@link Path} value is the path of an image file
 * and renders it as a thumbnail
 */
@Slf4j
public class PathImageCellFactory<S> implements TableCellFactory<S, Path> {

    @Override
    public TableCell<S, Path> call(TableColumn<S, Path> param) {
        return new TableCell<S, Path>() {
            @Override
            protected void updateItem(Path imagePath, boolean empty) {
                ImageView imageView = null;
                if (imagePath != null) {
                    try (InputStream inputStream = Files.newInputStream(imagePath)) {
                        Image image = new Image(inputStream);
                        imageView = new ImageView(image);
//                    imageView.setFitWidth(100);
//                    imageView.setPreserveRatio(true);
//                    imageView.setSmooth(true);
//                    imageView.setCache(true);
                    } catch (IOException e) {
                        log.warn("Failed to load thumbnail: {}", imagePath);
                    }
                }
                setGraphic(imageView);
            }
        };
    }

}
