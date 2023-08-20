package com.ogerardin.xpman.util.jfx.cell_factory;

import javafx.beans.NamedArg;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

import static com.ogerardin.xplane.util.IntrospectionHelper.invokeSneaky;


@Slf4j
public class ToolTipCellFactory<S, T> implements TableCellFactory<S, T> {

    private final String property;

    public ToolTipCellFactory(@NamedArg("property") String property) {
        this.property = property;
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> param) {
        return new TableCell<>() {
            @SneakyThrows
            @Override
            protected void updateItem(T item, boolean empty) {
                // Default cell factory behaviour, see DEFAULT_CELL_FACTORY
                if (item == getItem()) return;
                super.updateItem(item, empty);
                if (item == null) {
                    super.setText(null);
                    super.setGraphic(null);
                } else if (item instanceof Node) {
                    super.setText(null);
                    super.setGraphic((Node)item);
                } else {
                    super.setText(item.toString());
                    super.setGraphic(null);
                }

                // Addition behaviour: set tooltip to value of specified property
                S rowItem = getTableRow().getItem();
                if (rowItem == null) {
                    return;
                }
                BeanInfo beanInfo = Introspector.getBeanInfo(rowItem.getClass(), Object.class);
                Arrays.stream(beanInfo.getPropertyDescriptors())
                        .filter(pd -> pd.getName().equals(property))
                        .findAny()
                        .map(PropertyDescriptor::getReadMethod)
                        .map(m -> invokeSneaky(rowItem, m))
                        .map(v -> {
                            Tooltip tooltip = new Tooltip(v.toString());
                            tooltip.setWrapText(true);
                            tooltip.setShowDuration(javafx.util.Duration.INDEFINITE);
                            return tooltip;
                        })
                        .ifPresent(this::setTooltip);
            }
        };
    }

}
