package net.peachjean.itsco.support;

public interface FieldResolutionStrategy {
    /**
     * Resolves the object any way it needs to.  If it cannot resolve it, {@code null} is returned.  If this strategy
     * does not support the requested lookupType, an exception is thrown.
     * @throws IllegalArgumentException if an unsupported lookupType is passed
     */
    <T,C> T resolve(String name, Class<T> lookupType, C context, ContextAccessor<C> contextAccessor);

    boolean supports(Class<?> lookupType);

    boolean handlesReloading();
}
