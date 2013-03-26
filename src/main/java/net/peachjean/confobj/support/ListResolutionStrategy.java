package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

import java.util.AbstractList;
import java.util.List;

class ListResolutionStrategy implements FieldResolutionStrategy, FieldResolutionStrategy.RequiresDeterminer {

    private Determiner determiner;

    @Override
    public <T, C> T resolve(String name, GenericType<T> type, Configuration context, C resolutionContext) {
        GenericType<?> memberType = type.getParameters().get(0);
        FieldResolutionStrategy frs = determiner.determineStrategy(memberType);
        return type.cast(new ConfigBackedList(new CollectionHelper(name, memberType, context, frs, resolutionContext)));
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
        private final CollectionHelper<T> collectionHelper;

        ConfigBackedList(CollectionHelper<T> collectionHelper) {
            this.collectionHelper = collectionHelper;
        }

        @Override
        public T get(int index) {
            return this.collectionHelper.resolve(index);
        }

        @Override
        public int size() {
            return this.collectionHelper.size();
        }
    }

}
