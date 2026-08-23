package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Objects;

/**
 * The view for a {@link CategorySegment}.
 * Contains a single {@link Label} with the category name and a loading animation when the segment is computing.
 */
class CategorySegmentView extends StackPane {

    private final ImageView LOADING_ANIMATION = new ImageView(new Image(Objects.requireNonNull(CategorySegmentView.class.getResource("/img/dots.gif")).toExternalForm()));

    public CategorySegmentView(CategorySegment segment) {
        Label label = new Label();
        label.textProperty().bind(segment.textProperty());
        label.graphicProperty().bind(Bindings
                .when(segment.computingProperty())
                        .then(LOADING_ANIMATION)
                        .otherwise((ImageView) null)
                );
        label.getStyleClass().add("segment-label");
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        setAlignment(label, Pos.CENTER_LEFT);
        getChildren().add(label);
        getStyleClass().add(segment.getCategory().getStyleClass());
        setPadding(new Insets(5));
        setPrefHeight(50);
    }

}
