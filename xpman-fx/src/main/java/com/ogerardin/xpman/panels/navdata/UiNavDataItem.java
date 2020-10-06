package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.config.navdata.NavDataItem;
import com.ogerardin.xplane.config.navdata.NavDataSet;
import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class UiNavDataItem {

    @Delegate
    private final NavDataItem navDataItem;

    public UiNavDataItem() {
        this(null);
    }

    public UiNavDataItem(NavDataItem navDataItem) {
        this.navDataItem = navDataItem;
    }
}
