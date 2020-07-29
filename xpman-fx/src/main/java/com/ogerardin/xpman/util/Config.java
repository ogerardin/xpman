package com.ogerardin.xpman.util;

import com.ogerardin.xpman.XPmanFX;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
@UtilityClass
public class Config {

    private final Properties gitProperties = loadGitProperties();

    private Properties loadGitProperties() {
        Properties properties = new Properties();
        try (InputStream resourceAsStream = Config.class.getResourceAsStream("/git.properties")) {
            if (resourceAsStream == null) {
                log.warn("git.properties not found");
            } else {
                properties.load(resourceAsStream);
            }
        } catch (Exception e) {
            log.debug("Failed to load git.properties: {}", e.toString());
        }
        return properties;
    }

    public String getBranch() {
        return gitProperties.getProperty("git.branch");
    }

    public String getBuildHost() {
        return gitProperties.getProperty("git.build.host");
    }

    public String getBuildTime() {
        return gitProperties.getProperty("git.build.time");
    }

    public String getCommit() {
        return gitProperties.getProperty("git.commit.id.abbrev");
    }

    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public String getJavaVmVendor() {
        return System.getProperty("java.vm.vendor");
    }

    public String getOsArch() {
        return System.getProperty("os.arch");
    }

    public String getRuntimeName() {
        return System.getProperty("java.runtime.name");
    }

    public String getVmName() {
        return System.getProperty("java.vm.name");
    }

    public String getVersion() {
        return gitProperties.getProperty("git.build.version",
                XPmanFX.class.getPackage().getImplementationVersion());
    }

    public String getBuildNumber() {
        return gitProperties.getProperty("git.build.number");
    }
}
