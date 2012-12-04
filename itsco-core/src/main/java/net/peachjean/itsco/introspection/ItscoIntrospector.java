package net.peachjean.itsco.introspection;

import com.google.common.collect.Lists;
import net.peachjean.itsco.Itsco;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

public class ItscoIntrospector {
    /**
     * This method guarantees that the {@link ItscoVisitor#visitDefaults} method will be called first.
     *
     * @param itscoType
     * @param input
     * @param visitor
     * @param <T>
     * @param <I>
     * @return
     */
    public static <T,I> I visitMembers(Class<T> itscoType, I input, ItscoVisitor<T,I> visitor) {
        try {
            visitor.visitDefaults(determineDefaults(itscoType), input);
            final BeanInfo beanInfo = Introspector.getBeanInfo(itscoType);
            for(PropertyDescriptor p: beanInfo.getPropertyDescriptors()) {
                if(p.getReadMethod() == null) {
                    continue;
                }
                if(isItsco(p.getPropertyType())) {
                    visitor.visitItsco(p.getName(), p.getReadMethod(), p.getPropertyType(), isRequired(itscoType, p.getReadMethod()), input);
                }
                else {
                    visitor.visitSimple(p.getName(), p.getReadMethod(), p.getPropertyType(), isRequired(itscoType, p.getReadMethod()), input);
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not introspect " + itscoType, e);
        }
        return input;
    }

    public static boolean isItsco(Class<?> type) {
        return type.isAnnotationPresent(Itsco.class);
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
        for(Class<?> enclosedClass: type.getClasses())
        {
            if("Defaults".equals(enclosedClass.getSimpleName()))
            {
                return (Class<? extends T>) enclosedClass;
            }
        }
        throw new IllegalStateException("Could not locate Defaults class for " + type);
    }

}