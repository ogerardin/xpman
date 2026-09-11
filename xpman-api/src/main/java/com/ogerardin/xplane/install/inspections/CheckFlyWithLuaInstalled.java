package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaPlugin;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.RequiredArgsConstructor;

/**
 * Checks that FlyWithLuaPlugin is installed before allowing script installation.
 */
@RequiredArgsConstructor
public class CheckFlyWithLuaInstalled implements Inspection<Archive> {
    
    public static final CheckFlyWithLuaInstalled INSTANCE = new CheckFlyWithLuaInstalled(null);
    
    private final XPlane xPlane;
    
    @Override
    public InspectionResult inspect(Archive archive) {
        if (xPlane == null) {
            return InspectionResult.empty();
        }
        
        boolean flyWithLuaInstalled = xPlane.getPluginManager().getPlugins().stream()
            .anyMatch(plugin -> plugin instanceof FlyWithLuaPlugin);
        
        if (!flyWithLuaInstalled) {
            return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.ERROR)
                .object("FlyWithLuaPlugin")
                .message("FlyWithLuaPlugin plugin is required but not installed")
                .details("FlyWithLuaPlugin scripts require the FlyWithLuaPlugin plugin to be installed first. " +
                        "Please install FlyWithLuaPlugin before installing scripts.")
                .abort(true)
                .build());
        }
        
        return InspectionResult.empty();
    }
}
