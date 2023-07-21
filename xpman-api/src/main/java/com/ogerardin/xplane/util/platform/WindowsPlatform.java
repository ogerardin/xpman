package com.ogerardin.xplane.util.platform;

import com.kichik.pecoff4j.PE;
import com.kichik.pecoff4j.ResourceDirectory;
import com.kichik.pecoff4j.ResourceEntry;
import com.kichik.pecoff4j.constant.ResourceType;
import com.kichik.pecoff4j.io.PEParser;
import com.kichik.pecoff4j.io.ResourceParser;
import com.kichik.pecoff4j.resources.StringFileInfo;
import com.kichik.pecoff4j.resources.StringPair;
import com.kichik.pecoff4j.resources.StringTable;
import com.kichik.pecoff4j.resources.VersionInfo;
import com.kichik.pecoff4j.util.ResourceHelper;
import com.ogerardin.xplane.util.exec.CommandExecutor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
public class WindowsPlatform implements Platform {

    @Getter
    public final int osType = com.sun.jna.Platform.WINDOWS;

    @SneakyThrows
    @Override
    public void reveal(@NonNull Path path) {
        String explorerParam = "/select,\"" + path + "\"";
        CommandExecutor.exec("cmd", "/c", "explorer.exe " + explorerParam);
    }

    @Override
    public String getCpuType() {
        return System.getenv("PROCESSOR_IDENTIFIER");
    }

    @Override
    public int getCpuCount() {
        return Optional.ofNullable(System.getenv("NUMBER_OF_PROCESSORS"))
                .map(Integer::parseInt)
                .orElse(1);
    }

    @Override
    public String revealLabel() {
        return "Show in Explorer";
    }

    @SneakyThrows
    @Override
    public void openUrl(@NonNull URL url) {
        CommandExecutor.exec("cmd", "/c", String.format("start %s", url));
    }

    @SneakyThrows
    @Override
    public void openFile(@NonNull Path file) {
        CommandExecutor.exec("cmd", "/c", String.format("start \"\" \"%s\"", file));
    }

    @SneakyThrows
    @Override
    public void startApp(@NonNull Path app) {
        CommandExecutor.exec("cmd", "/c", String.format("start /b \"X-Plane\" \"%s\"", app));
    }

    @Override
    public boolean isRunnable(@NonNull Path path) {
        return path.endsWith(".exe");
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
