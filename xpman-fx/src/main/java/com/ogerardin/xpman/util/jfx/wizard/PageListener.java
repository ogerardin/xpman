package com.ogerardin.xpman.util.jfx.wizard;

import org.controlsfx.dialog.Wizard;

public interface PageListener {

    default void onEnteringPage(Wizard wizard) {}

    default void onExitingPage(Wizard wizard) {}

}
