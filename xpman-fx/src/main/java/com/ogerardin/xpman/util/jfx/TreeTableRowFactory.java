package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

/**
 * Interface expected by {@link TableView#setRowFactory}
 */
public interface TreeTableRowFactory<T> extends Callback<TreeTableView<T>, TreeTableRow<T>> {
}
