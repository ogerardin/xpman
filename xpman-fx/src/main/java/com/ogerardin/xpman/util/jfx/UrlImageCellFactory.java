package com.ogerardin.xpman.util.jfx;

import javafx.beans.NamedArg;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Factory for a {@code TableCell<?, URL>} that assumes the {@link URL} value is the URL of an image file
 * and renders it as a thumbnail
 */
@Slf4j
public record UrlImageCellFactory<S>(Double height) implements TableCellFactory<S, URL> {

    public UrlImageCellFactory(@NamedArg("height") Double height) {
        this.height = height;
    }

    @Override
    public TableCell<S, URL> call(TableColumn<S, URL> param) {
        return new TableCell<S, URL>() {
            @Override
            protected void updateItem(URL url, boolean empty) {
                ImageView imageView = null;
                if (url != null) {
                    try (InputStream inputStream = url.openStream()) {
                        Image image = new Image(inputStream);
                        imageView = new ImageView(image);
                        imageView.setFitHeight(height);
                        imageView.setPreserveRatio(true);
//                    imageView.setSmooth(true);
//                    imageView.setCache(true);
                    } catch (IOException e) {
                        log.warn("Failed to load image: {}", url);
                    }
                }
                setGraphic(imageView);
            }
        };
    }

}
