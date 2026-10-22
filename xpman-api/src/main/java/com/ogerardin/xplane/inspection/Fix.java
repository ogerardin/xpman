package com.ogerardin.xplane.inspection;

/**
 * An action which, when applied, is intended to remedy an {@link com.ogerardin.xplane.inspection.InspectionMessage}.
 *
 * <p>Fixes are offered in the inspection dialog and navdata cards, never in the install wizard,
 * where messages are only informative or blocking.</p>
 */
public interface Fix {

    /** Human-readable label, shown as a button caption in the UI. */
    String label();

    void apply();
}
