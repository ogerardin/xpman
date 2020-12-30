package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.navdata.NavDataItem;
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
