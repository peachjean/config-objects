package net.peachjean.confobj.support;

import org.apache.commons.configuration.Configuration;

class StringResolutionStrategy implements FieldResolutionStrategy {

    public static final StringResolutionStrategy INSTANCE = new StringResolutionStrategy();

    @Override
    public <T, C> T resolve(final String name, final GenericType<T> lookupType, final Configuration config, final C resolutionContext) {
        if (!this.supports(lookupType)) {
            throw new IllegalArgumentException("This strategy only supports Strings.");
        }
        if(!config.containsKey(name)) {
            return null;
        } else {
            return lookupType.cast(config.getString(name));
        }
    }

    @Override
    public boolean supports(final GenericType<?> lookupType) {
        return String.class.equals(lookupType.getRawType());
    }

    @Override
    public boolean isContextBacked() {
        return false;
    }
}
