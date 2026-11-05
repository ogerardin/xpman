package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.config.XPManPrefs;
import javafx.scene.control.TableColumnBase;

import java.util.Collection;

/**
 * Persists table column widths to {@link XPManPrefs}, keyed by {@code tableId.columnId} where
 * {@code columnId} is the column's {@code fx:id} (stable under reordering).
 * Initialized once at startup like {@link WindowPositionHelper}; a safe no-op when uninitialized.
 * Only updates the in-memory prefs: the regular quit-time save writes them to disk.
 */
public final class TableColumnWidthHelper {

    private TableColumnWidthHelper() {}

    private static XPManPrefs config;

    /** Sets the prefs backing the helper; {@code null} disables persistence. */
    public static void initialize(XPManPrefs config) {
        TableColumnWidthHelper.config = config;
    }

    /**
     * Restores saved widths via {@code setPrefWidth} (which propagates to {@code width}) and records
     * subsequent resizes by observing {@code width}: a user drag goes through the column resize policy
     * ({@code TableUtil.resize -> doSetWidth}), which changes {@code width} only, never {@code prefWidth}.
     */
    public static void setup(String tableId, Collection<? extends TableColumnBase<?, ?>> columns) {
        if (config == null) {
            return;
        }
        for (TableColumnBase<?, ?> column : columns) {
            String columnId = column.getId();
            if (columnId == null || columnId.isBlank()) {
                continue;
            }
            String key = tableId + "." + columnId;
            Double saved = config.getTableColumnWidths().get(key);
            if (saved != null && saved > 0 && Double.isFinite(saved)) {
                column.setPrefWidth(Math.max(column.getMinWidth(), Math.min(saved, column.getMaxWidth())));
            }
            column.widthProperty().addListener((_, _, newValue) -> {
                double width = newValue.doubleValue();
                if (width > 0 && Double.isFinite(width)) {
                    config.getTableColumnWidths().put(key, width);
                }
            });
        }
    }
}
