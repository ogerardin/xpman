package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.StackPane;

public class SegmentView extends StackPane {

    public SegmentView(Segment segment) {
        Label label = new Label();
        label.textProperty().bind(segment.textProperty());
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.2em;");
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        setAlignment(label, Pos.CENTER_LEFT);
        getChildren().add(label);
        setStyle(String.format("-fx-background-color: %s;", segment.getType().getColor()));
        setPadding(new Insets(5));
        setPrefHeight(50);
    }

}
