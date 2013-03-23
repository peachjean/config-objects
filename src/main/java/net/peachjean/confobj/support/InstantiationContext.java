package net.peachjean.confobj.support;

import java.lang.annotation.Annotation;

public interface InstantiationContext {
    <T> T lookup(GenericType<T> type);

    <T> T lookup(GenericType<T> type, Annotation qualifier);
}
