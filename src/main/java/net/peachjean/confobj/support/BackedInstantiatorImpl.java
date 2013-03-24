package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;

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
    private final Class<T> confObjType;
    private final Class<? extends T> confObjImplType;
    private final GenericType[] bindingType;
    private final Annotation[] bindingAnnotation;

    public BackedInstantiatorImpl(Class<T> confObjType, Class<? extends T> confObjImplType) {
        this.confObjType = confObjType;
        this.confObjImplType = confObjImplType;
        this.constructor = (Constructor<? extends T>) confObjImplType.getConstructors()[0];

        Type[] parameterTypes = constructor.getGenericParameterTypes();
        if (parameterTypes.length < 1 || !ConfigObjectBacker.class.isAssignableFrom(constructor.getParameterTypes()[0])) {
            throw new IllegalStateException("The single configuration object  constructor must have as its first parameter ConfigObjectBacker.");
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
    public T instantiate(ConfigObjectBacker backer) {
        return this.instantiate(backer, EMPTY_CONTEXT);
    }

    @Override
    public T instantiate(ConfigObjectBacker backer, InstantiationContext context) {
        Object[] parameters = new Object[bindingType.length];
        parameters[0] = backer;
        for (int i = 1; i < parameters.length; i++) {
            parameters[i] = lookupParameter(i, context);
        }
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to invoke constructor for implementation of " + confObjType.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to invoke constructor for implementation of " + confObjType.getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke constructor for implementation of " + confObjType.getName(), e);
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