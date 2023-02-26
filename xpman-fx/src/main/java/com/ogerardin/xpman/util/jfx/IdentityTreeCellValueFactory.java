package com.ogerardin.xpman.util.jfx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * A cell value factory for a {@link TreeTableColumn} that returns the value of the underlying item itself.
 * Useful when the cell factory needs access to the whole item and not just a property.
 */
public class IdentityTreeCellValueFactory<S> implements Callback<TreeTableColumn.CellDataFeatures<S, S>, ObservableValue<S>> {

    @Override
    public ObservableValue<S> call(TreeTableColumn.CellDataFeatures<S, S> param) {
        return new ReadOnlyObjectWrapper<>(param.getValue().getValue());
    }
}
