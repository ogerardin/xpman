package com.ogerardin.xplane.navdata;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A navigation data node as part of a hierarchy.
 */
public interface NavDataItem {

    /** Length of an AIRAC cycle designator, e.g. 2610 for October 2026. */
    int CYCLE_LENGTH = 4;

    /**
     * Reduces a cycle designator to its AIRAC form so that differently formatted values
     * compare equal. A cycle may be written as {@code YYYYMM}, of which the AIRAC
     * designator is the trailing four digits.
     */
    static String normalizeCycle(String cycle) {
        return cycle != null && cycle.length() > CYCLE_LENGTH
                ? cycle.substring(cycle.length() - CYCLE_LENGTH)
                : cycle;
    }

    /**
     * True when this layer, once installed, makes X-Plane ignore every layer below it.
     * Only layers (as opposed to files) can override.
     */
    default boolean isOverriding() {
        return false;
    }

    /**
     * The highest-priority layer that is actually present and overrides the ones below it,
     * as a 1-based index into {@code items}, or -1 when no layer overrides anything.
     *
     * <p>X-Plane reads no other navdata text file while an overriding layer is present,
     * so layers below it are ignored and their problems are irrelevant.</p>
     */
    static int overridingIndex(List<? extends NavDataItem> items) {
        return IntStream.range(0, items.size())
                .filter(i -> overrides(items.get(i)))
                .map(i -> i + 1)
                .findFirst()
                .orElse(-1);
    }

    /**
     * The layers X-Plane actually reads: the present ones, or only the present overriding
     * layer when there is one, since X-Plane reads no other navdata file then.
     */
    static <T extends NavDataItem> List<T> active(List<T> items) {
        boolean overridden = items.stream().anyMatch(NavDataItem::overrides);
        return items.stream()
                .filter(NavDataItem::getExists)
                .filter(item -> !overridden || item.isOverriding())
                .toList();
    }

    private static boolean overrides(NavDataItem item) {
        return item.isOverriding() && item.getExists();
    }

    String getName();

    default Path getPath() {
        return null;
    }

    default String getDescription() {
        return null;
    }

    default String getAiracCycle() {
        return null;
    }
    default String getMetadata() {
        return null;
    }
    default String getBuild() {
        return null;
    }
    /**
     * The name the dataset gives itself in its header, for formats that carry one
     * (ARINC 424). Null for formats without a dataset name.
     */
    default String getDatasetName() {
        return null;
    }

    @SuppressWarnings("java:S1452") // Covariant child lists let implementations expose their concrete node type.
    default List<? extends NavDataItem> getChildren() {
        return Collections.emptyList();
    }

    boolean getExists();

}
