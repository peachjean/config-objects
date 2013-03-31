package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

class ConfigurationConfigObjectBacker<I> implements ConfigObjectBacker<I> {

    // contains values that handle reloading on their own - these are often more expensive to create so we
    // don't want to recreate them on every get call
    private final Map<String, FieldResolution<?>> cachedValues;
    private final Configuration context;
    private final FieldResolutionStrategy.Determiner strategyDeterminer;
    private I containing;

    public ConfigurationConfigObjectBacker(Configuration context, FieldResolutionStrategy.Determiner strategyDeterminer) {
        this.context = context;
        this.strategyDeterminer = strategyDeterminer;
        cachedValues = new HashMap<String, FieldResolution<?>>();
    }

    @Override
    public void setContaining(I containing) {
        this.containing = containing;
    }

    @Override
    public <T> T lookup(final String name, final GenericType<T> lookupType) {
        return this.locateResolution(name, lookupType).resolve();
    }

    @Override
    public <T> T lookup(final String name, final GenericType<T> lookupType, final T defaultValue) {
        return this.locateResolution(name, lookupType).resolve(defaultValue);
    }

    private <T> FieldResolution<T> locateResolution(final String name, final GenericType<T> lookupType) {
        this.validateState();
        if(!this.cachedValues.containsKey(name)) {
            FieldResolutionStrategy resolutionStrategy = this.determineStrategy(lookupType);
            FieldResolution<T> resolution = resolutionStrategy.resolve(name, lookupType, context, containing);
            this.cachedValues.put(name, resolution);
        }
        return (FieldResolution<T>) this.cachedValues.get(name);
    }

    private void validateState() {
        if(this.containing == null) {
            throw new IllegalStateException("A containing object must be set before the backer is usable.");
        }
    }

    protected <T> FieldResolutionStrategy determineStrategy(final GenericType<T> lookupType) {
        return this.strategyDeterminer.determineStrategy(lookupType);
    }
}
