package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * Interface expected by {@link TableView#setRowFactory}
 */
public interface TableRowFactory<T> extends Callback<TableView<T>, TableRow<T>> {
}
