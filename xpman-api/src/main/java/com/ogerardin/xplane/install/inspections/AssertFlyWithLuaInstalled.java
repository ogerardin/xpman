package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Checks that FlyWithLua is installed before allowing script installation.
 */
@RequiredArgsConstructor
public class AssertFlyWithLuaInstalled implements Inspection<Archive> {
    
    private final XPlane xPlane;
    
    @Override
    public InspectionResult inspect(@NonNull Archive archive) {
        if (xPlane == null) {
            return InspectionResult.empty();
        }
        
        boolean flyWithLuaInstalled = xPlane.getPluginManager().getPlugins().stream()
            .anyMatch(FlyWithLua.class::isInstance);
        
        if (!flyWithLuaInstalled) {
            return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.ERROR)
                .object(FlyWithLua.FLY_WITH_LUA_PLUGIN)
                .message("FlyWithLua plugin is required but not installed")
                .details("FlyWithLua scripts require the FlyWithLua plugin to be installed first. " +
                        "Please install FlyWithLua before installing scripts.")
                .abort(true)
                .build());
        }
        
        return InspectionResult.empty();
    }
}
