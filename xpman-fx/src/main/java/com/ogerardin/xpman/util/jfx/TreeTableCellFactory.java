package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * Interface expected by {@link TreeTableColumn#setCellFactory}
 */
public interface TreeTableCellFactory<S, T> extends Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> {
}
