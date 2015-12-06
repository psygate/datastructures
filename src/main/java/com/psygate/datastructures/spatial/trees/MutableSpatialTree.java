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
package com.psygate.datastructures.spatial.trees;

import com.psygate.datastructures.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface specifying methods to alter a mutable spatial tree. Implementing
 * trees must be mutable and throw a concurrent modification exception, if they
 * are concurrently modified. The exception can be thrown by best-effort, so the
 * caller has to lock the tree if multithreading is required.
 *
 * @author psygate (https://github.com/psygate)
 *
 * @param <K> Key type for the spatial tree.
 * @param <V> Value type for the spatial tree.
 * @param <Q> Query type for the spatial tree.
 */
public interface MutableSpatialTree<K, V, Q> extends SpatialTree<K, V, Q> {

    /**
     *
     * @param pair Pair to insert.
     */
    public void put(Pair<K, V> pair);

    /**
     *
     * @param key Key to insert with associated value.
     * @param value Value to insert associated with key.
     */
    default void put(K key, V value) {
        put(new Pair<>(key, value));
    }

    /**
     *
     * @param entry Entry to insert as a key -&gt; value mapping.
     */
    default void put(Map.Entry<K, V> entry) {
        put(new Pair<>(entry));
    }

    /**
     *
     * @param values Map of values to insert as a key -&gt; value mapping.
     */
    default void putAll(Map<K, V> values) {
        putAll(values.entrySet());
    }

    /**
     *
     * @param values Values to insert as a key -&gt; value mapping.
     */
    default void putAll(Collection<? extends Map.Entry<K, V>> values) {
        values.stream().forEach((en) -> put(en));
    }

    /**
     *
     * @param key Key to remove.
     * @return A list containing all values that were associated with the key.
     */
    public Collection<V> remove(K key);

    /**
     *
     * @param keys Keys to remove.
     * @return A list containing all values that were associated with the keys.
     */
    default Collection<V> remove(Collection<K> keys) {
        return keys.stream().flatMap((k) -> remove(k).stream()).collect(Collectors.toList());
    }

    /**
     *
     * @param key Key to remove, if it is associated with the value.
     * @param value Value to remove, if it is associated with the key.
     * @return A list containing all values that were associated with the key.
     */
    public Collection<V> remove(K key, V value);

    /**
     *
     * @param value Value to remove.
     * @return A list containing all values that equal the value.
     */
    default Collection<V> removeValue(V value) {
        return removeValue(value, (Q b) -> true);
    }

    /**
     *
     * @param values List of values to remove.F
     * @return A list containing all values that equal the values.
     */
    default Collection<V> removeValue(Collection<V> values) {
        return values.stream().flatMap((v) -> removeValue(v).stream()).collect(Collectors.toList());
    }

    /**
     * Hinted value removal function, discarding nodes that to not satisfy the
     * provided predicate early.
     *
     * @param value Value to remove.
     * @param hint Hint used to check if an encountered node should be checked
     * for the value or not.
     * @return Collection containing all removed values.
     */
    public Collection<V> removeValue(V value, Predicate<Q> hint);

    /**
     * Hinted value removal function, discarding nodes that to not satisfy the
     * provided predicate early.
     *
     * @param values Values to remove.
     * @param hint Hint used to check if an encountered node should be checked
     * for the value or not.
     * @return Collection containing all removed values.
     */
    default Collection<V> removeValue(Collection<V> values, Predicate<Q> hint) {
        return values.stream().flatMap((v) -> removeValue(v, hint).stream()).collect(Collectors.toList());
    }

    /**
     * Clears the complete tree, discarding all key -&gt; value mappings and all
     * subtrees.
     */
    public void clear();
}
