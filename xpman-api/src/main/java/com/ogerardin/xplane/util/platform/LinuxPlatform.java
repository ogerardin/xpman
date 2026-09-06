package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Slf4j
public class LinuxPlatform implements Platform {

    public final int osType = com.sun.jna.Platform.LINUX;

    @SneakyThrows
    @Override
    public void reveal(@NonNull Path path) {
        // https://askubuntu.com/a/1109917/325617
        String shellParam = String.format("gtk-launch \"$(xdg-mime query default inode/directory)\" '%s'", path);
        CommandExecutor.exec("sh", "-c", shellParam);
    }

    @SneakyThrows
    @Override
    public String getCpuType() {
        ExecResults exec = CommandExecutor.exec("uname", "-p");
        return exec.outputLines().get(0);
    }

    @SneakyThrows
    @Override
    public int getCpuCount() {
        ExecResults exec = CommandExecutor.exec("nproc", "--all");
        return Integer.parseInt(exec.outputLines().get(0));
    }

    @Override
    public String revealLabel() {
        return "Show in Files";
    }

    @SneakyThrows
    @Override
    public void openUrl(@NonNull URL url) {
        CommandExecutor.exec("xdg-open", url.toString());
    }

    @SneakyThrows
    @Override
    public void openFile(@NonNull Path file) {
        CommandExecutor.exec("xdg-open", file.toString());
    }

    @SneakyThrows
    @Override
    public void startApp(@NonNull Path app) {
        CommandExecutor.exec("sh", "-c", app.toString());
    }

    @Override
    public boolean isRunnable(@NonNull Path path) {
        return Files.isExecutable(path);
    }

    @Override
    @SneakyThrows
    public String getVersion(Path exePath) {
        // there seems to be no symbol in the ELF symbol table pointing to the version string :(
        return null;
    }

    // ponytail: heuristic version extraction via string scanning in .rodata section.
    // Upgrade to proper ELF symbol-based extraction if accuracy becomes critical.
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\b(\\d+\\.\\d+(?:\\.\\d+){0,2})\\b");

    @Override
    @SneakyThrows
    public String extractPluginVersion(Path xplFile) {
        ElfFile elf = ElfFile.from(Files.newInputStream(xplFile));

        for (int i = 0; i < elf.e_shnum; i++) {
            ElfSection section = elf.getSection(i);
            String name = section.header.getName();
            if (name != null && name.contains("rodata")) {
                String content = new String(section.getData());
                String version = findVersionNearPluginName(content, xplFile);
                if (version != null) return version;
            }
        }

        return null;
    }

    private String findVersionNearPluginName(String content, Path xplFile) {
        String folderName = xplFile.getParent().getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folderName = folderName.substring(0, folderName.length() - 2);
        }

        int namePos = content.indexOf(folderName);
        if (namePos < 0) return null;

        int searchEnd = Math.min(content.length(), namePos + 500);
        String searchArea = content.substring(namePos, searchEnd);
        Matcher m = VERSION_PATTERN.matcher(searchArea);
        return m.find() ? m.group(1) : null;
    }

    @Override
    public List<Path> getCandidateInstallBaseFolders(Path userHome) {
        List<Path> bases = new ArrayList<>();
        bases.add(userHome);
        bases.add(Paths.get("/opt"));
        bases.add(userHome.resolve(".steam/steam/steamapps/common"));
        bases.add(userHome.resolve(".local/share/Steam/steamapps/common"));
        return bases;
    }

    @Override
    public String pluginPathIdentifier() {
        return "lin";
    }
}
