package com.ogerardin.xplane.manager;

import com.ogerardin.xplane.tools.Tool;
import lombok.*;

import java.util.List;
import java.util.function.Predicate;

/** Event fired by a {@link Manager} when loading starts or completes. */
@Getter
@Builder
@EqualsAndHashCode
@ToString()
public class ManagerEvent<T> {

    public enum Type implements Predicate<ManagerEvent<Tool>> {
        LOADING, LOADED;

        @Override
        public boolean test(ManagerEvent<Tool> event) {
            return event.getType() == this;
        }
    }

    @NonNull
    private final Type type;

    private final Manager<T> source;

    @ToString.Exclude
    private final List<T> items;
}
