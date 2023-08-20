package com.ogerardin.xplane.util;

import com.ogerardin.xplane.XPlane;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
    @Synchronized
    public List<Class<?>> findAllSubclasses(Class<?> baseClass) {
        final List<Class<?>> cachedResult = classToSubclasses.get(baseClass);
        if (cachedResult != null) {
            return cachedResult;
        }

        String packegaName = XPlane.class.getPackage().getName();
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(packegaName)
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
     * For each known subclass X of C, an attempt is made to construct an instance of X by invoking a constructor
     * that accepts the specified params; if it succeeds then this instance is returned.
     * If all subclasses of C have been examined and no constructor succeeded, an attempt is made to construct an instance
     * of C similarly by invoking a constructor that accepts the specified arguments.
     * <br>
     * This is useful for example to obtain an instance of a specialized subclass of Aircraft.
     * The idea is that constructors of an Aircraft subclass will fail if the class does not match the actual aircraft
     * file, thus prompting this method to return a default Aircraft instance. On the contrary
     * if a subclass (such as {@link com.ogerardin.xplane.aircrafts.custom.ZiboMod738}) recognizes an aircraft file, its constructor
     * will succeed prompting this method to return the specialized instance.
     * The method {@link #require} may be used in the constructor to throw a {@link InstantiationException} if a
     * condition is not met.
     */
    public <C> C getBestSubclassInstance(Class<C> baseClass, Object... constructorParams) throws InstantiationException {

        // find all subclasses of base class
        final List<Class<?>> candidateClasses = findAllSubclasses(baseClass);

        for (Class<?> candidateClass : candidateClasses) {
            try {
                C instance = newInstance(candidateClass, constructorParams);
                // constructor of a subclass succeeded, return the result
                log.debug("Matched {}({})", candidateClass.getSimpleName(), constructorParams);
                return instance;
            } catch (Exception ignored) {
                // failed to instantiate candidate class: just ignore
            }
        }
        // no candidate matched, instantiate base class
//        log.debug("Instantiating base class {} for {}", baseClass.getSimpleName(), constructorParams);
        return newInstance(baseClass, constructorParams);
    }

    @SuppressWarnings("unchecked")
    private <C> C newInstance(Class<?> baseClass, Object... constructorParams) throws InstantiationException {
        // try to find a constructor that matches the param types
        for (Constructor<?> constructor : baseClass.getDeclaredConstructors()) {
            try {
                return (C) constructor.newInstance(constructorParams);
            } catch (Exception ignored) {
                // constructor failed: just ignore
            }
        }
        throw new InstantiationException("No constructor matched the specified parameters");
    }

    /**
     * Fail the instantiation by throwing a {@link InstantiationException} if the specified boolean is not true
     */
    public void require(boolean valid) throws InstantiationException {
        if (! valid) {
            throw new InstantiationException();
        }
    }

    /**
     * Invokes the specified method on the specified object without declaring any checked exception.
     */
    @SneakyThrows
    public static Object invokeSneaky(Object target, Method method)  {
        return method.invoke(target);
    }
}
