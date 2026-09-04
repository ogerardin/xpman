package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@Data
@ToString(includeFieldNames = false)
@RequiredArgsConstructor
public class UiNavDataItem {

    @Delegate
    final NavDataItem navDataItem;

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    @EnabledIf("exists")
    public void reveal() {
        Platforms.getCurrent().reveal(getPath());
    }

    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return navDataItem instanceof Inspectable inspectable
                ? inspectable.inspect()
                : InspectionResult.empty();
    }

}
