package com.ogerardin.xpman.util.jfx.menu;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A {@link Runnable} that, when invoked, calls a given {@link Method} on a given target object with given
 * parameters. Optionnally a confirmation can be evaluated before calling the method, and an action can be
 * performed upon successful completion.
 * @param <T> type of the target object (on which the method will be called)
 * @param <R> type of the method result
 */
@SuppressWarnings("ClassCanBeRecord")
@Data
@Slf4j
@Builder
public class MethodAction<T, R> implements Runnable {

    private final Method method;
    private final T target;
    private final Object[] paramValues;

    private final Supplier<Boolean> confirm;
    private final Consumer<R> onSuccess;

    public void run() {
        if (confirm != null && !confirm.get()) {
            // confirm failed (action cancelled)
            return;
        }
        try {
            // invoke method with specified parameter values
            log.debug("Invoking {} on {} with parameters {}", method, getTarget(), paramValues);
            @SuppressWarnings("unchecked")
            R result = (R) method.invoke(getTarget(), paramValues);
            log.debug("Method invocation returned: {}", result);

            if (onSuccess != null) {
                onSuccess.accept(result);
            }

        } catch (Exception e) {
            log.error("Exception while invoking method", e);
        }
    }

}