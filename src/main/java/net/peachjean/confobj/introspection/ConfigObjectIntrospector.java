package net.peachjean.confobj.introspection;

import net.peachjean.confobj.ConfigObject;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

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
            visitBeanProperties(confObjType, confObjType, input, visitor);
            for(Class<?> iface: confObjType.getInterfaces()) {
                visitBeanProperties(iface, confObjType, input, visitor);
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not introspect " + confObjType, e);
        }
        visitImplementations(confObjType, input, visitor);
        return input;
    }

    private static <T, I> void visitBeanProperties(Class<?> beanClass, Class<T> confObjType, I input, ConfigObjectVisitor<T, I> visitor) throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
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
    }

    private static <T, I> void visitImplementations(Class<T> confObjType, I input, ConfigObjectVisitor<T, I> visitor) {
        String resourcePath = "META-INF/config-objects/" + confObjType.getName();
        try {
            Enumeration<URL> resources = confObjType.getClassLoader().getResources(resourcePath);
            while(resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                List<String> lines = IOUtils.readLines(resource.openStream(), Charsets.UTF_8);
                for(String line: lines) {
                    String[] parts = line.split("=");
                    if(parts.length != 2) {
                        continue;
                    }
                    Class<? extends T> implType = (Class<? extends T>) Class.forName(parts[1]);
                    visitor.visitNamedImplementation(implType, parts[0], input);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load implementation resources.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Invalid class name.", e);
        }
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
