package com.ogerardin.xplane.test.impl;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.inspection.impl.AircraftSpecInspection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AircraftSpecInspectionTest {

    @SneakyThrows
    @Test
    void inspectNoMessage() {
        AcfFile acfFile = mock();
        when(acfFile.getFileSpecVersion()).thenReturn("1200");
        Aircraft aircraft = mock();
        when(aircraft.getAcfFile()).thenReturn(acfFile);

        XPlane xPlane = mock();
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP12);

        AircraftSpecInspection inspection = new AircraftSpecInspection(xPlane);

        List<InspectionMessage> messages = inspection.inspect(aircraft);
        assertThat(messages, hasSize(0));
    }
    @SneakyThrows
    @Test
    void inspectOlder() {
        AcfFile acfFile = mock();
        when(acfFile.getFileSpecVersion()).thenReturn("1100");
        Aircraft aircraft = mock();
        when(aircraft.getAcfFile()).thenReturn(acfFile);

        XPlane xPlane = mock();
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP12);

        AircraftSpecInspection inspection = new AircraftSpecInspection(xPlane);

        List<InspectionMessage> messages = inspection.inspect(aircraft);
        assertThat(messages, hasSize(1));
        InspectionMessage message = messages.get(0);
        assertThat(message.getSeverity(), is(Severity.WARN));
    }

    @SneakyThrows
    @Test
    void inspectNewer() {
        AcfFile acfFile = mock();
        when(acfFile.getFileSpecVersion()).thenReturn("1200");
        Aircraft aircraft = mock();
        when(aircraft.getAcfFile()).thenReturn(acfFile);

        XPlane xPlane = mock();
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP11);

        AircraftSpecInspection inspection = new AircraftSpecInspection(xPlane);

        List<InspectionMessage> messages = inspection.inspect(aircraft);
        assertThat(messages, hasSize(1));
        InspectionMessage message = messages.get(0);
        assertThat(message.getSeverity(), is(Severity.ERROR));
    }
}