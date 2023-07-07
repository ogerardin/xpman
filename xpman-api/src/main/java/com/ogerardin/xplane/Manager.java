package com.ogerardin.xplane;

import com.ogerardin.xplane.events.EventDispatcher;
import com.ogerardin.xplane.events.EventSource;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.util.List;

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
