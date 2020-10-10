package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.util.SpelUtil;
import javafx.beans.NamedArg;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;

import sun.util.logging.PlatformLogger;
import sun.util.logging.PlatformLogger.Level;
import com.sun.javafx.property.PropertyReference;
import com.sun.javafx.scene.control.Logging;


/**
 * An equivalent of {@link javafx.scene.control.cell.PropertyValueFactory} using an expression written in SpEL
 * (Spring Expression Language) instead of a single property name.
 * The expression is evaluated with the row's item as context root.
 *
 * @param <S> The type of the class contained within the TableView.items list.
 * @param <T> The type of the class contained within the TableColumn cells.
 */
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
            T value = (T) SpelUtil.eval(getExpression(), rowData);
            return new ReadOnlyObjectWrapper<T>(value);
        } catch (IllegalStateException e) {
            // log the warning and move on
            final PlatformLogger logger = Logging.getControlsLogger();
            if (logger.isLoggable(Level.WARNING)) {
                logger.finest("Can not retrieve property '" + getExpression() +
                        "' in PropertyValueFactory: " + this +
                        " with provided class type: " + rowData.getClass(), e);
            }
        }

        return null;
    }
}
