package com.ogerardin.xpman.util.jfx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * A cell value factory for a {@link TableColumn} that returns the value of the unerlying item itself.
 * Useful when the cell factory needs access to the whole item and not just a property.
 */
public class IdentityCellValueFactory<S> implements Callback<TableColumn.CellDataFeatures<S, S>, ObservableValue<S>> {

    @Override
    public ObservableValue<S> call(TableColumn.CellDataFeatures<S, S> param) {
        return new ReadOnlyObjectWrapper<>(param.getValue());
    }
}
