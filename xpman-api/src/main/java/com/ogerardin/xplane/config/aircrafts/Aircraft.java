package com.ogerardin.xplane.config.aircrafts;

import com.ogerardin.xplane.config.LinkType;
import com.ogerardin.xplane.diag.DiagItem;
import com.ogerardin.xplane.diag.DiagProvider;
import com.ogerardin.xplane.file.AcfFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Slf4j
public class Aircraft implements DiagProvider {

    @Delegate(excludes = Named.class)
    private final AcfFile acfFile;

    public final String name;

    public Aircraft(AcfFile acfFile) {
        this(acfFile, acfFile.getName());
    }

    @Override
    public List<DiagItem> getDiagItems() {
        return null;
    }

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

    public Map<LinkType, URL> getLinks() {
        return Collections.emptyMap();
    }

}
