package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.navdata.NavDataSet;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

/** UI wrapper around {@link NavDataItem} that adds a reveal context-menu action and derived display properties. */
@Data
@ToString(includeFieldNames = false)
@RequiredArgsConstructor
public class UiNavDataItem {

    @Delegate
    final NavDataItem navDataItem;

    /**
     * A short description of this layer's state for the card header, e.g. {@code Cycle 2610}.
     * Empty for items that are not layers.
     */
    public String getState() {
        return navDataItem instanceof NavDataSet navDataSet ? navDataSet.describeState() : "";
    }

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    @EnabledIf("exists")
    public void reveal() {
        Platforms.getCurrent().reveal(getPath());
    }

    /**
     * Runs the inspection for the wrapped item and returns the result. Used by the card
     * to display problems inline; no longer exposed as an action (inspections are systematic).
     */
    public InspectionResult getInspectionResult() {
        return navDataItem instanceof Inspectable inspectable
                ? inspectable.inspect()
                : InspectionResult.empty();
    }

}
