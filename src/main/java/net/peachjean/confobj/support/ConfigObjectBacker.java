package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;

/**
 * An instance of this object is provided to every implementation of an configuration object interface.  It's where
 * the real work mapping properties to values is done.
 */
public interface ConfigObjectBacker<I> {
    void setContaining(I containing);

    <T> T lookup(final String name, final GenericType<T> lookupType);

    <T> T lookup(final String name, final GenericType<T> lookupType, T defaultValue);


}
