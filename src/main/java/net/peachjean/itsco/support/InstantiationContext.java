package net.peachjean.itsco.support;

import java.lang.annotation.Annotation;

public interface InstantiationContext {
    <T> T lookup(GenericType<T> type);

    <T> T lookup(GenericType<T> type, Annotation qualifier);
}
