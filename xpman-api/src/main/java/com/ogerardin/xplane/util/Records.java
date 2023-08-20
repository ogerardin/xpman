package com.ogerardin.xplane.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

import static com.ogerardin.xplane.util.IntrospectionHelper.invokeSneaky;

@UtilityClass
public class Records {

    /**
     * This method will return a coalesced record, where the value of each component is rightmost non-null value
     * among all specified records.
     */
    @SafeVarargs
    @SneakyThrows
    public <R extends Record> R coalesce(R... records) {
        if (records.length == 0) {
            throw new IllegalArgumentException("Cannot coalesce empty array");
        }

        Class<? extends Record> recordClass = records[0].getClass();
        RecordComponent[] recordComponents = recordClass.getRecordComponents();
        Object[] values = new Object[recordComponents.length];

        for (R record : records) {
            // overwrite the value of each component with the current record's value unless it is null
            for (int i = 0; i < recordComponents.length; i++) {
                RecordComponent recordComponent = recordComponents[i];
                Method accessor = recordComponent.getAccessor();
                Object value = invokeSneaky(record, accessor);
                if (value != null) {
                    values[i] = value;
                }
            }
        }

        // create a new record instance with the coalesced values
        @SuppressWarnings("unchecked")
        Constructor<R> constructor = (Constructor<R>) recordClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        R clone = constructor.newInstance(values);
        return clone;
    }

}
