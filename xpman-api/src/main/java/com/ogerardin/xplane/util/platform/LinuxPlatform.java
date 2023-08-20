package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.util.exec.CommandExecutor;
import com.ogerardin.xplane.util.exec.ExecResults;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class LinuxPlatform implements Platform {

    @Getter
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
        // there seem to be no symbol in the ELF symbol table pointing to the version string :(
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
        return null;
    }
}
