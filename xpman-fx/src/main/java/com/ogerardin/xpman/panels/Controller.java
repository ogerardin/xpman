package com.ogerardin.xpman.panels;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xpman.diag.DiagUtil;
import lombok.SneakyThrows;

/**
 * Abstract base class for section controllers that need to display inspection results.
 *
 * <p>Provides a single {@code displayInspectionResults} method that delegates to
 * {@link DiagUtil#displayInspectionMessages} to show a diagnostic dialog. Controllers
 * extend this class and reference this method from {@code @OnSuccess} annotations
 * (e.g. {@code @OnSuccess("displayInspectionResults(#result)")}) so that menu-driven
 * actions can surface inspection messages without each controller reimplementing the wiring.
 */
public abstract class Controller {

    @SuppressWarnings("unused")
    @SneakyThrows
    public void displayInspectionResults(InspectionResult result) {
        DiagUtil.displayInspectionMessages(result.getMessages());
    }
}
