package com.ogerardin.xplane.inspection;

/**
 * An action which, when applied, is intended to remedy an {@link com.ogerardin.xplane.inspection.InspectionMessage}
 * */
public interface Fix {

    void apply();
}
