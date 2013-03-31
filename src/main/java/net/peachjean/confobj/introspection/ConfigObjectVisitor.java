package net.peachjean.confobj.introspection;

import java.lang.reflect.Method;

public interface ConfigObjectVisitor<T, I> {
    <P> void visitSimple(String name, Method method, GenericType<P> propertyType, boolean required, I input);

    <P> void visitConfigObject(String name, Method method, GenericType<P> propertyType, boolean required, I input);

    <P> void visitPrimitive(String name, Method method, Class<P> propertyType, boolean required, I input);

    void visitDefaults(Class<? extends T> defaultsClass, I input);

    void visitNamedImplementation(Class<? extends T> implementationClass, String name, I input);
}
