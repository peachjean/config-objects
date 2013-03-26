package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

class SetResolutionStrategy implements FieldResolutionStrategy, FieldResolutionStrategy.RequiresDeterminer {
    private Determiner determiner;

    @Override
    public <T, C> T resolve(String name, GenericType<T> type, Configuration context, C resolutionContext) {
        GenericType<?> memberType = type.getParameters().get(0);
        FieldResolutionStrategy frs = determiner.determineStrategy(memberType);
        return type.cast(new ConfigBackedSet(new CollectionHelper(name, memberType, context, frs, resolutionContext)));
    }

    @Override
    public boolean supports(GenericType<?> lookupType) {
        return lookupType.getRawType().equals(Set.class);
    }

    @Override
    public boolean isContextBacked() {
        return true;
    }

    @Override
    public void setDeterminer(Determiner determiner) {
        this.determiner = determiner;
    }

    private static class ConfigBackedSet<T> extends AbstractSet<T> {
        private final CollectionHelper<T> collectionHelper;

        private ConfigBackedSet(CollectionHelper<T> collectionHelper) {
            this.collectionHelper = collectionHelper;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int index = 0;
                @Override
                public boolean hasNext() {
                    return index < ConfigBackedSet.this.size();
                }

                @Override
                public T next() {
                    if(!this.hasNext()) {
                        throw new NoSuchElementException("Iterator exhausted.");
                    }
                    return ConfigBackedSet.this.collectionHelper.resolve(index++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Remove not supported on this iterator.");
                }
            };
        }

        @Override
        public int size() {
            return this.collectionHelper.size();
        }
    }
}
