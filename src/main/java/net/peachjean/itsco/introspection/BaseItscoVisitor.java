package net.peachjean.itsco.introspection;

import java.lang.reflect.Method;

/**
 * Base visitor class that helps implementations from becoming stale as the visitor interface is expanded.
 * @param <T>
 * @param <I>
 */
public class BaseItscoVisitor<T, I> implements ItscoVisitor<T, I> {
    @Override
    public <P> void visitSimple(String name, Method method, Class<P> propertyType, boolean required, I input) {
        // do nothing
    }

    @Override
    public <P> void visitItsco(String name, Method method, Class<P> propertyType, boolean required, I input) {
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
