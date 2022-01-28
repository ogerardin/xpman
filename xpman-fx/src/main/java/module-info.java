module xpman.fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires xpman.api;
    requires org.slf4j;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires com.google.common;
    requires spring.expression;
    requires com.sun.jna;
    requires commons.lang;

    opens com.ogerardin.xpman.panels.plugins to  javafx.fxml;
    opens com.ogerardin.xpman.panels.navdata to  javafx.fxml;
    opens com.ogerardin.xpman.panels.scenery to  javafx.fxml;
    opens com.ogerardin.xpman.panels.aircrafts to  javafx.fxml;
    opens com.ogerardin.xpman.panels.xplane to javafx.fxml;
    opens com.ogerardin.xpman.util.jfx to javafx.fxml;
    opens com.ogerardin.xpman to javafx.fxml;

    exports com.ogerardin.xpman;
}