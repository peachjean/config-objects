package net.peachjean.confobj.support;

/**
 * An instance of this object is provided to every implementation of an configuration object  interface.  It's where
 * the real work mapping properties to values is done.
 */
public interface ConfigObjectBacker<I> {
    void setContaining(I containing);

    <T> T lookup(final String name, final Class<T> lookupType);

    <T> T lookup(final String name, final Class<T> lookupType, T defaultValue);


}
