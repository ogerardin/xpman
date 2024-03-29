package com.ogerardin.xplane.test.impl;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.inspection.impl.AircraftSpecInspection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AircraftSpecInspectionTest {

    @SneakyThrows
    @Test
    void inspectNoMessage() {
        XPlane xPlane = mock();
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP12);

        AcfFile acfFile = mock();
        when(acfFile.getFileSpecVersion()).thenReturn("1200");

        Aircraft aircraft = mock();
        when(aircraft.getAcfFile()).thenReturn(acfFile);
        when(aircraft.getXPlane()).thenReturn(xPlane);

        AircraftSpecInspection inspection = AircraftSpecInspection.INSTANCE;

        InspectionResult inspectionResult = inspection.inspect(aircraft);
        assertThat(inspectionResult.getMessages(), hasSize(0));
    }
    @SneakyThrows
    @Test
    void inspectOlder() {
        XPlane xPlane = mock();
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP12);

        AcfFile acfFile = mock();
        when(acfFile.getFileSpecVersion()).thenReturn("1100");

        Aircraft aircraft = mock();
        when(aircraft.getAcfFile()).thenReturn(acfFile);
        when(aircraft.getXPlane()).thenReturn(xPlane);

        AircraftSpecInspection inspection = AircraftSpecInspection.INSTANCE;

        InspectionResult inspectionResult = inspection.inspect(aircraft);
        assertThat(inspectionResult.getMessages(), hasSize(1));
        InspectionMessage message = inspectionResult.get(0);
        assertThat(message.getSeverity(), is(Severity.WARN));
    }

    @SneakyThrows
    @Test
    void inspectNewer() {
        XPlane xPlane = mock();
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP11);

        AcfFile acfFile = mock();
        when(acfFile.getFileSpecVersion()).thenReturn("1200");

        Aircraft aircraft = mock();
        when(aircraft.getAcfFile()).thenReturn(acfFile);
        when(aircraft.getXPlane()).thenReturn(xPlane);

        AircraftSpecInspection inspection = AircraftSpecInspection.INSTANCE;

        var result = inspection.inspect(aircraft);
        assertThat(result.getMessages(), hasSize(1));
        InspectionMessage message = result.get(0);
        assertThat(message.getSeverity(), is(Severity.ERROR));
    }
}