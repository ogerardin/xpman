package com.ogerardin.xplane.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class LegacyObservableListTest {

    @Test
    void update() {

        final List<String> baseList = new ArrayList<>();
        baseList.add("A1");

        final LegacyObservableList<String> observableList = new LegacyObservableList<>(baseList);
        log.debug("list before: {}", observableList);

        AtomicBoolean updateCalled = new AtomicBoolean(false);
        observableList.addObserver((o, arg) -> {
            log.debug("update called!");
            updateCalled.set(true);
        });

        baseList.add("A2");
        observableList.set(baseList);

        log.debug("list after: {}", observableList);

        assertEquals(observableList, baseList);
        assertThat("updated was called", updateCalled.get());

    }

}