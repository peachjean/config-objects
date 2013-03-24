package net.peachjean.confobj.support;

import org.apache.commons.configuration.Configuration;

class ConfigurationResolutionStrategy implements FieldResolutionStrategy {
    public static final ConfigurationResolutionStrategy INSTANCE = new ConfigurationResolutionStrategy();

    @Override
    public <T, C> T resolve(String name, GenericType<T> lookupType, Configuration context, C resolutionContext) {
        if(!this.supports(lookupType)) {
            throw new IllegalArgumentException("I only support Configuration resolution.");
        }
        return (T) context.subset(name);
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
