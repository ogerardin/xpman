package com.ogerardin.xplane.config;

import com.sun.jna.Platform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration;
import org.boris.pecoff4j.PE;
import org.boris.pecoff4j.ResourceDirectory;
import org.boris.pecoff4j.ResourceEntry;
import org.boris.pecoff4j.constant.ResourceType;
import org.boris.pecoff4j.io.PEParser;
import org.boris.pecoff4j.io.ResourceParser;
import org.boris.pecoff4j.resources.StringFileInfo;
import org.boris.pecoff4j.resources.StringPair;
import org.boris.pecoff4j.resources.StringTable;
import org.boris.pecoff4j.resources.VersionInfo;
import org.boris.pecoff4j.util.ResourceHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum XPlaneVariant {
    MAC(Platform.MAC, "X-Plane.app", XPlaneVariant::getMacVersion),
    WINDOWS(Platform.WINDOWS, "X-Plane.exe", XPlaneVariant::getPEVersion),
    LINUX(Platform.LINUX, "X-Plane-x86_64", XPlaneVariant::getELFVersion);

    private final int osType;
    private final String appFilename;
    private final Function<Path, String> versionFetcher;

    @SneakyThrows
    private static String getELFVersion(Path exePath) {
        // there seem to be no symbol in the ELF sylbol table pointing to the version string :(
/*
        ElfFile elfFile = ElfFile.from(Files.newInputStream(exePath));
        ElfSymbol symbol = elfFile.getELFSymbol("version_info");
        ElfSection section = elfFile.getSection(symbol.st_shndx);
        long offset_in_section = symbol.st_value - section.header.address;
        long offset_in_file = section.header.section_offset + offset_in_section;
        ByteBuffer buffer = ByteBuffer.allocate((int) symbol.st_size);
        try (FileChannel channel = FileChannel.open(exePath, StandardOpenOption.READ)) {
            channel.position(offset_in_file);
            channel.read(buffer);
        }
*/
        return "unknown";
    }

    @SneakyThrows
    private static String getPEVersion(Path exePath) {
        PE pe = PEParser.parse(exePath.toString());
        ResourceDirectory rd = pe.getImageData().getResourceTable();

        ResourceEntry[] entries = ResourceHelper.findResources(rd, ResourceType.VERSION_INFO);
        for (ResourceEntry resourceEntry : entries) {
            byte[] data = resourceEntry.getData();
            VersionInfo version = ResourceParser.readVersionInfo(data);
            StringFileInfo strings = version.getStringFileInfo();
            StringTable table = strings.getTable(0);
            for (int j = 0; j < table.getCount(); j++) {
                StringPair entry = table.getString(j);
                if (entry.getKey().equals("ProductVersion")) {
                    return entry.getValue();
                }
            }
        }
        return "unknown";
    }

    @SneakyThrows
    private static String getMacVersion(Path appPath) {
        Path plistFile = appPath.resolve("Contents").resolve("info.plist");
        XMLPropertyListConfiguration plist = new XMLPropertyListConfiguration(plistFile.toFile());
        return plist.getString("CFBundleShortVersionString");
    }

    public boolean applies(Path rootFolder) {
        if (appFilename == null) {
            return true;
        }
        return Files.exists(getAppPath(rootFolder));
    }

    public String getVersion(Path rootFolder) {
        return versionFetcher.apply(getAppPath(rootFolder));
    }

    public Path getAppPath(Path rootFolder) {
        return rootFolder.resolve(appFilename);
    }


}
