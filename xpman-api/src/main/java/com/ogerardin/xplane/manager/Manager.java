package com.ogerardin.xplane.manager;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.events.EventDispatcher;
import com.ogerardin.xplane.util.events.EventSource;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.util.List;

/**
 * Base class for managers that handle a list of X-Plane items, such as aircraft, scenery packages, ...
 * @param <T> the type of items handled by this manager
 */
@Data
public abstract class Manager<T> implements EventSource<ManagerEvent<T>> {

    protected final XPlane xPlane;

    @ToString.Exclude
    protected List<T> items;

    @Delegate
    @ToString.Exclude
    private final EventDispatcher<ManagerEvent<T>> eventSource = new EventDispatcher<>();

    public abstract void reload();
}
