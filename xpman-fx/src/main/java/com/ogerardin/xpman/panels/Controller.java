package com.ogerardin.xpman.panels;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xpman.diag.DiagUtil;
import lombok.SneakyThrows;

public abstract class Controller {

    @SuppressWarnings("unused")
    @SneakyThrows
    public void displayInspectionResults(InspectionResult result) {
        DiagUtil.displayInspectionMessages(result.getMessages());
    }
}
