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

    /**
     * When set, {@link #getFullPath()} returns this path directly instead of
     * resolving {@link #file} against the parent folder. Used for files that
     * live outside the NavDataSet's primary folder (e.g. XP12 airspaces/atc data).
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Path absolutePath = null;

    /**
     * Creates a NavDataFile backed by an absolute path, bypassing the normal
     * folder-relative resolution.
     */
    public static NavDataFile of(NavDataSet navDataSet, Path absolutePath) {
        NavDataFile f = new NavDataFile(navDataSet, absolutePath.getFileName());
        f.setAbsolutePath(absolutePath);
        return f;
    }

    @Override
    public Path getPath() {
        return getFullPath();
    }

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final DatFile datFile = loadDatFile();

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final DatFileData data = loadData();

    private DatFile loadDatFile() {
        return new DatFile(getFullPath());
    }

    private DatFileData loadData() {
        if (!Files.exists(getFullPath())) {
            log.debug("Nav data file not found: {}", getFullPath());
            return null;
        }
        try {
            return getDatFile().getData();
        } catch (Exception e) {
            log.warn("Failed to parse {}: {}", getFullPath(), e.toString());
            return null;
        }
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
        return absolutePath != null ? absolutePath : navDataSet.getFolder().resolve(file);
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

}
