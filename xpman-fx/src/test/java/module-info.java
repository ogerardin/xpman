module xpman.fx.test {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires xpman.api;
    requires org.slf4j;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires spring.expression;
    requires org.apache.commons.lang3;
    requires ch.qos.logback.classic;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires xpman.fx;
    requires xpman.api.test;
    requires org.junit.jupiter.api;
    requires org.hamcrest;

    exports com.ogerardin.xpman.observable.test;
    opens com.ogerardin.xpman.observable.test to javafx.base, javafx.fxml;
    opens com.ogerardin.xpman.test to org.junit.platform.commons;
    opens com.ogerardin.xpman.util.test to org.junit.platform.commons, xpman.fx, com.google.gson;
}