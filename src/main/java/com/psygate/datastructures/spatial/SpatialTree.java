/*
 * General datastructures.
 * Copyright (C) 2015  psygate (https://github.com/psygate)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */
package com.psygate.datastructures.spatial;

import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public interface SpatialTree<K, V, B> {

    public B getBounds();

//    public boolean put(K key, V value);
//
//    public void putAll(Map<K, V> map);
    public boolean envelopes(K point);

    public int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    default Stream<K> keyStream() {
        return entryStream().map(Map.Entry::getKey);
    }

    default Stream<V> valueStream() {
        return entryStream().map(Map.Entry::getValue);
    }

    default Stream<Map.Entry<K, V>> entryStream() {
        return selectiveEntryStream((IDBoundingBox t) -> true);
    }

    default Stream<K> selectiveKeyStream(Predicate<IDBoundingBox> predicate) {
        return selectiveEntryStream(predicate).map(Map.Entry::getKey);
    }

    default Stream<V> selectiveValueStream(Predicate<IDBoundingBox> predicate) {
        return selectiveEntryStream(predicate).map(Map.Entry::getValue);
    }

    public Stream<Map.Entry<K, V>> selectiveEntryStream(Predicate<IDBoundingBox> predicate);

    default Iterator<K> keyIterator() {
        return new Iterator<K>() {
            private final Iterator<Map.Entry<K, V>> it = entryIterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public K next() {
                return it.next().getKey();
            }
        };
    }

    default Iterator<V> valueIterator() {
        return new Iterator<V>() {
            private final Iterator<Map.Entry<K, V>> it = entryIterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public V next() {
                return it.next().getValue();
            }
        };
    }

    default Iterator<Map.Entry<K, V>> entryIterator() {
        return entryStream().iterator();
    }

    default Iterator<K> selectiveKeyIterator(Predicate<IDBoundingBox> predicate) {
        return new Iterator<K>() {
            private final Iterator<Map.Entry<K, V>> it = selectiveEntryIterator(predicate);

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public K next() {
                return it.next().getKey();
            }
        };
    }

    default Iterator<V> selectiveValueIterator(Predicate<IDBoundingBox> predicate) {
        return new Iterator<V>() {
            private final Iterator<Map.Entry<K, V>> it = selectiveEntryIterator(predicate);

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public V next() {
                return it.next().getValue();
            }
        };
    }

    default Iterator<Map.Entry<K, V>> selectiveEntryIterator(Predicate<IDBoundingBox> predicate) {
        return selectiveEntryStream(predicate).iterator();
    }

    default Collection<K> keys() {
        return keyStream().collect(Collectors.toList());
    }

    default Collection<V> values() {
        return valueStream().collect(Collectors.toList());

    }

    default Collection<Map.Entry<K, V>> entries() {
        return entryStream().collect(Collectors.toList());
    }

    public boolean containsKey(K key);

    public boolean contains(K key, V value);

    public boolean containsValue(V value, Predicate<IDBoundingBox> pred);

    public boolean containsValue(V value);
}
