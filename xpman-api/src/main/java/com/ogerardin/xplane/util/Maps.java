package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Maps {

    public <K, V> Map<K, V> mapOf(Object... keyValues) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i+=2) {
            //noinspection unchecked
            map.put((K) keyValues[i], (V) keyValues[i+1]);
        }
        return map;
    }

    @SafeVarargs
    public <K, V> Map<K, V> merge(Map<K, V>... maps) {
        final HashMap<K, V> result = new HashMap<>();
        for (Map<K, V> map : maps) {
            result.putAll(map);
        }
        return result;
    }


}
