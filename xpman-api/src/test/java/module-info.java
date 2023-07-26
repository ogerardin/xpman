module xpman.api.test {
    requires lombok;
    requires xpman.api;
    requires org.junit.jupiter.api;
    requires org.hamcrest;
    requires org.slf4j;
    requires petitparser.core;
    requires org.mockito;
    requires com.google.api.services.drive;
    requires org.apache.commons.io;

    exports com.ogerardin.test.util;

    opens com.ogerardin.xplane.test.file to org.junit.platform.commons;
    opens com.ogerardin.xplane.test.petitparser to org.junit.platform.commons;
    opens com.ogerardin.xplane.test.impl to org.junit.platform.commons;
    opens com.ogerardin.xplane.test.aircrafts to org.junit.platform.commons;
    opens com.ogerardin.xplane.test.laminar to org.junit.platform.commons;
    opens com.ogerardin.xplane.test.tools to org.junit.platform.commons;

}