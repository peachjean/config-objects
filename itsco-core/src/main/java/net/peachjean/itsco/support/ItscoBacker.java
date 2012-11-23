package net.peachjean.itsco.support;

public interface ItscoBacker {
    <T> T lookup(final String name, final Class<T> lookupType);

    <T> T lookup(final String name, final Class<T> lookupType, T defaultValue);
}
