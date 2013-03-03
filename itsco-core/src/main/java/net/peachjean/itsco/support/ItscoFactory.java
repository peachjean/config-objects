package net.peachjean.itsco.support;

import org.apache.commons.collections.Transformer;

public interface ItscoFactory<C> {
    <T> T create(C context, Class<T> itscoClass);

    <T> Transformer<C, T> createGenerator(Class<T> itscoClass);
}
