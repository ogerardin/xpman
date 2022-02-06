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
    requires logback.classic;

    opens com.ogerardin.xpman.panels.about to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.panels.navdata to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.plugins to  javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.scenery to  javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.scenery.rules to  javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.aircrafts to  javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.scenery_organizer to spring.expression;
    opens com.ogerardin.xpman.panels.xplane to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.util.jfx to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.util.jfx.wizard to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.install.wizard to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.diag to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman to javafx.base, javafx.fxml;

    exports com.ogerardin.xpman;
}