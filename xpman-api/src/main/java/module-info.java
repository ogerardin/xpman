module xpman.api {
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

    requires lombok;
    requires commons.lang;
    requires org.slf4j;
    requires one.util.streamex;
    requires com.sun.jna;
    requires commons.configuration;
    requires pecoff4j;
    requires com.google.api.services.drive;
    requires org.jsoup;
    requires io.github.classgraph;
    requires com.google.common;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.jackson2;
    requires petitparser.core;
    requires com.sun.jna.platform;

}