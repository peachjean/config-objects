package net.peachjean.itsco.support;

import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;
import javassist.*;
import javassist.bytecode.AccessFlag;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
public abstract class ItscoFactorySupport<C> {

    private final Instantiator instantiator = new Instantiator();

    protected ItscoFactorySupport() {
    }

    public <T> T create(C context, Class<T> itscoClass) {
        return instantiator.lookupFunction(itscoClass).apply(createBacker(context));
    }

    public <T> Function<C, T> createGenerator(final Class<T> itscoClass)
    {
        return new Function<C, T>() {
            final Function<ItscoBacker, T> implFunction = instantiator.lookupFunction(itscoClass);

            @Override
            public T apply(final C context) {
                return implFunction.apply(createBacker(context));
            }
        };
    }

    private ItscoBacker createBacker(final C context) {
        return new ItscoBacker() {
            public <T> T lookup(final String name, final Class<T> lookupType) {
                if(contains(context, name))
                {
                    return getAndReturn(name, lookupType);
                }
                else
                {
                    throw new IllegalStateException("No value for " + name);
                }
            }

            public <T> T lookup(final String name, final Class<T> lookupType, final T defaultValue) {
                if(contains(context, name))
                {
                    return getAndReturn(name, lookupType);
                }
                else
                {
                    return defaultValue;
                }
            }

            private <T> T getAndReturn(final String name, final Class<T> lookupType) {
                String value = contextLookup(context, name);
                if(String.class == lookupType)
                {
                    return (T) value;
                }
                try {
                    Method m = lookupType.getMethod("valueOf", String.class);
                    return lookupType.cast(m.invoke(null, value));
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Could not find a valueOf method on " + lookupType);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Could not invoke valueOf method on " + lookupType);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not invoke valueOf method on " + lookupType);
                }
            }
        };
    }

    protected abstract boolean contains(C context, String key);

    protected abstract String contextLookup(C context, String key);
}
