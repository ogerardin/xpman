package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.plugins.custom.FlyWithLua;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.RequiredArgsConstructor;

/**
 * Checks that FlyWithLua is installed before allowing script installation.
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
            .anyMatch(plugin -> plugin instanceof FlyWithLua);
        
        if (!flyWithLuaInstalled) {
            return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.ERROR)
                .object("FlyWithLua")
                .message("FlyWithLua plugin is required but not installed")
                .details("FlyWithLua scripts require the FlyWithLua plugin to be installed first. " +
                        "Please install FlyWithLua before installing scripts.")
                .abort(true)
                .build());
        }
        
        return InspectionResult.empty();
    }
}
