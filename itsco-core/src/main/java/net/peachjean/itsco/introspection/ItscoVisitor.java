package net.peachjean.itsco.introspection;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;

public interface ItscoVisitor<T,I> {
    <P> void visitSimple(String name, Method method, Class<P> propertyType, boolean required, I input);

    <P> void visitItsco(String name, Method method, Class<P> propertyType, boolean required, I input);

    <P> void visitPrimitive(String name, Method method, Class<P> propertyType, boolean required, I input);

    void visitDefaults(Class<? extends T> defaultsClass, I input);
}
