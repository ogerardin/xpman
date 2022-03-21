package com.ogerardin.xpman.util.jfx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * A cell value factory that returns the value of the item itself.
 */
public class IdentityCellValueFactory<S> implements Callback<TableColumn.CellDataFeatures<S, S>, ObservableValue<S>> {

    @Override
    public ObservableValue<S> call(TableColumn.CellDataFeatures<S, S> param) {
        return new ReadOnlyObjectWrapper<>(param.getValue());
    }
}
