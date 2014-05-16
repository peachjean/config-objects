package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;
import org.joda.convert.StringConvert;

public class JodaConvertResolutionStrategy implements FieldResolutionStrategy {
    public static final JodaConvertResolutionStrategy INSTANCE = new JodaConvertResolutionStrategy();
    @Override
    public <T, C> FieldResolution<T> resolve(final String name, final GenericType<T> type, final Configuration config, final C resolutionContext) {
        if (!this.supports(type)) {
            throw new IllegalArgumentException("This strategy only supports classes convertable by Joda Convert.");
        }
        return new FieldResolution.Simple<T>(ConfigurationUtils.determineFullPath(config, name)) {
            @Override
            protected T doResolve() {
                if(config.containsKey(name)) {
                    return StringConvert.INSTANCE.convertFromString(type.getRawType(), config.getString(name));
                } else {
                    return null;
                }
            }
        };
    }

    @Override
    public boolean supports(GenericType<?> lookupType) {
        return StringConvert.INSTANCE.isConvertible(lookupType.getRawType());
    }
}
