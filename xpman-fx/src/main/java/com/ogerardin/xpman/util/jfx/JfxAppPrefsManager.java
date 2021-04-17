package com.ogerardin.xpman.util.jfx;

import com.google.gson.Gson;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.prefs.Preferences;

/**
 * Utility class to save and load any object using the Java {@link Preferences} mechanism. This is especially
 * intended to load/save application preferences or configuration in one operation as long as it is contained in a single object.
 * Each field of the preferences object is mapped to a distinct preferences node with the same name as the field.
 * Field values are stored as a JSON-marshalled representation, except if the value in null in which case the
 * corresponding preferences key is removed.
 * Warning: normal limits for maximum length of a preference key or value apply.
 * @param <P> the type of the preferences object
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class JfxAppPrefsManager<P> {

    private final Gson GSON = new Gson();

    private final Class<P> prefsClass;

    private final Preferences prefsNode;

    /**
     * Create a {@link JfxAppPrefsManager} for the specified class. Values will be save to / loaded from a
     * user node obtained by calling {@link Preferences#userNodeForPackage} passing the prefs class.
     */
    public JfxAppPrefsManager(Class<P> prefsClass) {
        this(prefsClass, Preferences.userNodeForPackage(prefsClass));
    }

    /**
     * Create a {@link JfxAppPrefsManager} for the specified class. Values will be save to / loaded from a
     * user node obtained by calling {@link Preferences#userNodeForPackage} passing the specified app class.
     */
    public JfxAppPrefsManager(Class<P> prefsClass, Class<?> appClass) {
        this(prefsClass, Preferences.userNodeForPackage(appClass));
    }

    /**
     * Create a {@link JfxAppPrefsManager} for the specified class. Values will be save to / loaded from a
     * user node named as specified.
     */
    public JfxAppPrefsManager(Class<P> prefsClass, String nodeName) {
        this(prefsClass, Preferences.userRoot().node(nodeName));
    }

    @Getter(lazy = true)
    private final BeanInfo beanInfo = introspect(prefsClass);

    @SneakyThrows
    private BeanInfo introspect(Class<P> beanClass) {
        return Introspector.getBeanInfo(beanClass);
    }

    @SneakyThrows
    public P load() {
        log.debug("Loading prefs for {}", prefsClass);
        Preferences prefsNode = getPrefsNode();
        P prefs = prefsClass.newInstance();
        final BeanInfo beanInfo = introspect(prefsClass);
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyDescriptor.getReadMethod().getDeclaringClass() == Object.class) {
                continue;
            }
            final String fieldName = propertyDescriptor.getName();
            final Class<?> fieldType = propertyDescriptor.getPropertyType();
            log.debug("  Loading field '{}' of type {}", fieldName, fieldType);
            final String stringValue = prefsNode.get(fieldName, null);
            log.debug("    Stored value is: {}", stringValue);
            if (stringValue == null) {
//                propertyDescriptor.getWriteMethod().invoke(prefs, (Object) null);
                continue;
            }
            final Object value = GSON.fromJson(stringValue, fieldType);
            log.debug("    Unmarshalled as: {}", value);
            propertyDescriptor.getWriteMethod().invoke(prefs, value);
        }
        return prefs;
    }

    @SneakyThrows
    public void save(P prefs) {
        log.debug("Storing prefs for {} with value: {}", prefsClass, prefs);
        Preferences prefsNode = getPrefsNode();
        final BeanInfo beanInfo = introspect(prefsClass);
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyDescriptor.getReadMethod().getDeclaringClass() == Object.class) {
                continue;
            }
            final Class<?> fieldType = propertyDescriptor.getPropertyType();
            final String fieldName = propertyDescriptor.getName();
            final Object value = propertyDescriptor.getReadMethod().invoke(prefs);
            log.debug("  Saving field '{}' of type {} with value: {}", fieldName, fieldType, value);
            if (value == null) {
                log.debug("    Null value -> removing key '{}'", fieldName);
                prefsNode.remove(fieldName);
                continue;
            }
            final String json = GSON.toJson(value);
            log.debug("    Storing as: {}", json);
            prefsNode.put(fieldName, json);
        }
        prefsNode.flush();
    }

    @SneakyThrows
    public void clean() {
        final Preferences prefsRoot = getPrefsNode();
        prefsRoot.removeNode();
        prefsRoot.flush();
    }
}
