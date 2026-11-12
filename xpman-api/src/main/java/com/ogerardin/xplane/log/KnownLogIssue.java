package com.ogerardin.xplane.log;

import com.ogerardin.xplane.inspection.Severity;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/** Curated explanations and user-impact severities for recognized X-Plane log diagnostics. */
@Getter
public enum KnownLogIssue {
    APT_BAD_FREQUENCIES("The airport .* has lost some controllers due to bad frequencies", Severity.INFO,
            "Bundled airport data has invalid ATC frequencies, so X-Plane drops the affected controller positions. This affects ATC coverage only and normally needs no add-on repair."),
    APT_UNKNOWN_KEY("Unknown key '.*' for .* on line", Severity.INFO,
            "X-Plane ignored an unknown field in airport data. The affected airport metadata may be incomplete; no action is normally needed."),
    APT_OVERRIDE_CONFLICT("airport .* overrides code .* but it has already been overridden", Severity.WARN,
            "Multiple airport records claim the same location code. X-Plane keeps the higher-priority record, so the overridden airport data may not load."),
    APT_FREQ_CONFLICT("Found overlapping airports with overlapping controller freqs|Rewrote .* for being in conflict", Severity.WARN,
            "Nearby airports use overlapping ATC frequencies. X-Plane rewrites one frequency, which may affect ATC tuning."),
    SOUND_ERROR("snd error in file", Severity.WARN,
            "An aircraft FMOD sound definition is invalid, so one or more aircraft sounds may be missing. Update or reinstall the aircraft."),
    SOUND_POLYPHONIC("Event: .* is not a one-shot but is polyphonic fire-and-forget", Severity.WARN,
            "Aircraft FMOD sound events are misdeclared as one-shot when they should be polyphonic, so those sounds may not play correctly."),
    OBJ_LOD_RANGE("has a first LOD near distance of .* but it should start at 0", Severity.INFO,
            "This scenery object's level-of-detail starts away from zero, so it may appear late. This is usually cosmetic."),
    RUNLOOP_BACKED_UP("Runloop is backed up", Severity.INFO,
            "X-Plane's main loop temporarily fell behind, which may cause a short stutter. A single occurrence is usually transient."),
    AUTH_FAILURE("http error: 401|HTTP Error 401|JWT .*failed|Refresh Error Code|Product key check failed|failed to verify signature|The refresh token is invalid|JWT Token Expired|requested key was not found in the lockbox", Severity.INFO,
            "X-Plane's online authentication refresh failed. If this repeats, online services or licensing may not work; verify the connection and sign-in."),
    GFX_CAPABILITY("extension missing proc|doesn't support required extension|Failed to initialize OpenGL bridge", Severity.ERROR,
            "X-Plane could not initialize a required graphics capability, so rendering may fail. Update the graphics driver or use a compatible graphics API.");

    private final Pattern pattern;
    private final Severity severity;
    private final String explanation;

    KnownLogIssue(String regex, Severity severity, String explanation) {
        this.pattern = Pattern.compile(regex);
        this.severity = severity;
        this.explanation = explanation;
    }

    public static Optional<KnownLogIssue> matching(String message) {
        return Arrays.stream(values()).filter(issue -> issue.pattern.matcher(message).find()).findFirst();
    }

    /** Patterns recognized as benign noise; matching lines are dropped from inspection results entirely. */
    private static final Pattern[] NOISE_PATTERNS = {
            Pattern.compile("Setting browser language to allow: .*"),
    };

    /** Returns true if the message matches a known noise pattern and should be suppressed. */
    public static boolean isNoise(String message) {
        return Arrays.stream(NOISE_PATTERNS).anyMatch(p -> p.matcher(message).find());
    }
}
