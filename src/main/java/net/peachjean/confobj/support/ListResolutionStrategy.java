package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.AbstractList;
import java.util.List;

class ListResolutionStrategy implements FieldResolutionStrategy, FieldResolutionStrategy.RequiresDeterminer {

    private Determiner determiner;

    @Override
    public <T, C> T resolve(String name, GenericType<T> type, Configuration context, C resolutionContext) {
        HierarchicalConfiguration hConfig = ConfigurationUtils.convertToHierarchical(context);
        GenericType<?> memberType = type.getParameters().get(0);
        FieldResolutionStrategy frs = determiner.determineStrategy(memberType);
        return type.cast(new ConfigBackedList(name, memberType, hConfig, frs, resolutionContext));
    }

    @Override
    public boolean supports(GenericType<?> lookupType) {
        return lookupType.getRawType().isAssignableFrom(List.class);
    }

    @Override
    public boolean isContextBacked() {
        return true;
    }

    @Override
    public void setDeterminer(Determiner determiner) {
        this.determiner = determiner;
    }

    static class ConfigBackedList<T> extends AbstractList<T> {
        private final String name;
        private final GenericType<T> memberType;
        private final HierarchicalConfiguration backingConfiguration;
        private final FieldResolutionStrategy frs;
        private final Object resolutionContext;

        ConfigBackedList(String name, GenericType<T> memberType, HierarchicalConfiguration backingConfiguration, FieldResolutionStrategy frs, Object resolutionContext) {
            this.name = name;
            this.memberType = memberType;
            this.backingConfiguration = backingConfiguration;
            this.frs = frs;
            this.resolutionContext = resolutionContext;
        }

        @Override
        public T get(int index) {
            return frs.resolve(name + "(" + index + ")", memberType, backingConfiguration, resolutionContext);
        }

        @Override
        public int size() {
            return backingConfiguration.getMaxIndex(name) + 1;
        }
    }
}
