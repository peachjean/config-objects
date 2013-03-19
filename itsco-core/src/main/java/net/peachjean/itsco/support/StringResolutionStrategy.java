package net.peachjean.itsco.support;

import org.apache.commons.configuration.Configuration;

class StringResolutionStrategy implements FieldResolutionStrategy {

    public static final StringResolutionStrategy INSTANCE = new StringResolutionStrategy();

    @Override
    public <T, C> T resolve(final String name, final Class<T> lookupType, final Configuration config) {
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
    public boolean supports(final Class<?> lookupType) {
        return String.class.equals(lookupType);
    }

    @Override
    public boolean handlesReloading() {
        return false;
    }
}
