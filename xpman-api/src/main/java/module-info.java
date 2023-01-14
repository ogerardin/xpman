module xpman.api {
    requires lombok;
    requires org.slf4j;
    requires one.util.streamex;
    requires org.jsoup;
    requires io.github.classgraph;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires org.apache.commons.io;

    // filename-based automodules
    requires commons.lang;
    requires commons.configuration;
    requires pecoff4j;
    requires petitparser.core;
    requires zip4j;

    requires com.google.gson;
    requires com.google.api.client;
    requires com.google.api.services.drive;
    requires google.api.client;
    requires com.google.api.client.json.gson;

    exports com.ogerardin.xplane;
    exports com.ogerardin.xplane.util.platform;
    exports com.ogerardin.xplane.inspection;
    exports com.ogerardin.xplane.util;
    exports com.ogerardin.xplane.install;
    exports com.ogerardin.xplane.events;
    exports com.ogerardin.xplane.aircrafts;
    exports com.ogerardin.xplane.file;
    exports com.ogerardin.xplane.scenery;
    exports com.ogerardin.xplane.plugins;
    exports com.ogerardin.xplane.laminar;
    exports com.ogerardin.xplane.navdata;
    exports com.ogerardin.xplane.tools;

    opens com.ogerardin.xplane.tools to com.google.gson;
    exports com.ogerardin.xplane.install.inspections;

}