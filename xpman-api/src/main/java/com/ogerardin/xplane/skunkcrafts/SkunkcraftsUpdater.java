package com.ogerardin.xplane.skunkcrafts;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.UtilityClass;

import com.ogerardin.xplane.util.progress.ProgressListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.zip.CRC32;

/**
 * Static utility methods implementing the Skunkcrafts Updater protocol.
 *
 * <p>Protocol overview:
 * <ul>
 *   <li>Local: {@code skunkcrafts_updater.cfg} in addon base folder (pipe-delimited key|value)</li>
 *   <li>Local: optional {@code skunkcrafts_updater_beta.cfg} declaring the beta channel's module</li>
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
    private static final String BETA_CFG_FILENAME = "skunkcrafts_updater_beta.cfg";
    private static final String WHITELIST_FILENAME = "skunkcrafts_updater_whitelist.txt";
    private static final String BLACKLIST_FILENAME = "skunkcrafts_updater_blacklist.txt";
    private static final String IGNORE_FILENAME = "skunkcrafts_updater_ignore.txt";
    private static final String KEY_VERSION = "version";
    private static final String KEY_LIVERIES = "liveries";
    private static final String LINE_SPLIT = "\\r?\\n";

    private static final Set<String> METADATA_KEYS = Set.of(
            "name", KEY_VERSION, "build", "url", "manifest_url", "base_url", "module",
            "disabled", "locked", "zone", KEY_LIVERIES
    );

    private static final long CRC_SENTINEL_MISSING = 0xFFFFFFFFL;
    private static final long CRC_SENTINEL_SKIP = 0L;

    private static final String STATE_FILENAME = "skunkcrafts_updater_xpman.json";
    private static final Gson GSON = new Gson();

    /** HTTP validators of a downloaded file, for conditional revalidation. */
    private record Validator(String etag, String lastModified) {}

    /**
     * Global user preference (registered once at UI startup, read live): when true the beta
     * channel is treated as unavailable wherever availability is checked.
     */
    public static final AtomicReference<BooleanSupplier> ignoreBetaChannel = 
            new AtomicReference<>(() -> false);

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
        return findConfig(folder, CFG_FILENAME);
    }

    /**
     * Finds and parses the addon's beta channel config (skunkcrafts_updater_beta.cfg).
     *
     * @return parsed config, or null if no beta config file exists
     */
    public SkunkcraftsConfig findBetaConfig(Path folder) {
        return findConfig(folder, BETA_CFG_FILENAME);
    }

    private SkunkcraftsConfig findConfig(Path folder, String cfgFilename) {
        Path cfgFile = folder.resolve(cfgFilename);
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
            return fetchRemoteVersionChecked(moduleUrl);
        } catch (java.net.SocketTimeoutException e) {
            log.warn("Timeout fetching remote version from {}: {}", cfgUrl, e.getMessage());
        } catch (IOException e) {
            log.warn("Failed to fetch remote version from {}: {}", cfgUrl, e.getMessage());
        }
        return null;
    }

    /** Returns an absent label as null, but does not swallow a failed metadata request. */
    public String fetchRemoteVersionChecked(String moduleUrl) throws IOException {
        String content = fetchText(normalizeBaseUrl(moduleUrl) + CFG_FILENAME);
        for (String line : content.split(LINE_SPLIT)) {
            String[] parts = line.trim().split("\\|", 2);
            if (parts.length >= 2 && KEY_VERSION.equalsIgnoreCase(parts[0].trim())) return parts[1].trim();
        }
        return null;
    }

    /**
     * Fetches and parses the remote whitelist.
     */
    public List<WhitelistEntry> fetchRemoteWhitelist(String moduleUrl) {
        String url = normalizeBaseUrl(moduleUrl) + WHITELIST_FILENAME;
        try {
            return fetchRemoteWhitelistChecked(moduleUrl);
        } catch (java.net.SocketTimeoutException e) {
            log.warn("Timeout fetching remote whitelist from {}: {}", url, e.getMessage());
            return List.of();
        } catch (IOException e) {
            log.warn("Failed to fetch remote whitelist from {}: {}", url, e.getMessage());
            return List.of();
        }
    }

    private List<WhitelistEntry> fetchRemoteWhitelistChecked(String moduleUrl) throws IOException {
        return parseWhitelist(fetchText(normalizeBaseUrl(moduleUrl) + WHITELIST_FILENAME));
    }

    /**
     * Parses whitelist content (pipe-delimited: path|crc|size per line).
     */
    public List<WhitelistEntry> parseWhitelist(String content) {
        List<WhitelistEntry> entries = new ArrayList<>();
        for (String line : content.split(LINE_SPLIT)) {
            parseWhitelistLine(line).ifPresent(entries::add);
        }
        return entries;
    }

    private Optional<WhitelistEntry> parseWhitelistLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) return Optional.empty();

        String[] parts = trimmed.split("\\|");
        if (parts.length == 0) return Optional.empty();
        String path = parts[0].trim();
        if (METADATA_KEYS.contains(path.toLowerCase())) return Optional.empty();

        path = path.replace("\\", "/");
        while (path.startsWith("/")) path = path.substring(1);
        if (path.isEmpty()) return Optional.empty();

        Long crc = parts.length >= 2 && !parts[1].trim().isEmpty() ? parseCRC(parts[1].trim()) : null;
        Long size = parseSize(parts);
        return Optional.of(new WhitelistEntry(path, crc, size));
    }

    private static Long parseSize(String[] parts) {
        if (parts.length < 3 || parts[2].trim().isEmpty()) return null;
        try {
            return Long.parseLong(parts[2].trim());
        } catch (NumberFormatException _) {
            return null;
        }
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
        for (String line : content.split(LINE_SPLIT)) {
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
     * Computes which files need to be downloaded (missing, CRC/size mismatch, or a proven-stale
     * manifest entry whose served file changed since we recorded it).
     */
    public List<WhitelistEntry> computeFilesToUpdate(
            Path baseFolder,
            String moduleUrl,
            List<WhitelistEntry> whitelist,
            Set<String> ignoreSet
    ) throws IOException {
        String baseUrl = normalizeBaseUrl(moduleUrl);
        Map<String, SkunkcraftsXpmanState.StaleManifestEntry> stale = loadStaleEntries(baseFolder);
        List<WhitelistEntry> toUpdate = new ArrayList<>();

        for (WhitelistEntry entry : whitelist) {
            if (!isIgnored(entry.relativePath(), ignoreSet)
                    && needsUpdate(entry, baseFolder.resolve(entry.relativePath()), baseUrl, stale)) toUpdate.add(entry);
        }

        return toUpdate;
    }

    private boolean needsUpdate(WhitelistEntry entry, Path localFile, String baseUrl,
            Map<String, SkunkcraftsXpmanState.StaleManifestEntry> stale) throws IOException {
        if (!Files.exists(localFile)) return true;
        if (entry.expectedCRC() != null) {
            long remoteCRC = entry.expectedCRC();
            if (remoteCRC == CRC_SENTINEL_MISSING || remoteCRC == CRC_SENTINEL_SKIP) return false;
            long localCRC = computeCRC32(localFile);
            if (localCRC == remoteCRC) return false;
            // The manifest disagrees with the local file: if we previously proved this entry stale
            // and the local file still matches what the repository then served, the file is current
            // unless the repository now serves different bytes.
            var record = stale.get(staleKey(baseUrl, entry.relativePath()));
            if (record != null && record.manifestCrc() == remoteCRC && record.servedCrc() == localCRC) {
                return !servedFileUnchanged(baseUrl + entry.relativePath(), record);
            }
            return true;
        }
        return entry.expectedSize() != null && Files.size(localFile) != entry.expectedSize();
    }

    /** Conditional GET: true when the served file is unchanged (or cannot be revalidated). */
    private static boolean servedFileUnchanged(String fileUrl, SkunkcraftsXpmanState.StaleManifestEntry record) {
        // shortcut: without stored validators we suppress without asking — a server that never sends
        // ETag/Last-Modified could hide a republish until its manifest is fixed; upgrade to a byte-compare then
        if (record.etag() == null && record.lastModified() == null) return true;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) encodeUrl(fileUrl).openConnection();
            if (record.etag() != null) conn.setRequestProperty("If-None-Match", record.etag());
            if (record.lastModified() != null) conn.setRequestProperty("If-Modified-Since", record.lastModified());
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "XPman/1.0");
            return conn.getResponseCode() == 304;
        } catch (Exception e) {
            log.warn("Failed to revalidate {}: assuming unchanged", fileUrl, e);
            return true;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String staleKey(String baseUrl, String relativePath) {
        return baseUrl + "|" + relativePath;
    }

    private static Map<String, SkunkcraftsXpmanState.StaleManifestEntry> loadStaleEntries(Path baseFolder) {
        Path file = baseFolder.resolve(STATE_FILENAME);
        if (!Files.exists(file)) return new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            var state = GSON.fromJson(reader, SkunkcraftsXpmanState.class);
            if (state != null && state.stale() != null) return new HashMap<>(state.stale());
        } catch (Exception e) {
            log.warn("Failed to read {}", file, e);
        }
        return new HashMap<>();
    }

    private static void saveStaleEntries(Path baseFolder, Map<String, SkunkcraftsXpmanState.StaleManifestEntry> stale) {
        Path file = baseFolder.resolve(STATE_FILENAME);
        try {
            if (stale.isEmpty()) {
                Files.deleteIfExists(file);
            } else {
                Files.writeString(file, GSON.toJson(new SkunkcraftsXpmanState(stale)));
            }
        } catch (IOException e) {
            log.warn("Failed to save {}", file, e);
        }
    }

    /**
     * Computes the files to update: fetches the remote whitelist and blacklist, computes the ignore set,
     * and returns a summary with the file count and their total download size.
     *
     * @param baseFolder the base folder of the addon
     * @param config the Skunkcrafts configuration
     * @return the summary of files to update; discovery and local read failures propagate
     */
    public SkunkcraftsUpdateSummary computeFilesToUpdateSummary(Path baseFolder, SkunkcraftsConfig config) throws IOException {
        List<WhitelistEntry> filesToUpdate = fetchAndDiff(baseFolder, config);
        long totalSize = filesToUpdate.stream()
                .mapToLong(entry -> entry.expectedSize() != null ? entry.expectedSize() : fetchRemoteFileSize(config.moduleUrl(), entry))
                .sum();
        return new SkunkcraftsUpdateSummary(filesToUpdate.size(), totalSize);
    }

    /**
     * Fetches the size of a single remote file via HTTP HEAD, falling back to 0 if unavailable.
     */
    private static long fetchRemoteFileSize(String moduleUrl, WhitelistEntry entry) {
        String url = normalizeBaseUrl(moduleUrl) + entry.relativePath();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) encodeUrl(url).openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            return conn.getResponseCode() == 200 ? conn.getContentLengthLong() : 0;
        } catch (Exception e) {
            log.warn("Failed to fetch file size for {}", url, e);
            return 0;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static List<WhitelistEntry> fetchAndDiff(Path baseFolder, SkunkcraftsConfig config) throws IOException {
        return computeFilesToUpdate(baseFolder, config.moduleUrl(), fetchRemoteWhitelistChecked(config.moduleUrl()), ignoreSet(baseFolder, config));
    }

    /** The ignore set of a channel: its remote blacklist, the local user ignores, and liveries when excluded. */
    private static Set<String> ignoreSet(Path baseFolder, SkunkcraftsConfig config) {
        Set<String> ignoreSet = new HashSet<>();
        ignoreSet.addAll(fetchRemoteBlacklist(config.moduleUrl()));
        ignoreSet.addAll(parseLocalIgnore(baseFolder));
        if (!config.liveries()) {
            ignoreSet.add(KEY_LIVERIES);
        }
        return ignoreSet;
    }

    /**
     * Computes the channel status of an addon from both channels' whitelists;
     * {@link SkunkcraftsChannelStatus#NONE} when not updatable. Domain classes call this once
     * from a lazy field so the UI never pays it on the FX thread.
     */
    public SkunkcraftsChannelStatus channelStatus(SkunkcraftsUpdatable updatable) {
        if (!updatable.isSkunkcraftsUpdatable()) {
            return SkunkcraftsChannelStatus.NONE;
        }
        return computeChannelStatus(
                updatable.getSkunkcraftsFolder(), updatable.getSkunkcraftsConfig(), updatable.getSkunkcraftsBetaConfig());
    }

    /**
     * Computes content-truth availability for both channels: a channel has an update when its whitelist
     * diff against the local files is non-empty. The beta channel is additionally suppressed when its
     * whitelist is identical to the stable one — it offers nothing distinct whatever the version labels say.
     * The beta channel's latest version label is fetched only when the beta channel is offered.
     */
    public SkunkcraftsChannelStatus computeChannelStatus(Path baseFolder, SkunkcraftsConfig stableCfg, SkunkcraftsConfig betaCfg) {
        try {
            var status = computeChannelStatusChecked(baseFolder, stableCfg, betaCfg);
            return new SkunkcraftsChannelStatus(status.stableUpdateAvailable(), status.betaUpdateAvailable(),
                    status.betaUpdateAvailable() ? fetchRemoteVersion(betaCfg.moduleUrl()) : null);
        } catch (IOException e) {
            log.warn("Failed to check channel status", e);
            if (betaCfg != null) return computeChannelStatus(baseFolder, stableCfg, null);
            return SkunkcraftsChannelStatus.NONE;
        }
    }

    /** Checks content availability without version labels, preserving discovery failures. */
    public SkunkcraftsChannelStatus computeChannelStatusChecked(Path baseFolder, SkunkcraftsConfig stableCfg, SkunkcraftsConfig betaCfg) throws IOException {
        if (stableCfg == null || stableCfg.moduleUrl() == null) {
            return SkunkcraftsChannelStatus.NONE;
        }

        List<WhitelistEntry> stableWhitelist = fetchRemoteWhitelistChecked(stableCfg.moduleUrl());
        boolean stable = !computeFilesToUpdate(baseFolder, stableCfg.moduleUrl(), stableWhitelist, ignoreSet(baseFolder, stableCfg)).isEmpty();

        if (betaCfg == null || betaCfg.moduleUrl() == null) {
            return new SkunkcraftsChannelStatus(stable, false, null);
        }

        List<WhitelistEntry> betaWhitelist = fetchRemoteWhitelistChecked(betaCfg.moduleUrl());
        if (betaWhitelist.equals(stableWhitelist)) {
            return new SkunkcraftsChannelStatus(stable, false, null);
        }

        boolean beta = !computeFilesToUpdate(baseFolder, betaCfg.moduleUrl(), betaWhitelist, ignoreSet(baseFolder, betaCfg)).isEmpty();
        return new SkunkcraftsChannelStatus(stable, beta, null);
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
        int failedVerifications = 0;
        Map<String, SkunkcraftsXpmanState.StaleManifestEntry> stale = loadStaleEntries(baseFolder);
        boolean staleDirty = false;

        try {
            for (WhitelistEntry entry : filesToUpdate) {
                String filename = Path.of(entry.relativePath()).getFileName().toString();
                progress.progress((double) done / total, "Downloading " + filename + "...");

                Path localFile = baseFolder.resolve(entry.relativePath());
                Path parentDir = localFile.getParent();
                if (parentDir != null) {
                    Files.createDirectories(parentDir);
                }

                String fileUrl = baseUrl + entry.relativePath().replace("\\", "/");
                Validator validator = downloadFile(fileUrl, localFile);

                // A CRC mismatch means the repository's manifest is stale, not that the download
                // is corrupt (the official updater logs "bad CRC32" per file and carries on);
                // keep what the repository actually serves, remember the proof so the entry stops
                // resurfacing, and forget it once the manifest agrees again.
                if (entry.expectedCRC() != null) {
                    long remoteCRC = entry.expectedCRC();
                    if (remoteCRC != CRC_SENTINEL_MISSING && remoteCRC != CRC_SENTINEL_SKIP) {
                        long actualCRC = computeCRC32(localFile);
                        if (actualCRC != remoteCRC) {
                            log.warn("Failed to verify {}: expected CRC {} but got {} — keeping the downloaded file; the repository's file list is probably stale",
                                    entry.relativePath(), Long.toHexString(remoteCRC), Long.toHexString(actualCRC));
                            failedVerifications++;
                            stale.put(staleKey(baseUrl, entry.relativePath()), new SkunkcraftsXpmanState.StaleManifestEntry(
                                    remoteCRC, actualCRC, validator.etag(), validator.lastModified()));
                            staleDirty = true;
                        } else if (stale.remove(staleKey(baseUrl, entry.relativePath())) != null) {
                            staleDirty = true;
                        }
                    }
                }

                done++;
            }

            progress.progress(1.0, failedVerifications == 0 ? "Update complete"
                    : "Update completed, but " + failedVerifications
                    + " file(s) could not be verified (the repository's file list may be stale)");
        } finally {
            if (staleDirty) saveStaleEntries(baseFolder, stale);
        }
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
        List<WhitelistEntry> whitelist = fetchRemoteWhitelistChecked(config.moduleUrl());

        Set<String> ignoreSet = new HashSet<>();
        ignoreSet.addAll(fetchRemoteBlacklist(config.moduleUrl()));
        ignoreSet.addAll(parseLocalIgnore(baseFolder));

        if (!config.liveries()) {
            ignoreSet.add(KEY_LIVERIES);
        }

        progress.progress(-1.0, "Computing files to update...");
        List<WhitelistEntry> filesToUpdate = computeFilesToUpdate(baseFolder, config.moduleUrl(), whitelist, ignoreSet);

        if (filesToUpdate.isEmpty()) {
            progress.progress(1.0, "Already up to date");
            return;
        }

        downloadAndApply(baseFolder, config.moduleUrl(), filesToUpdate, progress);

        String remoteVersion = fetchRemoteVersion(config.moduleUrl());
        if (remoteVersion != null) {
            writeVersion(baseFolder.resolve(CFG_FILENAME), remoteVersion);
        }
    }

    /**
     * Updates the version field in the local Skunkcrafts config file.
     */
    private static void writeVersion(Path cfgFile, String newVersion) throws IOException {
        List<String> lines = Files.readAllLines(cfgFile);
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).trim().split("\\|", 2);
            if (parts.length >= 2 && KEY_VERSION.equalsIgnoreCase(parts[0].trim())) {
                lines.set(i, "version|" + newVersion);
                found = true;
                break;
            }
        }
        if (found) {
            Files.write(cfgFile, lines);
        }
    }

    /**
     * Converts a URL string that may contain unencoded characters (e.g. spaces in filenames) into a
     * properly percent-encoded {@link URL}.
     */
    public static URL encodeUrl(String urlStr) throws IOException {
        try {
            URL raw = new URL(urlStr);
            return new URI(raw.getProtocol(), raw.getAuthority(), raw.getPath(), raw.getQuery(), null).toURL();
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Invalid URL: " + urlStr, e);
        }
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
        URL url = encodeUrl(urlStr);
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

    private Validator downloadFile(String urlStr, Path destination) throws IOException {
        Path tempFile = destination.resolveSibling(destination.getFileName() + ".tmp");
        try {
            URL url = encodeUrl(urlStr);
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
            String etag = conn.getHeaderField("ETag");
            String lastModified = conn.getHeaderField("Last-Modified");

            try (InputStream is = conn.getInputStream()) {
                Files.copy(is, tempFile);
            } finally {
                conn.disconnect();
            }

            Files.move(tempFile, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return new Validator(etag, lastModified);
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
        } catch (NumberFormatException _) {
            try {
                long value = Long.parseUnsignedLong(s, 16);
                return value & 0xFFFFFFFFL;
            } catch (NumberFormatException _) {
                return null;
            }
        }
    }
}
