package com.ogerardin.xplane.aircrafts;

import lombok.Data;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

@Data
public class Livery {

    private final Aircraft aircraft;

    /** path to livery folder (relative to aircraft's "liveries" folder) */
    private final Path folder;

    public String getName() {
        return folder.getFileName().toString();
    }

    public Path getThumb() {
        return getAcfDerivedFile("_icon11_thumb.png");
    }

    public Path getIcon() {
        return getAcfDerivedFile("_icon11.png");
    }

    private Path getAcfDerivedFile(String suffix) {
        String acfFilename = aircraft.getAcfFile().getFile().getFileName().toString();
        String derivedFilename = FilenameUtils.removeExtension(acfFilename) + suffix;
        return aircraft.getLiveriesFolder().resolve(folder).resolve(derivedFilename);
    }


}
