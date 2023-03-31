package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@Data
@RequiredArgsConstructor
public class UiNavDataItem {

    @Delegate
    final NavDataItem navDataItem;

    public UiNavDataItem() {
        this(null);
    }

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    @EnabledIf("exists")
    public void reveal() {
        Platforms.getCurrent().reveal(getPath());
    }

}
