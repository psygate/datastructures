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
 * A generalised immutable interface for SpatialTrees. This interface provides
 * some default implementations that make it easier to implement a spatial tree.
 *
 * @author psygate (https://github.com/psygate)
 * @param <K> Key type for the spatial tree.
 * @param <V> Value type for the spatial tree.
 * @param <B> Area type for the spatial tree.
 */
public interface SpatialTree<K, V, B> {

    /**
     *
     * @return Bounds of the tree.
     */
    public B getBounds();

    /**
     *
     * @param key Key to check if the key lays inside the bounding box of the
     * tree. Equivalent to getBounds().contains(key).
     * @return True if the key is inside the tree and could be inserted.
     */
    public boolean envelopes(K key);

    /**
     *
     * @return Number of elements inside the tree.
     */
    public int size();

    /**
     * Returns true if the tree is empty. An implementation of this interface
     * may override this to achieve a higher performance.
     *
     * @return True if the tree contains no elements.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     *
     * @return A stream providing all keys contained in the tree. Keys in the
     * stream may not be unique or distinct since a key can be associated with
     * more than one value.
     */
    default Stream<K> keyStream() {
        return entryStream().map(Map.Entry::getKey);
    }

    /**
     *
     * @return A stream providing all values contained in the tree.
     */
    default Stream<V> valueStream() {
        return entryStream().map(Map.Entry::getValue);
    }

    /**
     *
     * @return A stream providing all entries contained in the tree.
     */
    default Stream<Map.Entry<K, V>> entryStream() {
        return selectiveEntryStream((B t) -> true);
    }

    /**
     *
     * @param predicate A predicate to use to discard candidate nodes before
     * checking the subtree.
     * @return A stream of keys that are only contained in nodes where
     * predicate.test(node.getBounds()) is true.
     */
    default Stream<K> selectiveKeyStream(Predicate<B> predicate) {
        return selectiveEntryStream(predicate).map(Map.Entry::getKey);
    }

    /**
     *
     * @param predicate A predicate to use to discard candidate nodes before
     * checking the subtree.
     * @return A stream of values that are only contained in nodes where
     * predicate.test(node.getBounds()) is true.
     */
    default Stream<V> selectiveValueStream(Predicate<B> predicate) {
        return selectiveEntryStream(predicate).map(Map.Entry::getValue);
    }

    /**
     *
     * @param predicate A predicate to use to discard candidate nodes before
     * checking the subtree.
     * @return A stream of entries that are only contained in nodes where
     * predicate.test(node.getBounds()) is true.
     */
    public Stream<Map.Entry<K, V>> selectiveEntryStream(Predicate<B> predicate);

    /**
     *
     * @return An iterator iterating over all keys of the tree. The iterator can
     * provide a key more than once, if the key is contained more than once
     * inside the tree.
     */
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

    /**
     *
     * @return An iterator iterating over all values of the tree.
     */
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

    /**
     *
     * @return An iterator iterating over all entries of the tree.
     */
    default Iterator<Map.Entry<K, V>> entryIterator() {
        return entryStream().iterator();
    }

    /**
     *
     * @param predicate A predicate to use to discard candidate nodes before
     * checking the subtree.
     * @return An iterator iterating over all keys of the tree. The iterator can
     * provide a key more than once, if the key is contained more than once
     * inside the tree.
     */
    default Iterator<K> selectiveKeyIterator(Predicate<B> predicate) {
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

    /**
     *
     * @param predicate A predicate to use to discard candidate nodes before
     * checking the subtree.
     * @return An iterator iterating over all values of the tree.
     */
    default Iterator<V> selectiveValueIterator(Predicate<B> predicate) {
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

    /**
     *
     * @param predicate A predicate to use to discard candidate nodes before
     * checking the subtree.
     * @return An iterator iterating over all entries of the tree.
     */
    default Iterator<Map.Entry<K, V>> selectiveEntryIterator(Predicate<B> predicate) {
        return selectiveEntryStream(predicate).iterator();
    }

    /**
     *
     * @return Collection containing all keys in the tree. The collection can
     * contain a key more than once, if the key is contained more than once
     * inside the tree.
     */
    default Collection<K> keys() {
        return keyStream().collect(Collectors.toList());
    }

    /**
     *
     * @return Collection containing all values in the tree.
     */
    default Collection<V> values() {
        return valueStream().collect(Collectors.toList());

    }

    /**
     *
     * @return Collection containing all entries in the tree.
     */
    default Collection<Map.Entry<K, V>> entries() {
        return entryStream().collect(Collectors.toList());
    }

    /**
     *
     * @param key Key to search for.
     * @return True if the key is contained in the tree and associated with a
     * value.
     */
    public boolean containsKey(K key);

    /**
     *
     * @param key Key to search for.
     * @param value Value associated with <b>key</b> to search for.
     * @return True if the tree contains at least one entry where
     * Objects.equals(key, entry.getKey()) &amp;&amp; Objects.equals(value,
     * entry.getValue()).
     */
    public boolean contains(K key, V value);

    /**
     * Hinted search for value. The predicate is used to discard nodes that do
     * not need to be traversed in order to search for the value.
     *
     * @param value Value to search for.
     * @param pred Predicate used to discard nodes that do not need to be
     * traversed in order to search for the value.
     * @return True if the tree contains a value where Objects.equals(value,
     * treeValue) is true, that is in a node of which the bounding box suffices
     */
    public boolean containsValue(V value, Predicate<B> pred);

    /**
     *
     * @param value Value to search for.
     * @return True if the tree contains the value.
     */
    public boolean containsValue(V value);
}
