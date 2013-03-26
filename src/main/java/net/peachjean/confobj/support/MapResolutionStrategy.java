package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

import java.util.*;

class MapResolutionStrategy implements FieldResolutionStrategy, FieldResolutionStrategy.RequiresDeterminer {
    private Determiner determiner;

    @Override
    public <T, C> T resolve(String name, GenericType<T> type, Configuration context, C resolutionContext) {
        GenericType<?> valueType = type.getParameters().get(1);

        return type.cast(new ConfigBackedMap(context.subset(name), valueType, determiner.determineStrategy(valueType), resolutionContext));
    }

    @Override
    public boolean supports(GenericType<?> lookupType) {
        return lookupType.getRawType().equals(Map.class) && lookupType.getParameters().size() == 2 &&
                lookupType.getParameters().get(0).getRawType().equals(String.class) &&
                determiner.isStrategyAvailable(lookupType.getParameters().get(1));
    }

    @Override
    public boolean isContextBacked() {
        return true;
    }

    @Override
    public void setDeterminer(Determiner determiner) {
        this.determiner = determiner;
    }

    private static class ConfigBackedMap<V> extends AbstractMap<String, V> {
        private final Configuration configuration;
        private final GenericType<V> valueType;
        private final FieldResolutionStrategy valueFrs;
        private final Object resolutionContext;

        private ConfigBackedMap(Configuration configuration, GenericType<V> valueType, FieldResolutionStrategy valueFrs, Object resolutionContext) {
            this.configuration = configuration;
            this.valueType = valueType;
            this.valueFrs = valueFrs;
            this.resolutionContext = resolutionContext;
        }

        @Override
        public Set<Entry<String, V>> entrySet() {
            Set<Entry<String, V>> set = new HashSet<Entry<String, V>>();
            Iterator<String> keys = configuration.getKeys();
            while(keys.hasNext()) {
                String key = keys.next();
                set.add(new SimpleEntry<String, V>(key, valueFrs.resolve(key, valueType, configuration, resolutionContext)));
            }
            return set;
        }
    }
}
