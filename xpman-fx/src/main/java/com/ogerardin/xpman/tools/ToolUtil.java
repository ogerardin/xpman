package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.InstallableTool;
import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.tools.ToolsManager;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xpman.util.jfx.console.ConsoleController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

@UtilityClass
public class ToolUtil {

    @SneakyThrows
    public void installTool(XPlane xPlane, InstallableTool tool) {
        installTool(xPlane, tool, false);
    }

    @SneakyThrows
    public void installTool(XPlane xPlane, InstallableTool tool, boolean runAfterInstall) {
        ConsoleController consoleController = displayConsole("Installing " + tool.getName());
        AsyncHelper.runAsync(() -> {
            ToolsManager toolsManager = xPlane.getToolsManager();
            InstalledTool installedTool = toolsManager.install(tool, consoleController);
            if (runAfterInstall) {
                toolsManager.launch(installedTool);
            }
        });
    }

    @SneakyThrows
    public void uninstallTool(XPlane xPlane, InstalledTool tool) {
        ConsoleController consoleController = displayConsole("Uninstalling " + tool.getName());
        AsyncHelper.runAsync(() -> xPlane.getToolsManager().uninstall(tool, consoleController));
    }

    /**
     * Run the given tool, installing it first if necessary.
     */
    public void runTool(XPlane xPlane, Tool tool) {
        if (tool instanceof InstalledTool installedTool) {
            xPlane.getToolsManager().launch(installedTool);
            return;
        }
        // tool is not installed, ask if we should install it
        Alert alert = new Alert(CONFIRMATION, tool.getName() + " is not installed. Do you want to install and run it now?");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> installTool(xPlane, (InstallableTool) tool, true));
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
