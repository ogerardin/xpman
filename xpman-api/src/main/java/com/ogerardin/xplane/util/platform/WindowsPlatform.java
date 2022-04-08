package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.util.exec.CommandExecutor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class WindowsPlatform implements Platform {

    @Getter
    public final int osType = com.sun.jna.Platform.WINDOWS;

    @SneakyThrows
    @Override
    public void reveal(@NonNull Path path) {
        String explorerParam = "/select,\"" + path.toString() + "\"";
        CommandExecutor.exec("cmd", "/c", "explorer.exe " + explorerParam);
    }

    @Override
    public String revealLabel() {
        return "Show in Explorer";
    }

    @SneakyThrows
    @Override
    public void openUrl(@NonNull URL url) {
        CommandExecutor.exec("cmd", "/c", String.format("start %s", url.toString()));
    }

    @SneakyThrows
    @Override
    public void openFile(@NonNull Path file) {
        CommandExecutor.exec("cmd", "/c", String.format("start \"\" \"%s\"", file.toString()));
    }

    @SneakyThrows
    @Override
    public void startApp(@NonNull Path app) {
        CommandExecutor.exec("cmd", "/c", String.format("start /b \"X-Plane\" \"%s\"", app.toString()));
    }

    @Override
    public boolean isRunnable(@NonNull Path path) {
        return Files.isExecutable(path);
    }

    @Override
    @SneakyThrows
    public String getVersion(Path exePath) {
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
}
