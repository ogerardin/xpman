package com.ogerardin.xpman.panels;

import com.ogerardin.xpman.platform.Platforms;
import com.ogerardin.xpman.util.Config;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import lombok.SneakyThrows;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Optional;

public class AboutController {

    @FXML
    private Text build;
    @FXML
    private Text runtime;
    @FXML
    private Text vm;
    @FXML
    private Text title;



    @FXML
    void initialize() {
        String version = Config.getVersion();
        title.setText(MessageFormat.format("X-Plane Manager {0}", Optional.ofNullable(version).orElse("DEV")));
        build.setText(MessageFormat.format("Build {0} (commit #{1} on {2})",
                Optional.ofNullable(Config.getBuildNumber()).orElse("N/A"),
                Optional.ofNullable(Config.getCommit()).orElse("N/A"),
                Optional.ofNullable(Config.getBranch()).orElse("N/A")));
        runtime.setText(MessageFormat.format("Runtime: {0} {1} {2}", Config.getRuntimeName(), Config.getJavaVersion(), Config.getOsArch()));
        vm.setText(MessageFormat.format("VM: {0} by {1}", Config.getVmName(), Config.getJavaVmVendor()));
    }

    @SneakyThrows
    public void openLink() {
        Platforms.getCurrent().openUrl(new URL("https://github.com/ogerardin/xpman"));
    }

    @SneakyThrows
    public void showLicence() {
        Platforms.getCurrent().openUrl(new URL("https://www.gnu.org/licenses/gpl-3.0.txt"));
    }
}
