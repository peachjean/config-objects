package net.peachjean.confobj.introspection;

import java.lang.reflect.Method;

/**
 * Base visitor class that helps implementations from becoming stale as the visitor interface is expanded.
 * @param <T>
 * @param <I>
 */
public class BaseConfigObjectVisitor<T, I> implements ConfigObjectVisitor<T, I> {
    @Override
    public <P> void visitSimple(String name, Method method, Class<P> propertyType, boolean required, I input) {
        // do nothing
    }

    @Override
    public <P> void visitConfigObject(String name, Method method, Class<P> propertyType, boolean required, I input) {
        // do nothing
    }

    @Override
    public <P> void visitPrimitive(String name, Method method, Class<P> propertyType, boolean required, I input) {
        // do nothing
    }

    @Override
    public void visitDefaults(Class<? extends T> defaultsClass, I input) {
        // do nothing
    }
}
