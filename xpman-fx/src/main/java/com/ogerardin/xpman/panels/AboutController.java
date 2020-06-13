package com.ogerardin.xpman.panels;

import com.ogerardin.xpman.XPmanFX;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.text.MessageFormat;
import java.util.Optional;

public class AboutController {
    
    public Text text;

    @FXML
    void initialize() {
        String version = version();
        text.setText(MessageFormat.format("Version: {0}", Optional.ofNullable(version).orElse("UNKNOWN")));
    }

    public String version() {
        return XPmanFX.class.getPackage().getImplementationVersion();
    }

}
