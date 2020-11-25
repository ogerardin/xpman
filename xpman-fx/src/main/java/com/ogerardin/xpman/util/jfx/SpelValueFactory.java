package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.util.SpelUtil;
import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;


/**
 * An equivalent of {@link javafx.scene.control.cell.PropertyValueFactory} using an expression written in SpEL
 * (Spring Expression Language) instead of a single property name.
 * The expression is evaluated with the row's item as context root.
 *
 * @param <S> The type of the class contained within the TableView.items list.
 * @param <T> The type of the class contained within the TableColumn cells.
 */
@Slf4j
public class SpelValueFactory<S,T> implements Callback<CellDataFeatures<S,T>, ObservableValue<T>> {

    private final String expression;

    /**
     * Creates a default PropertyValueFactory to extract the value from a given
     * TableView row item reflectively, using the given property name.
     *
     * @param expression The name of the property with which to attempt to
     *      reflectively extract a corresponding value for in a given object.
     */
    public SpelValueFactory(@NamedArg("expression") String expression) {
        this.expression = expression;
    }

    /** {@inheritDoc} */
    @Override public ObservableValue<T> call(CellDataFeatures<S,T> param) {
        return getCellDataReflectively(param.getValue());
    }

    /**
     * Returns the expression provided in the constructor.
     */
    public final String getExpression() { return expression; }

    private ObservableValue<T> getCellDataReflectively(S rowData) {
        if (getExpression() == null || getExpression().isEmpty() || rowData == null) {
            return null;
        }



        try {
            @SuppressWarnings("unchecked")
            T value = (T) SpelUtil.eval(getExpression(), rowData);
            return new ReadOnlyObjectWrapper<T>(value);
        } catch (IllegalStateException e) {
            // log the warning and move on
                log.debug("Can not evaluate expression '" + getExpression() +
                        "' in SpelValueFactory: " + this +
                        " with provided class type: " + rowData.getClass(), e);
        }

        return null;
    }
}
