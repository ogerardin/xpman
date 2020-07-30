package com.ogerardin.xplane.config.scenery;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.install.*;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.util.FileUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.nio.file.Path;
import java.util.List;

public class SceneryInstaller extends DefaultInstaller {

    public SceneryInstaller(XPlaneInstance xPlaneInstance) {
        super(xPlaneInstance.getSceneryManager().getSceneryFolder(), ".obj");
    }
}
