package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xplane.util.LegacyObservableList;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ObservableListAdapterTest {

    @Test
    void update() {

        final List<String> baseList = new ArrayList<>();

        final LegacyObservableList<String> legacyObservableList = new LegacyObservableList<>(baseList);
        baseList.add("A1");

        final ObservableListAdapter<String> jfxObservable = new ObservableListAdapter<>(legacyObservableList);
        log.debug("list before: {}", jfxObservable);

        AtomicReference<Observable> invalidated = new AtomicReference<>();
        jfxObservable.addListener((InvalidationListener) o -> {
            log.debug("invalidated: {}", o);
            invalidated.set(o);
        });

        AtomicReference<ListChangeListener.Change<? extends String>> changed = new AtomicReference<>();
        jfxObservable.addListener((ListChangeListener<String>) c -> {
            log.debug("changed: {}", c);
            changed.set(c);
        });

        baseList.add("A2");
        legacyObservableList.set(baseList);

        log.debug("list after: {}", jfxObservable);

        assertEquals(legacyObservableList, jfxObservable);
        assertSame(jfxObservable, invalidated.get());

        var change = changed.get();
        assertNotNull(change);
        assertTrue(change.next());
        assertTrue(change.wasReplaced());
        assertFalse(change.next());

    }
}