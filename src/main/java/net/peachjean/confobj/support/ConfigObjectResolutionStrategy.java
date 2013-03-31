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
    public <T, C> FieldResolution<T> resolve(String name, GenericType<T> lookupType, Configuration configuration, C resolutionContext) {
        if (!this.supports(lookupType)) {
            throw new IllegalArgumentException("This strategy only supports configuration object types, not " + lookupType.getRawType().getName());
        }

        @SuppressWarnings("unchecked")
        Configuration subContext = configuration.subset (name);
        T resolved = configObjectFactory.create(subContext, lookupType.getRawType(), resolutionContext);
        return new FieldResolution.Resolved<T>(ConfigurationUtils.determineFullPath(configuration, name), resolved);
    }

    @Override
    public boolean supports(final GenericType<?> lookupType) {
        return lookupType.getRawType().isAnnotationPresent(ConfigObject.class);
    }
}
