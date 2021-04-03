package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Interface expected by {@link TableColumn#setCellFactory}
 */
public interface TableCellFactory<S, T> extends Callback<TableColumn<S, T>, TableCell<S, T>> {
}
