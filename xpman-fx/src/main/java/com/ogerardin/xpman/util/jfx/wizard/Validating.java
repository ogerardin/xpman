package com.ogerardin.xpman.util.jfx.wizard;

import javafx.beans.property.ReadOnlyBooleanProperty;

/** Interface for wizard pages that can report validation state via an {@code invalidProperty}. */
public interface Validating {

    ReadOnlyBooleanProperty invalidProperty();

}
