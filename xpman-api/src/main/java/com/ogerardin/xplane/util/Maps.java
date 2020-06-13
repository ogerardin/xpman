package com.ogerardin.xplane.util;

import java.util.HashMap;
import java.util.Map;

public enum Maps {
    ;

    public static <K, V> Map<K, V> mapOf(Object... keyValues) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i+=2) {
            //noinspection unchecked
            map.put((K) keyValues[i], (V) keyValues[i+1]);
        }
        return map;
    }
}
