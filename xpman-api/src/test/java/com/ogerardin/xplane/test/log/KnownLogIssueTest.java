package com.ogerardin.xplane.test.log;

import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.log.KnownLogIssue;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class KnownLogIssueTest {

    private static final Map<KnownLogIssue, String> SAMPLES = new LinkedHashMap<>();
    private static final Map<KnownLogIssue, Severity> severities = new LinkedHashMap<>();
    static {
        SAMPLES.put(KnownLogIssue.APT_BAD_FREQUENCIES,
                "The airport ETOU (Wiesbaden Erbenheim AAF) has lost some controllers due to bad frequencies.");
        SAMPLES.put(KnownLogIssue.APT_UNKNOWN_KEY,
                "Unknown key 'altimeter_setting' for '[H] Uniklinik Frankfurt a.M.' on line 434191.");
        SAMPLES.put(KnownLogIssue.APT_OVERRIDE_CONFLICT,
                "WARNING: airport Hubbard (ID TA49) overrides code 25TX but it has already been overridden by Chennault Airfield");
        SAMPLES.put(KnownLogIssue.APT_FREQ_CONFLICT,
                "Found overlapping airports with overlapping controller freqs at KCEA and KIAB!");
        SAMPLES.put(KnownLogIssue.SOUND_ERROR,
                "snd error in file: Aircraft/Laminar Research/McDonnell Douglas MD-82/fmod/MD80.snd line 1803");
        SAMPLES.put(KnownLogIssue.OBJ_LOD_RANGE,
                "The object Custom Scenery/KSEA Demo Area/object.obj has a first LOD near distance of 2000 but it should start at 0!");
        SAMPLES.put(KnownLogIssue.RUNLOOP_BACKED_UP,
                "Runloop is backed up. Current task count: 223, estimated runtime: 14.520s");
        SAMPLES.put(KnownLogIssue.AUTH_FAILURE,
                "JWT Signature Exception: 14 failed to verify signature: VerifyFinal failed");
        SAMPLES.put(KnownLogIssue.GFX_CAPABILITY,
                "GFX bridge doesn't support required extension GL_EXT_memory_object");
        SAMPLES.put(KnownLogIssue.SOUND_POLYPHONIC,
                "Event: event:/md80/equipment/switches/rot_med_single is not a one-shot but is polyphonic fire-and-forget.");

        severities.put(KnownLogIssue.APT_BAD_FREQUENCIES, Severity.INFO);
        severities.put(KnownLogIssue.APT_UNKNOWN_KEY, Severity.INFO);
        severities.put(KnownLogIssue.APT_OVERRIDE_CONFLICT, Severity.WARN);
        severities.put(KnownLogIssue.APT_FREQ_CONFLICT, Severity.WARN);
        severities.put(KnownLogIssue.SOUND_ERROR, Severity.WARN);
        severities.put(KnownLogIssue.OBJ_LOD_RANGE, Severity.INFO);
        severities.put(KnownLogIssue.RUNLOOP_BACKED_UP, Severity.INFO);
        severities.put(KnownLogIssue.AUTH_FAILURE, Severity.INFO);
        severities.put(KnownLogIssue.GFX_CAPABILITY, Severity.ERROR);
        severities.put(KnownLogIssue.SOUND_POLYPHONIC, Severity.WARN);
    }

    @Test
    void recognizesEachPinnedPatternWithExpectedSeverity() {
        SAMPLES.forEach((issue, sample) -> {
            assertThat(KnownLogIssue.matching(sample), is(java.util.Optional.of(issue)));
            assertThat(issue.getSeverity(), is(severities.get(issue)));
            assertThat(issue.getExplanation(), not(blankOrNullString()));
        });
    }

    @Test
    void jwtTokenExpiredMatchesAuthFailure() {
        String line = "0:00:00.000 E/JWT: JWT Token Expired: 14 token expired";
        assertThat(KnownLogIssue.matching(line), is(java.util.Optional.of(KnownLogIssue.AUTH_FAILURE)));
    }

    @Test
    void laminarBuildPathAuthErrorsMatchAuthFailure() {
        String httpError = "/Users/administrator/.gitlab-runner/builds/5iac3Er_D/0/x-plane/design/source_code/core/installer/license_mgr.cpp(237): error 536871313 - HTTP Error 401 (Unauthorized)";
        String lockbox = "/Users/administrator/.gitlab-runner/builds/5iac3Er_D/0/x-plane/design/source_code/core/installer/license_mgr.cpp(1333): error 1879068224 - The requested key was not found in the lockbox";
        assertThat(KnownLogIssue.matching(httpError), is(java.util.Optional.of(KnownLogIssue.AUTH_FAILURE)));
        assertThat(KnownLogIssue.matching(lockbox), is(java.util.Optional.of(KnownLogIssue.AUTH_FAILURE)));
    }

    @Test
    void browserLanguageNoiseIsSuppressed() {
        assertThat(KnownLogIssue.isNoise("Setting browser language to allow: en"), is(true));
        assertThat(KnownLogIssue.isNoise("Setting browser language to allow: fr"), is(true));
        assertThat(KnownLogIssue.isNoise("Some other warning"), is(false));
    }

    @Test
    void doesNotClassifyPluginLoadLines() {
        assertThat(KnownLogIssue.matching("Loaded: /x/y.zpl (sig)."), is(java.util.Optional.empty()));
    }
}
