package com.ogerardin.xpman.util.jfx;

import com.google.api.client.googleapis.notifications.NotificationUtils;
import com.sun.javafx.collections.ObservableListWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MirroringListTest {

    @Test
    void mirrors() {

        final ObservableListWrapper<String> observableList = new ObservableListWrapper<>(new ArrayList<>());
        observableList.add(UUID.randomUUID().toString());

        final MirroringList<String, String> mirroringList = new MirroringList<>(observableList, MirroringListTest::transform);
        log.debug("source list: {}", observableList);
        log.debug("target list: {}", mirroringList);

        assertEquals(expected(observableList), mirroringList);

        observableList.add(UUID.randomUUID().toString());
        log.debug("source list: {}", observableList);
        log.debug("target list: {}", mirroringList);

        assertEquals(expected(observableList), mirroringList);
    }

    private static String transform(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    private List<String> expected(ObservableListWrapper<String> observableList) {
        final List<String> expected = observableList.stream()
                .map(MirroringListTest::transform)
                .collect(Collectors.toList());
        return expected;
    }

}