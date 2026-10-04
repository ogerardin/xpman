package com.ogerardin.xplane.test.navdata;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.navdata.NavDataSetStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Hermetic tests for the four-state {@link NavDataSetStatus} traffic light.
 */
class NavDataSetStatusTest {

    private static InspectionResult resultWith(Severity... severities) {
        return InspectionResult.of(Arrays.stream(severities)
                .map(severity -> InspectionMessage.builder().severity(severity).message("m").build())
                .toList());
    }

    @Test
    void absentIsInactive() {
        assertThat(NavDataSetStatus.of(false, false, resultWith(Severity.WARN)),
                is(NavDataSetStatus.INACTIVE));
    }

    @Test
    void ignoredIsInactiveEvenWhenItCarriesAnError() {
        assertThat(NavDataSetStatus.of(true, true, resultWith(Severity.ERROR)),
                is(NavDataSetStatus.INACTIVE));
    }

    @Test
    void presentAndCleanIsOk() {
        assertThat(NavDataSetStatus.of(true, false, resultWith(Severity.INFO, Severity.INFO)),
                is(NavDataSetStatus.OK));
    }

    @Test
    void presentWithNoMessagesIsOk() {
        assertThat(NavDataSetStatus.of(true, false, InspectionResult.empty()),
                is(NavDataSetStatus.OK));
    }

    @Test
    void presentWithWarningsIsWarning() {
        assertThat(NavDataSetStatus.of(true, false, resultWith(Severity.INFO, Severity.WARN)),
                is(NavDataSetStatus.WARNING));
    }

    @Test
    void theWorstSeverityWins() {
        assertThat(NavDataSetStatus.of(true, false, resultWith(Severity.WARN, Severity.ERROR)),
                is(NavDataSetStatus.ERROR));
    }
}