package com.ogerardin.xplane.util;

import com.ogerardin.xplane.config.XPlaneInstance;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
@Slf4j
public class IntrospectionHelper {

    private final Map<Class<?>, List<Class<?>>> classToSubclasses = new HashMap<>();

    /**
     * Returns all subclasses of the specified base class
     */
    public List<Class<?>> findAllSubclasses(Class<?> baseClass) {
        final List<Class<?>> cachedResult = classToSubclasses.get(baseClass);
        if (cachedResult != null) {
            return cachedResult;
        }

        String packegaName = XPlaneInstance.class.getPackage().getName();
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .whitelistPackages(packegaName)
                .scan()
        ) {
            ClassInfoList classInfoList = scanResult.getSubclasses(baseClass.getName());
            List<Class<?>> subclasses = classInfoList.loadClasses();
            log.debug("Subclasses of {} found: {}", baseClass, subclasses);
            classToSubclasses.put(baseClass, subclasses);
            return subclasses;
        }
    }

    /**
     * Returns an instance of {@link C} matching the specified constructor parameters.
     * Each known subclass of C is examined in sequence:
     * - if the class exposes a constructor compatible with the specified params, it is invoked with the specified params.
     * - if the constructor succeeds, the resulting instance is returned.
     * If all subclasses of C have been examined and none succeeded, an instance of baseClass is returned.
     */
    @SuppressWarnings("unchecked")
    public <C> C getBestSubclassInstance(Class<C> baseClass, Object... constructorParams) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        // determine param types
        Class<?>[] paramTypes = new Class[constructorParams.length];
        for (int i = 0; i < constructorParams.length; i++) {
            Object constructorParam = constructorParams[i];
            paramTypes[i] = constructorParam.getClass();
        }


        // find all subclasses of base class
        final List<Class<?>> candidateClasses = findAllSubclasses(baseClass);

        for (Class<?> candidateClass : candidateClasses) {
            try {
                // try to find a constructor that matches the  param types
                Constructor<?> constructor = candidateClass.getConstructor(paramTypes);
                // try to call it
                C instance = (C) constructor.newInstance(constructorParams);
                // it worked!
                log.debug("Recognized {} for {}", candidateClass.getSimpleName(), constructorParams);
                return instance;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
                // failed to instantiate candidate class: just ignore
            }
        }
        // no candidate matched, instantiate base class
//        log.debug("Instantiating base class {} for {}", baseClass.getSimpleName(), constructorParams);
        return baseClass.getConstructor(paramTypes).newInstance(constructorParams);
    }

}
