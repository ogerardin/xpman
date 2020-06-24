package com.ogerardin.xpman.panels;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import javafx.scene.control.TableView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Data
public class DefaultPanelController<T> {

    /** The table to update. This property must be set in the initialize method, after FXML bindings have been populated */
    private TableView<T> tableView;

    @Getter(AccessLevel.PROTECTED)
    private XPlaneInstance xPlaneInstance;

    public DefaultPanelController(
            XPlaneInstanceProperty xPlaneInstanceProperty,
            Function<XPlaneInstance, List<T>> loader) {

        xPlaneInstanceProperty.addListener((observable, oldValue, newValue) -> {
            xPlaneInstance = newValue;

            LoadTask<T> loadTask = new LoadTask<>(tableView, () -> loader.apply(newValue));

            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

}
