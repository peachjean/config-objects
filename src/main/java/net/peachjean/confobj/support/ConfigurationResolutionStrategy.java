package net.peachjean.confobj.support;

import org.apache.commons.configuration.Configuration;

class ConfigurationResolutionStrategy implements FieldResolutionStrategy {
    @Override
    public <T, C> T resolve(String name, Class<T> lookupType, Configuration context, C resolutionContext) {
        if(!this.supports(lookupType)) {
            throw new IllegalArgumentException("I only support Configuration resolution.");
        }
        return (T) context.subset(name);
    }

    @Override
    public boolean supports(Class<?> lookupType) {
        return lookupType.equals(Configuration.class);
    }

    @Override
    public boolean isContextBacked() {
        return true;
    }
}
