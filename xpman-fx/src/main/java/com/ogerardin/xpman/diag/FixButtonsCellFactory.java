package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.Fix;
import com.ogerardin.xplane.inspection.InspectionMessage;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

/**
 * Cell factory for the "Fix" column in the inspection dialog.
 *
 * <p>Renders one button per {@link Fix} in an {@link HBox}. When clicked, the fix is executed
 * via {@link FixRunner}. On success, the row is removed from the table because the dialog
 * shows a snapshot and a fixed message would be stale.</p>
 */
public class FixButtonsCellFactory implements Callback<TableColumn<InspectionMessage, List<Fix>>, TableCell<InspectionMessage, List<Fix>>> {

    private static final int ICON_SIZE = 14;

    @Override
    public TableCell<InspectionMessage, List<Fix>> call(TableColumn<InspectionMessage, List<Fix>> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(List<Fix> fixes, boolean empty) {
                super.updateItem(fixes, empty);

                if (empty || fixes == null || fixes.isEmpty()) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    HBox box = new HBox(4);
                    fixes.forEach(fix -> {
                        Button button = new Button(fix.label());
                        button.setGraphic(new FontIcon(Feather.TOOL));
                        ((FontIcon) button.getGraphic()).setIconSize(ICON_SIZE);
                        button.setOnAction(_ -> {
                            boolean success = FixRunner.run(fix, this);
                            if (success) {
                                TableView<InspectionMessage> table = getTableView();
                                InspectionMessage row = getTableRow().getItem();
                                if (table != null && row != null) {
                                    table.getItems().remove(row);
                                }
                            }
                        });
                        box.getChildren().add(button);
                    });
                    setGraphic(box);
                    setStyle("-fx-alignment: center-left;");
                }
            }
        };
    }
}
