package net.peachjean.confobj.support;

import net.peachjean.confobj.ConfigObject;
import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

class ConfigObjectResolutionStrategy implements FieldResolutionStrategy {
    private ConfigObjectFactory configObjectFactory;

    public ConfigObjectResolutionStrategy(ConfigObjectFactory configObjectFactory) {
        this.configObjectFactory = configObjectFactory;
    }

    @Override
    public <T, C> T resolve(String name, GenericType<T> lookupType, Configuration configuration, C resolutionContext) {
        if (!this.supports(lookupType)) {
            throw new IllegalArgumentException("This strategy only supports configuration object types, not " + lookupType.getRawType().getName());
        }

        @SuppressWarnings("unchecked")
        Configuration subContext = configuration.subset (name);
        return configObjectFactory.create(subContext, lookupType.getRawType(), resolutionContext);
    }

    @Override
    public boolean supports(final GenericType<?> lookupType) {
        return lookupType.getRawType().isAnnotationPresent(ConfigObject.class);
    }

    @Override
    public boolean isContextBacked() {
        return true;
    }
}
