package net.peachjean.confobj.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Allows for lookup of objects that should only be generated once.  Guarantees that for a given key K, the same value V
 * will always be returned. However, it is fully possible that the loader may be called more than once for any given K -
 * however the results second and subsequent calls will be thrown away.
 * @param <K>
 * @param <V>
 */
class SimpleCache<K, V> {
    private final ConcurrentMap<K, V> backingMap = new ConcurrentHashMap<K, V>();
    private final Loader<K, V> loader;

    SimpleCache(Loader<K, V> loader) {
        this.loader = loader;
    }

    V get(K key) {
        if(!this.backingMap.containsKey(key)) {
            this.backingMap.putIfAbsent(key, this.loader.load(key));
        }
        return this.backingMap.get(key);
    }

    static interface Loader<K, V> {
        V load(K key);
    }

    static <K, V> SimpleCache<K, V> build(Loader<K, V> loader) {
        return new SimpleCache<K, V>(loader);
    }
}
