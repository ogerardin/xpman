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

    opens com.ogerardin.xplane.test.file;
    opens com.ogerardin.xplane.test.petitparser;
}