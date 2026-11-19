package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.log.LogEntry;
import com.ogerardin.xplane.log.SessionLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects missing or unloadable scenery assets from log entries.
 */
class MissingSceneryLogDiagnostics {

    private static final String UNKNOWN_PACK = "Unknown scenery pack";

    // Pattern to detect scenery asset failures
    private static final Pattern SCENERY_FAILURE_PATTERN = Pattern.compile(
            "(?i)(Failed to find resource|Missing object|Could not find object|Unable to load)"
    );

    // Pattern to extract asset path (handles quoted and unquoted paths)
    private static final Pattern ASSET_PATH_PATTERN = Pattern.compile(
            "(?i)(?:Failed to find resource|Missing object|Could not find object|Unable to load)\\s*[:\\s]*" +
            "(?:'([^']+)'|\"([^\"]+)\"|([^'\"\\s]+))"
    );

    // Pattern to extract pack name from path (supports both / and \\ separators)
    private static final Pattern PACK_NAME_PATTERN = Pattern.compile(
            "(?i)Custom Scenery[/\\\\]([^/\\\\]+)[/\\\\]"
    );

    private static final Set<String> SCENERY_EXTENSIONS = Set.of(
            ".obj", ".dsf", ".ter", ".pol", ".fac", ".for", ".png", ".dds", ".jpg", ".jpeg"
    );

    static List<InspectionMessage> collect(SessionLog log, Set<Integer> consumedLines) {
        List<InspectionMessage> findings = new ArrayList<>();

        // Track failures per pack
        Map<String, List<AssetFailure>> failuresByPack = new HashMap<>();

        for (int i = 0; i < log.getEntries().size(); i++) {
            LogEntry entry = log.getEntries().get(i);
            int lineNumber = i + 1;

            // Skip if already consumed by crash diagnostics
            if (consumedLines.contains(lineNumber)) {
                continue;
            }

            String message = entry.message();
            Matcher failureMatcher = SCENERY_FAILURE_PATTERN.matcher(message);
            if (!failureMatcher.find()) {
                continue;
            }

            // Extract asset path
            Matcher pathMatcher = ASSET_PATH_PATTERN.matcher(message);
            if (!pathMatcher.find()) {
                continue;
            }

            // Get the path from whichever group matched (single-quote, double-quote, or unquoted)
            String assetPath = pathMatcher.group(1) != null ? pathMatcher.group(1) :
                              pathMatcher.group(2) != null ? pathMatcher.group(2) :
                              pathMatcher.group(3);

            // Check if it's a scenery asset
            if (!isSceneryAsset(assetPath, message, entry.subsystem())) {
                continue;
            }

            // Extract pack name
            String packName = extractPackName(assetPath);

            // Determine failure type
            boolean isMissing = message.contains("Failed to find resource") ||
                               message.contains("Missing object") ||
                               message.contains("Could not find object");

            // Determine asset type
            String assetType = extractAssetType(assetPath);

            // Record failure
            AssetFailure failure = new AssetFailure(lineNumber, assetPath, assetType, isMissing);
            failuresByPack.computeIfAbsent(packName, k -> new ArrayList<>()).add(failure);

            // Mark line as consumed
            consumedLines.add(lineNumber);
        }

        // Create InspectionMessages per pack, separating missing vs load failures
        for (Map.Entry<String, List<AssetFailure>> entry : failuresByPack.entrySet()) {
            String packName = entry.getKey();
            List<AssetFailure> failures = entry.getValue();

            // Separate missing and load failures
            List<AssetFailure> missingAssets = new ArrayList<>();
            List<AssetFailure> loadFailures = new ArrayList<>();
            for (AssetFailure f : failures) {
                if (f.isMissing) {
                    missingAssets.add(f);
                } else {
                    loadFailures.add(f);
                }
            }

            // Create message for missing assets
            if (!missingAssets.isEmpty()) {
                Set<String> distinctAssets = new HashSet<>();
                for (AssetFailure f : missingAssets) {
                    distinctAssets.add(f.assetPath);
                }
                int assetCount = distinctAssets.size();
                String message = String.format("%d missing asset%s in scenery pack '%s'",
                        assetCount, assetCount != 1 ? "s" : "", packName);

                StringBuilder details = new StringBuilder();
                for (AssetFailure f : missingAssets) {
                    details.append(String.format("line %d: %s (%s)%n",
                            f.lineNumber, f.assetPath, f.assetType));
                }

                findings.add(InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .object(packName)
                        .message(message)
                        .details(details.toString().trim())
                        .searchQuery("X-Plane scenery asset " + packName)
                        .build());
            }

            // Create message for load failures
            if (!loadFailures.isEmpty()) {
                Set<String> distinctAssets = new HashSet<>();
                for (AssetFailure f : loadFailures) {
                    distinctAssets.add(f.assetPath);
                }
                int assetCount = distinctAssets.size();
                String message = String.format("%d asset%s could not be loaded in scenery pack '%s'",
                        assetCount, assetCount != 1 ? "s" : "", packName);

                StringBuilder details = new StringBuilder();
                for (AssetFailure f : loadFailures) {
                    details.append(String.format("line %d: %s (%s)%n",
                            f.lineNumber, f.assetPath, f.assetType));
                }

                findings.add(InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .object(packName)
                        .message(message)
                        .details(details.toString().trim())
                        .searchQuery("X-Plane scenery asset " + packName)
                        .build());
            }
        }

        return findings;
    }

    private static boolean isSceneryAsset(String path, String message, String subsystem) {
        // Require scenery context: either SCN subsystem or scenery path
        boolean hasSceneryContext = "SCN".equals(subsystem) ||
                                   path.toLowerCase().contains("custom scenery") ||
                                   message.toLowerCase().contains("custom scenery");

        if (!hasSceneryContext) {
            return false;
        }

        // Check if it has a scenery extension
        String lowerPath = path.toLowerCase();
        for (String ext : SCENERY_EXTENSIONS) {
            if (lowerPath.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }

    private static String extractPackName(String path) {
        Matcher matcher = PACK_NAME_PATTERN.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return UNKNOWN_PACK;
    }

    private static String extractAssetType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".obj")) {
            return "3D Object";
        } else if (lowerPath.endsWith(".dsf")) {
            return "Terrain Mesh";
        } else if (lowerPath.endsWith(".ter")) {
            return "Terrain";
        } else if (lowerPath.endsWith(".pol")) {
            return "Polygon";
        } else if (lowerPath.endsWith(".fac")) {
            return "Facade";
        } else if (lowerPath.endsWith(".for")) {
            return "Forest";
        } else if (lowerPath.endsWith(".png") || lowerPath.endsWith(".dds") ||
                   lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "Texture";
        } else {
            return "Asset";
        }
    }

    private static class AssetFailure {
        final int lineNumber;
        final String assetPath;
        final String assetType;
        final boolean isMissing;

        AssetFailure(int lineNumber, String assetPath, String assetType, boolean isMissing) {
            this.lineNumber = lineNumber;
            this.assetPath = assetPath;
            this.assetType = assetType;
            this.isMissing = isMissing;
        }
    }
}
