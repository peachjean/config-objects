package net.peachjean.itsco.support;

import org.apache.commons.configuration.Configuration;

public interface FieldResolutionStrategy {
    /**
     * Resolves the object any way it needs to.  If it cannot resolve it, {@code null} is returned.  If this strategy
     * does not support the requested lookupType, an exception is thrown.
     *
     * @throws IllegalArgumentException if an unsupported lookupType is passed
     */
    <T, C> T resolve(String name, Class<T> lookupType, Configuration context, C resolutionContext);

    boolean supports(Class<?> lookupType);

    boolean handlesReloading();
}
