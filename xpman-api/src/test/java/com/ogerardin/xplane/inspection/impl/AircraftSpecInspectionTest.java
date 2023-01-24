package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.inspection.InspectionMessage;
import lombok.SneakyThrows;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AircraftSpecInspectionTest {

    @SneakyThrows
    @Test
    void inspect() {
        Aircraft aircraft = mock();
        when(aircraft.getAcfFile().getFileSpecVersion()).thenReturn("1200");

        XPlane xPlane = mock();
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP12);

        AircraftSpecInspection inspection = new AircraftSpecInspection(xPlane);

        List<InspectionMessage> messages = inspection.inspect(aircraft);
        assertThat(messages, IsCollectionWithSize.hasSize(0));
    }
}