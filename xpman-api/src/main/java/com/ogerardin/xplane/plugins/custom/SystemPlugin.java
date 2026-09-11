package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Set;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * subclass of {@link Plugin} that represents a system plugin (non deletable)
 */
@Slf4j
@SuppressWarnings("unused")
public class SystemPlugin extends Plugin {

    private static final Set<String> SYSTEM_PLUGIN_NAMES = Set.of("PluginAdmin");

    public SystemPlugin(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile);
        require(isSystemPlugin(xplFile));
    }

    @Override
    public boolean getSystem() {
        return true;
    }

    private static boolean isSystemPlugin(Path xplFile) {
        String baseFolderName = getBaseFolder(xplFile).getFileName().toString();
        return SYSTEM_PLUGIN_NAMES.contains(baseFolderName);
    }
}
