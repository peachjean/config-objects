package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

class ConfigurationResolutionStrategy implements FieldResolutionStrategy {
    public static final ConfigurationResolutionStrategy INSTANCE = new ConfigurationResolutionStrategy();

    @Override
    public <T, C> FieldResolution<T> resolve(String name, GenericType<T> lookupType, Configuration context, C resolutionContext) {
        if(!this.supports(lookupType)) {
            throw new IllegalArgumentException("I only support Configuration resolution.");
        }
        T resolved = lookupType.cast(context.subset(name));
        return new FieldResolution.Resolved<T>(ConfigurationUtils.determineFullPath(context, name), resolved);
    }

    @Override
    public boolean supports(GenericType<?> lookupType) {
        return lookupType.equals(Configuration.class);
    }

    @Override
    public boolean isContextBacked() {
        return true;
    }
}
