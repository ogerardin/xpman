package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.navdata.NavDataItem;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class UiNavDataItem {

    @Delegate
    final NavDataItem navDataItem;

    public UiNavDataItem() {
        this(null);
    }
}
