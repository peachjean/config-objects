package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

public interface FieldResolutionStrategy {
    /**
     * Resolves the object any way it needs to.  If it cannot resolve it, {@code null} is returned.  If this strategy
     * does not support the requested lookupType, an exception is thrown.
     *
     * @throws IllegalArgumentException if an unsupported lookupType is passed
     */
    <T, C> FieldResolution<T> resolve(String name, GenericType<T> type, Configuration context, C resolutionContext);

    boolean supports(GenericType<?> lookupType);

    /**
     * If this method returns {@code true} then any object returned by {@link #resolve} will change dynamically as the
     * backing configuration changes.
     *
     * This means that when a resolution strategy is context-backed, any clients should retain references returned by
     * {@link #resolve}, rather than re-invoking it every time.
     * @return
     * @deprecated This should not be used anymore.  Simply store the {@link FieldResolution} returned from the
     * {@link #resolve} method and let the strategy care about if the resulting object is context-backed or not.
     */
    @Deprecated
    boolean isContextBacked();

    /**
     * This is used by strategies for "container" objects - that delegate to other strategies.
     */
    static interface Determiner {
        boolean isStrategyAvailable(GenericType<?> type);

        FieldResolutionStrategy determineStrategy(GenericType<?> type);
    }

    static interface RequiresDeterminer {
        void setDeterminer(Determiner determiner);
    }
}
