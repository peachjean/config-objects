package net.peachjean.itsco.support;

import org.apache.commons.lang3.AnnotationUtils;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

class BackedInstantiatorImpl<T> implements BackedInstantiator<T> {
    private static final InstantiationContext EMPTY_CONTEXT = new InstantiationContext() {
        @Override
        public <T> T lookup(GenericType<T> type) {
            return null;
        }

        @Override
        public <T> T lookup(GenericType<T> type, Annotation qualifier) {
            return null;
        }
    };
    private final Constructor<? extends T> constructor;
    private final Class<T> itscoClass;
    private final Class<? extends T> itscoImplClass;
    private final GenericType[] bindingType;
    private final Annotation[] bindingAnnotation;

    public BackedInstantiatorImpl(Class<T> itscoClass, Class<? extends T> itscoImplClass) {
        this.itscoClass = itscoClass;
        this.itscoImplClass = itscoImplClass;
        this.constructor = (Constructor<? extends T>) itscoImplClass.getConstructors()[0];

        Type[] parameterTypes = constructor.getGenericParameterTypes();
        if (parameterTypes.length < 1 || !ItscoBacker.class.isAssignableFrom(constructor.getParameterTypes()[0])) {
            throw new IllegalStateException("The single itsco constructor must have as its first parameter ItscoBacker.");
        }
        bindingType = new GenericType[parameterTypes.length];
        bindingAnnotation = new Annotation[parameterTypes.length];
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        for (int i = 1; i < parameterTypes.length; i++) {
            bindingType[i] = GenericType.forType(parameterTypes[i]);
            Annotation[] annotations = parameterAnnotations[i];
            for (int j = 0; j < annotations.length; j++) {
                if (annotations[j].annotationType().isAnnotationPresent(Qualifier.class)) {
                    bindingAnnotation[i] = annotations[j];
                }
            }
        }
    }

    @Override
    public T instantiate(ItscoBacker backer) {
        return this.instantiate(backer, EMPTY_CONTEXT);
    }

    @Override
    public T instantiate(ItscoBacker backer, InstantiationContext context) {
        Object[] parameters = new Object[bindingType.length];
        parameters[0] = backer;
        for (int i = 1; i < parameters.length; i++) {
            parameters[i] = lookupParameter(i, context);
        }
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to invoke constructor for implementation of " + itscoClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to invoke constructor for implementation of " + itscoClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke constructor for implementation of " + itscoClass.getName(), e);
        }

    }

    private Object lookupParameter(int i, InstantiationContext context) {
        if (bindingAnnotation[i] == null) {
            return context.lookup(bindingType[i]);
        } else {
            return context.lookup(bindingType[i], bindingAnnotation[i]);
        }
    }
}