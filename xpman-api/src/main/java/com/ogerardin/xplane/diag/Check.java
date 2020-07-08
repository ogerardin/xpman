package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;

import java.util.function.BiFunction;

public interface Check<T> extends BiFunction<T, XPlaneInstance, Boolean> {

}
