package com.ogerardin.xpman.util.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.Logs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipFile;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LogsTest {

    @Test
    void zipContainsRegularFilesAndTheirContents(@TempDir Path tempDir) throws IOException {
        Path source = Files.createDirectory(tempDir.resolve("logs"));
        Files.writeString(source.resolve("xpman.log"), "current log");
        Files.writeString(source.resolve("xpman.2026-10-08.0.log"), "previous log");
        Path zip = source.resolve("logs.zip");

        Logs.zip(source, zip);

        try (ZipFile archive = new ZipFile(zip.toFile())) {
            assertThat(archive.stream().map(entry -> entry.getName()).collect(toSet()),
                    is(Set.of("xpman.log", "xpman.2026-10-08.0.log")));
            try (var log = archive.getInputStream(archive.getEntry("xpman.log"))) {
                assertThat(new String(log.readAllBytes()), is("current log"));
            }
        }
    }

    @Test
    void mainLogbackConfigurationCreatesThePlatformLogFile(@TempDir Path tempDir) throws Exception {
        String previousUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        LoggerContext context = new LoggerContext();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(XPmanFX.class.getResource("/logback.xml"));

            Path logDir = Path.of(context.getProperty("LOG_DIR"));
            assertThat(Files.isRegularFile(logDir.resolve("xpman.log")), is(true));
        } finally {
            context.stop();
            if (previousUserHome == null) {
                System.clearProperty("user.home");
            } else {
                System.setProperty("user.home", previousUserHome);
            }
        }
    }
}
