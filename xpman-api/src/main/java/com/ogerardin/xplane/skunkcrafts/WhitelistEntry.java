package com.ogerardin.xplane.skunkcrafts;

/**
 * A single entry from a Skunkcrafts whitelist file.
 *
 * @param relativePath path relative to the addon base folder
 * @param expectedCRC  expected CRC32 checksum (null if not provided)
 * @param expectedSize expected file size in bytes (null if not provided)
 */
public record WhitelistEntry(String relativePath, Long expectedCRC, Long expectedSize) {}
