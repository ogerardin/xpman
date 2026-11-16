package com.ogerardin.xpman.test;

import com.ogerardin.xpman.util.jfx.UpdateUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SkunkcraftsUpdateFailureMessageTest {

    @Test
    void translatesHttpFailuresWithoutExposingTheDownloadUrl() {
        IOException failure = new IOException("HTTP 502 from https://cdn.example.com/private/file.xpl");

        assertThat(UpdateUtil.friendlyUpdateFailureMessage(failure),
                is("The update server returned an error (HTTP 502). The server may be temporarily unavailable — please try again later."));
    }

    @Test
    void displaysTheRootCauseMessageForOtherFailures() {
        IOException failure = new IOException("Update failed", new IOException("Disk is full"));

        assertThat(UpdateUtil.friendlyUpdateFailureMessage(failure), is("Disk is full"));
    }

    @Test
    void usesAFallbackWhenFailureHasNoMessage() {
        assertThat(UpdateUtil.friendlyUpdateFailureMessage(new IOException()), is("An unexpected error occurred."));
    }
}
