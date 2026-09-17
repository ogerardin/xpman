package com.ogerardin.xplane.skunkcrafts;

import lombok.extern.slf4j.Slf4j;
import lombok.experimental.UtilityClass;

import com.ogerardin.xplane.util.progress.ProgressListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * Static utility methods implementing the Skunkcrafts Updater protocol.
 *
 * <p>Protocol overview:
 * <ul>
 *   <li>Local: {@code skunkcrafts_updater.cfg} in addon base folder (pipe-delimited key|value)</li>
 *   <li>Remote: {@code {moduleUrl}/skunkcrafts_updater.cfg} for version check</li>
 *   <li>Remote: {@code {moduleUrl}/skunkcrafts_updater_whitelist.txt} for file manifest</li>
 *   <li>Remote: {@code {moduleUrl}/skunkcrafts_updater_blacklist.txt} for excluded files</li>
 *   <li>Local: {@code skunkcrafts_updater_ignore.txt} for user-side exclusions</li>
 * </ul>
 */
@Slf4j
@UtilityClass
public class SkunkcraftsUpdater {

    private static final String CFG_FILENAME = "skunkcrafts_updater.cfg";
    private static final String WHITELIST_FILENAME = "skunkcrafts_updater_whitelist.txt";
    private static final String BLACKLIST_FILENAME = "skunkcrafts_updater_blacklist.txt";
    private static final String IGNORE_FILENAME = "skunkcrafts_updater_ignore.txt";

    private static final Set<String> METADATA_KEYS = Set.of(
            "name", "version", "build", "url", "manifest_url", "base_url", "module",
            "disabled", "locked", "zone", "liveries"
    );

    private static final long CRC_SENTINEL_MISSING = 0xFFFFFFFFL;
    private static final long CRC_SENTINEL_SKIP = 0L;

    /**
     * Checks if the addon is locked by the developer (updates temporarily unavailable).
     *
     * @param folder the addon base folder
     * @return true if locked, false if not locked or no config found
     */
    public boolean isLocked(Path folder) {
        SkunkcraftsConfig config = findConfig(folder);
        return config != null && config.locked();
    }

    /**
     * Finds and parses the Skunkcrafts config file in the given folder.
     *
     * @return parsed config, or null if no config file exists
     */
    public SkunkcraftsConfig findConfig(Path folder) {
        Path cfgFile = folder.resolve(CFG_FILENAME);
        if (!Files.exists(cfgFile)) {
            return null;
        }
        try {
            return SkunkcraftsConfig.parse(cfgFile);
        } catch (IOException e) {
            log.warn("Failed to parse Skunkcrafts config: {}", cfgFile, e);
            return null;
        }
    }

    /**
     * Fetches the remote version from the Skunkcrafts module URL.
     *
     * @param moduleUrl the base URL from the local config
     * @return the remote version string, or null if unavailable
     */
    public String fetchRemoteVersion(String moduleUrl) {
        String cfgUrl = normalizeBaseUrl(moduleUrl) + CFG_FILENAME;
        try {
            String content = fetchText(cfgUrl);
            for (String line : content.split("\\r?\\n")) {
                String[] parts = line.trim().split("\\|", 2);
                if (parts.length >= 2 && "version".equalsIgnoreCase(parts[0].trim())) {
                    return parts[1].trim();
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            log.warn("Timeout fetching remote version from {}: {}", cfgUrl, e.getMessage());
        } catch (IOException e) {
            log.warn("Failed to fetch remote version from {}: {}", cfgUrl, e.getMessage());
        }
        return null;
    }

    /**
     * Fetches and parses the remote whitelist.
     */
    public List<WhitelistEntry> fetchRemoteWhitelist(String moduleUrl) {
        String url = normalizeBaseUrl(moduleUrl) + WHITELIST_FILENAME;
        try {
            String content = fetchText(url);
            return parseWhitelist(content);
        } catch (java.net.SocketTimeoutException e) {
            log.warn("Timeout fetching remote whitelist from {}: {}", url, e.getMessage());
            return List.of();
        } catch (IOException e) {
            log.warn("Failed to fetch remote whitelist from {}: {}", url, e.getMessage());
            return List.of();
        }
    }

    /**
     * Parses whitelist content (pipe-delimited: path|crc|size per line).
     */
    public List<WhitelistEntry> parseWhitelist(String content) {
        List<WhitelistEntry> entries = new ArrayList<>();
        for (String line : content.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) continue;

            String[] parts = trimmed.split("\\|");
            if (parts.length == 0) continue;

            String firstPart = parts[0].trim();
            if (METADATA_KEYS.contains(firstPart.toLowerCase())) continue;

            String path = firstPart.replace("\\", "/");
            while (path.startsWith("/")) path = path.substring(1);
            if (path.isEmpty()) continue;

            Long crc = null;
            Long size = null;

            if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                crc = parseCRC(parts[1].trim());
            }
            if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                try {
                    size = Long.parseLong(parts[2].trim());
                } catch (NumberFormatException ignored) {}
            }

            entries.add(new WhitelistEntry(path, crc, size));
        }
        return entries;
    }

    /**
     * Fetches and parses the remote blacklist.
     */
    public Set<String> fetchRemoteBlacklist(String moduleUrl) {
        String url = normalizeBaseUrl(moduleUrl) + BLACKLIST_FILENAME;
        try {
            String content = fetchText(url);
            return parseBlacklist(content);
        } catch (java.net.SocketTimeoutException e) {
            log.warn("Timeout fetching remote blacklist from {}: {}", url, e.getMessage());
            return Set.of();
        } catch (IOException e) {
            log.warn("Failed to fetch remote blacklist from {}: {}", url, e.getMessage());
            return Set.of();
        }
    }

    /**
     * Parses blacklist/ignore content (one path per line).
     */
    public Set<String> parseBlacklist(String content) {
        Set<String> paths = new HashSet<>();
        for (String line : content.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) continue;
            String normalized = trimmed.replace("\\", "/");
            while (normalized.startsWith("/")) normalized = normalized.substring(1);
            while (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
            paths.add(normalized);
        }
        return paths;
    }

    /**
     * Reads the local ignore file from the addon folder.
     */
    public Set<String> parseLocalIgnore(Path folder) {
        Path ignoreFile = folder.resolve(IGNORE_FILENAME);
        if (!Files.exists(ignoreFile)) {
            return Set.of();
        }
        try {
            return parseBlacklist(Files.readString(ignoreFile));
        } catch (IOException e) {
            log.debug("Failed to read local ignore file: {}", ignoreFile, e);
            return Set.of();
        }
    }

    /**
     * Checks if a path should be ignored based on the ignore set.
     */
    public boolean isIgnored(String relativePath, Set<String> ignoreSet) {
        String normalized = relativePath.replace("\\", "/");
        for (String entry : ignoreSet) {
            if (normalized.equals(entry) || normalized.startsWith(entry + "/")) {
                return true;
            }
            if (entry.startsWith("*") && normalized.endsWith(entry.substring(1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes CRC32 checksum of a file.
     */
    public long computeCRC32(Path file) throws IOException {
        CRC32 crc = new CRC32();
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[65536];
            int read;
            while ((read = is.read(buffer)) != -1) {
                crc.update(buffer, 0, read);
            }
        }
        return crc.getValue();
    }

    /**
     * Computes which files need to be downloaded (missing or CRC/size mismatch).
     */
    public List<WhitelistEntry> computeFilesToUpdate(
            Path baseFolder,
            List<WhitelistEntry> whitelist,
            Set<String> ignoreSet
    ) throws IOException {
        List<WhitelistEntry> toUpdate = new ArrayList<>();

        for (WhitelistEntry entry : whitelist) {
            if (isIgnored(entry.relativePath(), ignoreSet)) continue;

            Path localFile = baseFolder.resolve(entry.relativePath());

            if (!Files.exists(localFile)) {
                toUpdate.add(entry);
                continue;
            }

            if (entry.expectedCRC() != null) {
                long remoteCRC = entry.expectedCRC();
                if (remoteCRC == CRC_SENTINEL_MISSING || remoteCRC == CRC_SENTINEL_SKIP) {
                    continue;
                }
                long localCRC = computeCRC32(localFile);
                if (localCRC != remoteCRC) {
                    toUpdate.add(entry);
                }
            } else if (entry.expectedSize() != null) {
                long localSize = Files.size(localFile);
                if (localSize != entry.expectedSize()) {
                    toUpdate.add(entry);
                }
            }
        }

        return toUpdate;
    }

    /**
     * Computes the number of files that need to be updated.
     * This is a convenience method that handles the full computation: fetching remote whitelist,
     * computing ignore set, and counting files to update.
     *
     * @param baseFolder the base folder of the addon
     * @param config the Skunkcrafts configuration
     * @return the number of files to update, or 0 if computation fails
     */
    public int computeFilesToUpdateCount(Path baseFolder, SkunkcraftsConfig config) {
        try {
            // Fetch remote whitelist
            List<WhitelistEntry> whitelist = fetchRemoteWhitelist(config.moduleUrl());
            
            // Compute ignore set
            Set<String> ignoreSet = new HashSet<>();
            ignoreSet.addAll(fetchRemoteBlacklist(config.moduleUrl()));
            ignoreSet.addAll(parseLocalIgnore(baseFolder));
            
            if (!config.liveries()) {
                ignoreSet.add("liveries");
            }
            
            // Compute files to update
            List<WhitelistEntry> filesToUpdate = computeFilesToUpdate(baseFolder, whitelist, ignoreSet);
            
            return filesToUpdate.size();
        } catch (Exception e) {
            log.warn("Failed to compute files to update", e);
            return 0;
        }
    }

    /**
     * Downloads and applies updates for the given files.
     */
    public void downloadAndApply(
            Path baseFolder,
            String moduleUrl,
            List<WhitelistEntry> filesToUpdate,
            ProgressListener progress
    ) throws IOException {
        String baseUrl = normalizeBaseUrl(moduleUrl);
        int total = filesToUpdate.size();
        int done = 0;

        for (WhitelistEntry entry : filesToUpdate) {
            String filename = Path.of(entry.relativePath()).getFileName().toString();
            progress.progress((double) done / total, "Downloading " + filename + "...");

            Path localFile = baseFolder.resolve(entry.relativePath());
            Path parentDir = localFile.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }

            String fileUrl = baseUrl + entry.relativePath().replace("\\", "/");
            downloadFile(fileUrl, localFile);

            // Post-download validation: verify CRC if expected
            if (entry.expectedCRC() != null) {
                long remoteCRC = entry.expectedCRC();
                if (remoteCRC != CRC_SENTINEL_MISSING && remoteCRC != CRC_SENTINEL_SKIP) {
                    long actualCRC = computeCRC32(localFile);
                    if (actualCRC != remoteCRC) {
                        throw new IOException("Download verification failed for " + entry.relativePath()
                                + ": expected CRC " + Long.toHexString(remoteCRC)
                                + " but got " + Long.toHexString(actualCRC));
                    }
                }
            }

            done++;
        }

        progress.progress(1.0, "Update complete");
    }

    /**
     * Full update workflow: fetch remote data, compute diff, download changes.
     */
    public void applyUpdate(
            Path baseFolder,
            SkunkcraftsConfig config,
            ProgressListener progress
    ) throws IOException {
        if (config.moduleUrl() == null) {
            throw new IOException("No module URL in Skunkcrafts config");
        }

        progress.progress(-1.0, "Fetching remote whitelist...");
        List<WhitelistEntry> whitelist = fetchRemoteWhitelist(config.moduleUrl());

        Set<String> ignoreSet = new HashSet<>();
        ignoreSet.addAll(fetchRemoteBlacklist(config.moduleUrl()));
        ignoreSet.addAll(parseLocalIgnore(baseFolder));

        if (!config.liveries()) {
            ignoreSet.add("liveries");
        }

        progress.progress(-1.0, "Computing files to update...");
        List<WhitelistEntry> filesToUpdate = computeFilesToUpdate(baseFolder, whitelist, ignoreSet);

        if (filesToUpdate.isEmpty()) {
            progress.progress(1.0, "Already up to date");
            return;
        }

        downloadAndApply(baseFolder, config.moduleUrl(), filesToUpdate, progress);
    }

    private String normalizeBaseUrl(String url) {
        if (url == null) return "";
        String base = url.replace("\\", "/");
        if (base.endsWith(CFG_FILENAME)) {
            base = base.substring(0, base.length() - CFG_FILENAME.length());
        }
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base;
    }

    private String fetchText(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "XPman/1.0");

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            conn.disconnect();
            throw new IOException("HTTP " + status + " from " + urlStr);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private void downloadFile(String urlStr, Path destination) throws IOException {
        Path tempFile = destination.resolveSibling(destination.getFileName() + ".tmp");
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "XPman/1.0");

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                conn.disconnect();
                throw new IOException("HTTP " + status + " from " + urlStr);
            }

            try (InputStream is = conn.getInputStream()) {
                Files.copy(is, tempFile);
            } finally {
                conn.disconnect();
            }

            Files.move(tempFile, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private Long parseCRC(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            long value;
            if (s.startsWith("0x") || s.startsWith("0X")) {
                value = Long.parseUnsignedLong(s.substring(2), 16);
            } else {
                value = Long.parseUnsignedLong(s);
            }
            // Normalize to 32 bits (CRC32 is inherently 32-bit)
            return value & 0xFFFFFFFFL;
        } catch (NumberFormatException e) {
            try {
                long value = Long.parseUnsignedLong(s, 16);
                return value & 0xFFFFFFFFL;
            } catch (NumberFormatException e2) {
                return null;
            }
        }
    }
}
