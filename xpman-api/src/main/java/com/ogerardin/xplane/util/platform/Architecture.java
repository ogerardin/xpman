package com.ogerardin.xplane.util.platform;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Instruction-set architectures an X-Plane plugin binary may target on macOS.
 * Provides a factory from raw Mach-O cputype values so the platform parser flows into a label-bearing enum.
 */
@Getter
@RequiredArgsConstructor
public enum Architecture {

	X86_64(0x01000007, "x86_64"),
	ARM64(0x0100000C, "arm64");

	// Mach-O cputype constants from <mach/machine.h>, with CPU_ARCH_ABI64 = 0x01000000 folded in
	private final int cputype;
	private final String displayName;

	/** Returns the matching architecture, or empty for any unknown cputype — permissive on doubt. */
	public static Optional<Architecture> ofMachOCpuType(int cputype) {
		for (Architecture arch : values()) {
			if (arch.cputype == cputype) return Optional.of(arch);
		}
		return Optional.empty();
	}

}
