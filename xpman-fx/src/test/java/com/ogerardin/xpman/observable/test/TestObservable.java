package com.ogerardin.xpman.observable.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xpman.XPlaneProperty;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestObservable extends Application {

    static XPlaneProperty xPlaneProperty = new XPlaneProperty();


    @SneakyThrows
    public static void main(String[] args) {
        xPlaneProperty.set(new XPlane(XPlane.getDefaultXPRootFolder()));
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml-test/test-observable.fxml"));

        Scene scene = new Scene(root, 300, 275);

        stage.setTitle("Test observable");
        stage.setScene(scene);
        stage.show();
    }

}
