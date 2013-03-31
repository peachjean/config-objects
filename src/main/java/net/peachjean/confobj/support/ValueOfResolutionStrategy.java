package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class ValueOfResolutionStrategy implements FieldResolutionStrategy {

    public static final ValueOfResolutionStrategy INSTANCE = new ValueOfResolutionStrategy();

    @Override
    public <T, C> FieldResolution<T> resolve(final String name, final GenericType<T> lookupType, final Configuration config, final C resolutionContext) {
        final Method m = lookupMethod(lookupType.getRawType());
        if(m == null) {
            throw new IllegalArgumentException("Class " + lookupType.getRawType().getName() + " does not have a valueOf method.");
        }
        return new FieldResolution.Simple<T>(ConfigurationUtils.determineFullPath(config, name)) {
            @Override
            protected T doResolve() {
                if(config.containsKey(name)) {
                    return getAndReturn(name, lookupType, config, m);
                } else {
                    return null;
                }
            }
        };
    }

    private <T, C> T getAndReturn(final String name, final GenericType<T> lookupType, Configuration config, Method m) {
        String value = config.getString(name);
        try {
            return lookupType.cast(m.invoke(null, value));
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke valueOf method on " + lookupType, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not invoke valueOf method on " + lookupType, e);
        }
    }

    private <T> Method lookupMethod(final Class<T> lookupType) {
        try {
            final Method valueOf = lookupType.getMethod("valueOf", String.class);
            final int mod = valueOf.getModifiers();
            if (!Modifier.isStatic(mod) || Modifier.isAbstract(mod) || !Modifier.isPublic(mod)) {
                return null;
            }
            return valueOf;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public boolean supports(final GenericType<?> lookupType) {
        return lookupMethod(lookupType.getRawType()) != null;
    }
}
