package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

@Slf4j
public class ToolsManager extends Manager<Tool> {

    @NonNull
    @Getter
    private final Path toolsFolder;

    private List<Tool> tools = null;

    public ToolsManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.toolsFolder = xPlane.getPaths().tools();
    }

    public List<Tool> getTools() {
        if (tools == null) {
            loadTools();
        }
        return Collections.unmodifiableList(tools);
    }

    /**
     * Trigger an asynchronous reload of the aircraft list.
     */
    public void reload() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::loadTools);
    }

    @SneakyThrows
    @Synchronized
    private void loadTools() {

        log.info("Loading tools...");
        fireEvent(new ManagerEvent.Loading<>());

        // find all runnable files under the tools folder
        Predicate<Path> isRunnable = p -> Platforms.getCurrent().isRunnable(p);
        List<Path> toolFiles = Files.list(toolsFolder)
                .filter(isRunnable)
                .toList();
        log.debug("Found {} tools", toolFiles.size());

        // build Tool object for each applicable file
        tools = toolFiles.stream()
                .map(this::getTool)
                .toList();

        log.info("Loaded {} tools", tools.size());
        fireEvent(new ManagerEvent.Loaded<>(tools));
    }

    @SneakyThrows
    private Tool getTool(Path path) {
        return IntrospectionHelper.getBestSubclassInstance(Tool.class, path);
    }

}
