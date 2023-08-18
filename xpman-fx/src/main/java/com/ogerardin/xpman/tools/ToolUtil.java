package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xpman.util.jfx.console.ConsoleController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class ToolUtil {

    @SneakyThrows
    public void installTool(XPlane xPlane, Tool tool) {
        ConsoleController consoleController = displayConsole("Installing " + tool.getName());
        AsyncHelper.runAsync(() -> xPlane.getToolsManager().install(tool, consoleController));
    }

    @SneakyThrows
    public void uninstallTool(XPlane xPlane, Tool tool) {
        ConsoleController consoleController = displayConsole("Uninstalling " + tool.getName());
        AsyncHelper.runAsync(() -> xPlane.getToolsManager().uninstall(tool, consoleController));
    }

    public void runTool(XPlane xPlane, Tool tool) {
        xPlane.getToolsManager().launch(tool);
    }

    private ConsoleController displayConsole(@NonNull String title) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader loader = new FXMLLoader(ConsoleController.class.getResource("/fxml/console.fxml"));
        dialog.setDialogPane(loader.load());
        dialog.setTitle(title);
        dialog.show();
        return loader.getController();
    }

}
