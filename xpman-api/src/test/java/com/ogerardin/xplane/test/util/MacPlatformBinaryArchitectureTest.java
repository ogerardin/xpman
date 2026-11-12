package com.ogerardin.xplane.test.util;

import com.ogerardin.xplane.util.platform.Architecture;
import com.ogerardin.xplane.util.platform.MacPlatform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * Exercises the pure file-reading Mach-O parser on synthetic headers. Runs on any CI OS
 * since the parser does not shell out.
 */
class MacPlatformBinaryArchitectureTest {

    private final MacPlatform platform = new MacPlatform();

    @TempDir
    Path tempDir;

    private static final int CPU_TYPE_X86_64 = 0x01000007;
    private static final int CPU_TYPE_ARM64 = 0x0100000C;
    private static final int MACH_O_MAGIC_64 = 0xFEEDFACF;
    private static final int MACH_O_CIGAM_64 = 0xCFFAEDFE;
    private static final int FAT_MAGIC_BE = 0xCAFEBABE;

    @Test
    void thinLittleEndianX86_64() throws IOException {
        Path bin = tempDir.resolve("thin-x64");
        Files.write(bin, thinLE(MACH_O_MAGIC_64, CPU_TYPE_X86_64));
        assertThat(platform.getBinaryArchitectures(bin), is(List.of(Architecture.X86_64)));
    }

    @Test
    void thinLittleEndianArm64() throws IOException {
        Path bin = tempDir.resolve("thin-arm64");
        Files.write(bin, thinLE(MACH_O_MAGIC_64, CPU_TYPE_ARM64));
        assertThat(platform.getBinaryArchitectures(bin), is(List.of(Architecture.ARM64)));
    }

    @Test
    void thinCigamByteFlipped() throws IOException {
        // CIGAM magic: file was written in the opposite byte order — parser must read cputype accordingly.
        Path bin = tempDir.resolve("cigam");
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(MACH_O_CIGAM_64);       // bytes in file: FE ED FA CF
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(CPU_TYPE_ARM64);        // bytes in file: 0C 00 00 01
        Files.write(bin, buf.array());
        assertThat(platform.getBinaryArchitectures(bin), is(List.of(Architecture.ARM64)));
    }

    @Test
    void fatUniversalX86_64AndArm64() throws IOException {
        Path bin = tempDir.resolve("fat-universal");
        Files.write(bin, fatBe(List.of(CPU_TYPE_X86_64, CPU_TYPE_ARM64)));
        assertThat(platform.getBinaryArchitectures(bin),
                containsInAnyOrder(Architecture.X86_64, Architecture.ARM64));
    }

    @Test
    void fatArm64Only() throws IOException {
        Path bin = tempDir.resolve("fat-arm64");
        Files.write(bin, fatBe(List.of(CPU_TYPE_ARM64)));
        assertThat(platform.getBinaryArchitectures(bin), is(List.of(Architecture.ARM64)));
    }

    @Test
    void unknownMagicReturnsEmpty() throws IOException {
        Path bin = tempDir.resolve("garbage");
        Files.write(bin, new byte[] { 0x7F, 'E', 'L', 'F', 0, 0, 0, 0, 0, 0 });
        assertThat(platform.getBinaryArchitectures(bin).isEmpty(), is(true));
    }

    @Test
    void fileTooSmallReturnsEmpty() throws IOException {
        Path bin = tempDir.resolve("tiny");
        Files.write(bin, new byte[] { (byte) 0xCF, (byte) 0xFA });
        assertThat(platform.getBinaryArchitectures(bin).isEmpty(), is(true));
    }

    @Test
    void unknownCputypeReturnsEmpty() throws IOException {
        Path bin = tempDir.resolve("unknown-cpu");
        Files.write(bin, thinLE(MACH_O_MAGIC_64, 0x99 /* not an Apple arch */));
        assertThat(platform.getBinaryArchitectures(bin).isEmpty(), is(true));
    }

    private static byte[] thinLE(int magic, int cputype) {
        ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(magic);
        buf.putInt(cputype);
        return buf.array();
    }

    private static byte[] fatBe(List<Integer> cputypes) {
        // 4 bytes magic + 4 bytes nfat_arch + 20 bytes per fat_arch.
        int nfat = cputypes.size();
        ByteBuffer buf = ByteBuffer.allocate(8 + nfat * 20).order(ByteOrder.BIG_ENDIAN);
        buf.putInt(FAT_MAGIC_BE);
        buf.putInt(nfat);
        for (int cpu : cputypes) {
            buf.putInt(cpu);                 // cputype
            buf.putInt(0); buf.putInt(0);    // cpusubtype, offset
            buf.putInt(0); buf.putInt(0);    // size, align
        }
        return buf.array();
    }
}
