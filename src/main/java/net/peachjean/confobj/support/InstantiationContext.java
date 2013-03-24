package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;

import java.lang.annotation.Annotation;

public interface InstantiationContext {
    <T> T lookup(GenericType<T> type);

    <T> T lookup(GenericType<T> type, Annotation qualifier);
}
