package com.ogerardin.xpman.util.jfx.wizard;

import org.controlsfx.dialog.Wizard;

public interface FlowListener {

    default void onEnteringPage(Wizard wizard) {}

    default void onExitingPage(Wizard wizard) {}

}
