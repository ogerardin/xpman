module xpman.api {
    requires lombok;
    requires org.slf4j;
    requires one.util.streamex;
    requires com.google.api.services.drive;
    requires org.jsoup;
    requires io.github.classgraph;
    requires com.google.common;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.jackson2;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    // filename-based automodules
    requires commons.lang;
    requires commons.configuration;
    requires pecoff4j;
    requires petitparser.core;

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

}