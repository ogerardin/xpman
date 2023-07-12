package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.file.DatFile;
import com.ogerardin.xplane.file.data.dat.DatFileData;
import com.ogerardin.xplane.file.data.dat.DatHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A single Nav data file as part of a {@link NavDataSet}
 */
@Data
@Slf4j
public class NavDataFile implements NavDataItem {

    @ToString.Exclude
    private final NavDataSet navDataSet;

    private final Path file;

    @Override
    public Path getPath() {
        return getFullPath();
    }

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final DatFile datFile = loadDatFile();

    private DatFile loadDatFile() {
        return new DatFile(getFullPath());
    }

    @Override
    public String getName() {
        final Path relativePath = navDataSet.getXPlane().getBaseFolder().relativize(getFullPath());
        return relativePath.toString();
    }

    @Override
    public Boolean getExists() {
        return Files.exists(getFullPath());
    }

    private Path getFullPath() {
        return navDataSet.getFolder().resolve(file);
    }

    public String getAiracCycle() {
        return Optional.ofNullable(getData())
                .map(DatFileData::getHeader)
                .map(DatHeader::getDataCycle)
                .orElse(null);
    }

    public String getMetadata() {
        return Optional.ofNullable(getData())
                .map(DatFileData::getHeader)
                .map(DatHeader::getMetadata)
                .orElse(null);
    }

    public String getBuild() {
        return Optional.ofNullable(getData())
                .map(DatFileData::getHeader)
                .map(DatHeader::getBuild)
                .orElse(null);
    }
    private DatFileData getData() {
        try {
            return getDatFile().getData();
        } catch (Exception e) {
            log.info("Failed to parse {}: {}", getFullPath(), e.toString());
            return null;
        }
    }

}
