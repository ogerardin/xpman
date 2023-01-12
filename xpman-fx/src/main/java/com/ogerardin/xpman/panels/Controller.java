package com.ogerardin.xpman.panels;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Optional;

public abstract class Controller {

    protected final ImageView LOADING = Optional.ofNullable(this.getClass().getResource("/img/loading.gif"))
            .map(URL::toExternalForm)
            .map(Image::new)
            .map(ImageView::new)
            .get();

}
