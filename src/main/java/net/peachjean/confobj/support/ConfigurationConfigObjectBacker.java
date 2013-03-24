package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

class ConfigurationConfigObjectBacker<I> implements ConfigObjectBacker<I> {

    // contains values that handle reloading on their own - these are often more expensive to create so we
    // don't want to recreate them on every get call
    private final Map<String, Object> cachedValues;
    private final Configuration context;
    private final Iterable<FieldResolutionStrategy> strategies;
    private I containing;

    public ConfigurationConfigObjectBacker(Configuration context, Iterable<FieldResolutionStrategy> strategies) {
        this.context = context;
        this.strategies = strategies;
        cachedValues = new HashMap<String, Object>();
    }

    @Override
    public void setContaining(I containing) {
        this.containing = containing;
    }

    @Override
    public <T> T lookup(final String name, final GenericType<T> lookupType) {
        this.validateState();
        FieldResolutionStrategy resolutionStrategy = this.determineStrategy(lookupType);
        if (resolutionStrategy.isContextBacked()) {
            if (cachedValues.containsKey(name)) {
                return lookupType.cast(cachedValues.get(name));
            }
        }
        final T resolved = resolutionStrategy.resolve(name, lookupType, context, containing);
        if (resolved == null) {
            throw new IllegalStateException("No value for " + name);
        }
        if (resolutionStrategy.isContextBacked()) {
            cachedValues.put(name, resolved);
        }
        return resolved;
    }

    @Override
    public <T> T lookup(final String name, final GenericType<T> lookupType, final T defaultValue) {
        this.validateState();
        FieldResolutionStrategy resolutionStrategy = this.determineStrategy(lookupType);
        final T resolved = resolutionStrategy.resolve(name, lookupType, context, containing);
        return resolved != null ? resolved : defaultValue;
    }

    private void validateState() {
        if(this.containing == null) {
            throw new IllegalStateException("A containing object must be set before the backer is usable.");
        }
    }

    protected <T> FieldResolutionStrategy determineStrategy(final GenericType<T> lookupType) {
        for (FieldResolutionStrategy strategy : strategies) {
            if (strategy.supports(lookupType)) {
                return strategy;
            }
        }
        throw new IllegalStateException("No strategy to support type " + lookupType.getRawType().getName());
    }
}
