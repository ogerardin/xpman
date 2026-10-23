package com.ogerardin.xpman.test;

import com.ogerardin.xpman.panels.plugins.PluginsController.ScriptGroupHeader;
import com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptGroupHeaderContextMenuTest {

    @Test
    void groupHeaderHasNoContextMenuActions() {
        assertTrue(IntrospectionHelper.computeRelevantMethods(ScriptGroupHeader.class).isEmpty());
    }
}
