package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.atc.AtcFileData;
import com.ogerardin.xplane.file.petitparser.AtcFileParser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An atc.dat file (XP12) as part of a {@link NavDataSet}.
 *
 * <p>atc.dat files provide airspace boundaries for regional, approach and large
 * tower controllers.</p>
 *
 * <p>See <a href="https://developer.x-plane.com/article/atc-dat/">atc.dat</a>
 * for the file format specification.</p>
 */
@Data
@Slf4j
public class AtcFile implements NavDataItem {

    @ToString.Exclude
    private final NavDataSet navDataSet;

    private final Path file;

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final AtcFileData data = loadData();

    private AtcFileData loadData() {
        if (!Files.exists(file)) {
            log.debug("atc.dat file not found: {}", file);
            return null;
        }
        try {
            return new AtcFileParser().parse(Files.readString(file));
        } catch (Exception e) {
            log.warn("Failed to parse {}: {}", file, e.toString());
            return null;
        }
    }

    @Override
    public Path getPath() {
        return file;
    }

    @Override
    public Boolean getExists() {
        return Files.exists(file);
    }

    @Override
    public String getName() {
        return navDataSet.getXPlane().getBaseFolder().relativize(file).toString();
    }

    @Override
    public String getDescription() {
        AtcFileData data = getData();
        if (data == null) {
            return null;
        }
        Header header = data.getHeader();
        return "<h3>" + getName() + "</h3><p><b>Origin:</b> " + header.getOrigin()
                + "<br/><b>Spec version:</b> " + header.getSpecVersion()
                + "<br/><b>File type:</b> " + header.getFileType() + "</p>";
    }
}