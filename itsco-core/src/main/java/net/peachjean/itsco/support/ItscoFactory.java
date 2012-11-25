package net.peachjean.itsco.support;

import com.google.common.base.Function;

public interface ItscoFactory<C> {
    <T> T create(C context, Class<T> itscoClass);

    <T> Function<C, T> createGenerator(Class<T> itscoClass);
}
