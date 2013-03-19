package net.peachjean.itsco.support;

/**
 * An instance of this object is provided to every implementation of an itsco interface.  It's where the real work
 * mapping properties to values is done.
 */
public interface ItscoBacker {
    <T> T lookup(final String name, final Class<T> lookupType);

    <T> T lookup(final String name, final Class<T> lookupType, T defaultValue);


}
