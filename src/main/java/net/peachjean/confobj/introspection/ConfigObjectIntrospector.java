package net.peachjean.confobj.introspection;

import net.peachjean.confobj.ConfigObject;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class ConfigObjectIntrospector {
    /**
     * This method guarantees that the {@link ConfigObjectVisitor#visitDefaults} method will be called first.
     *
     * @param confObjType
     * @param input
     * @param visitor
     * @param <T>
     * @param <I>
     * @return
     */
    public static <T, I> I visitMembers(Class<T> confObjType, I input, ConfigObjectVisitor<T, I> visitor) {
        try {
            visitor.visitDefaults(determineDefaults(confObjType), input);
            final BeanInfo beanInfo = Introspector.getBeanInfo(confObjType);
            for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
                if (p.getReadMethod() == null) {
                    continue;
                }
                if (isConfigObject(p.getPropertyType())) {
                    visitor.visitConfigObject(p.getName(), p.getReadMethod(), GenericType.forType(p.getReadMethod().getGenericReturnType()), isRequired(confObjType, p.getReadMethod()), input);
                } else if (p.getPropertyType().isPrimitive()) {
                    visitor.visitPrimitive(p.getName(), p.getReadMethod(), p.getPropertyType(), isRequired(confObjType, p.getReadMethod()), input);
                } else {
                    visitor.visitSimple(p.getName(), p.getReadMethod(), GenericType.forType(p.getReadMethod().getGenericReturnType()), isRequired(confObjType, p.getReadMethod()), input);
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not introspect " + confObjType, e);
        }
        return input;
    }

    public static boolean isConfigObject(Class<?> type) {
        return type.isAnnotationPresent(ConfigObject.class);
    }

    private static boolean isRequired(final Class<?> type, final Method m) {
        Class<?> defaultsClass = determineDefaults(type);
        try {
            defaultsClass.getDeclaredMethod(m.getName());
            return false;
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    private static <T> Class<? extends T> determineDefaults(final Class<T> type) {
        for (Class<?> enclosedClass : type.getClasses()) {
            if ("Defaults".equals(enclosedClass.getSimpleName())) {
                return (Class<? extends T>) enclosedClass;
            }
        }
        throw new IllegalStateException("Could not locate Defaults class for " + type);
    }

}
