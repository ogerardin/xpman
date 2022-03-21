module xpman.fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires xpman.api;
    requires org.slf4j;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires spring.expression;
    requires commons.lang;
    requires logback.classic;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    opens com.ogerardin.xpman to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.config to com.google.gson;
    opens com.ogerardin.xpman.diag to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.install.wizard to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.panels.about to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.panels.aircrafts to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.aircrafts.details to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.navdata to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.plugins to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.scenery to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.scenery.rules to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.scenery.wizard to javafx.base, javafx.fxml, spring.expression;
    opens com.ogerardin.xpman.panels.xplane to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.scenery_organizer to javafx.base, javafx.fxml, spring.expression, com.google.gson;
    opens com.ogerardin.xpman.tools to javafx.fxml, javafx.base;
    opens com.ogerardin.xpman.util.jfx to javafx.base, javafx.fxml, com.google.gson;
    opens com.ogerardin.xpman.util.jfx.menu to com.google.gson, javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.util.jfx.wizard to javafx.base, javafx.fxml;

    exports com.ogerardin.xpman.panels.aircrafts;
    exports com.ogerardin.xpman.tools to javafx.fxml, spring.expression;
    exports com.ogerardin.xpman;
    opens com.ogerardin.xpman.util.jfx.menu.annotation to com.google.gson, javafx.base, javafx.fxml;
}