package net.peachjean.itsco.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ValueOfResolutionStrategy implements FieldResolutionStrategy {

    public static final ValueOfResolutionStrategy INSTANCE = new ValueOfResolutionStrategy();

    @Override
    public <T, C> T resolve(final String name, final Class<T> lookupType, final C context, final ContextAccessor<C> accessor) {

        if (accessor.contains(context, name)) {
            return getAndReturn(name, lookupType, context, accessor);
        } else {
            return null;
        }
    }

    private <T, C> T getAndReturn(final String name, final Class<T> lookupType, C context, ContextAccessor<C> accessor) {
        String value = accessor.contextLookup(context, name);
        try {
            Method m = lookupMethod(lookupType);
            if (m == null) {
                throw new IllegalArgumentException("Class " + lookupType.getName() + " does not have a valueOf method.");
            }
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
    public boolean supports(final Class<?> lookupType) {
        return lookupMethod(lookupType) != null;
    }

    @Override
    public boolean handlesReloading() {
        return false;
    }
}
