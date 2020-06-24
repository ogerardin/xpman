package com.ogerardin.xplane.config.aircrafts;

import com.ogerardin.xplane.config.LinkType;
import com.ogerardin.xplane.diag.DiagItem;
import com.ogerardin.xplane.diag.DiagProvider;
import com.ogerardin.xplane.file.AcfFile;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class Aircraft implements DiagProvider {

    // all AcfFile methods (except those defined in Named) are available on Aircraft
    @Delegate(excludes = Named.class)
    @Setter(AccessLevel.PACKAGE)
    private AcfFile acfFile;

    public final String name;

    public boolean enabled = false;

    public Aircraft(AcfFile acfFile, String name) {
        this.acfFile = acfFile;
        this.name = name;
    }

    public Aircraft(AcfFile acfFile) {
        this(acfFile, acfFile.getName());
    }

    @Override
    public List<DiagItem> getDiagItems() {
        return null;
    }

    //** Methods excluded from delegation */
    private interface Named {
        String getName();
    }

    public String getVersion() {
        return null;
    }

    public Path getThumb() {
        Path file = getAcfFile().getFile();
        String filename = file.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String thumbFilename = filename.substring(0, dot) + "_icon11_thumb.png";
        Path thumbFile = file.resolveSibling(thumbFilename);
        return thumbFile;
    }

    protected void assertValid(boolean valid) throws InstantiationException {
        if (! valid) {
            throw new InstantiationException();
        }
    }

    public Map<LinkType, URL> getLinks() {
        return Collections.emptyMap();
    }

}
