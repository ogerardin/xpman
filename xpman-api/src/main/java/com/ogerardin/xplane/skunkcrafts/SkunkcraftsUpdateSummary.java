package com.ogerardin.xplane.skunkcrafts;

/**
 * Summary of the files that need to be downloaded for a Skunkcrafts-managed addon.
 *
 * @param fileCount number of files to update
 * @param totalSizeBytes total download size in bytes (sizes not provided in the whitelist count as 0)
 */
public record SkunkcraftsUpdateSummary(int fileCount, long totalSizeBytes) {}