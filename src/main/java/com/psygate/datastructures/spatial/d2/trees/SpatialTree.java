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
package com.psygate.datastructures.spatial.d2.trees;

import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
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

    public boolean isEmpty();

    public Stream<K> keyStream();

    public Stream<V> valueStream();

    public Stream<Map.Entry<K, V>> entryStream();

    public Stream<K> selectiveKeyStream(Predicate<IDBoundingBox> predicate);

    public Stream<V> selectiveValueStream(Predicate<IDBoundingBox> predicate);

    public Stream<Map.Entry<K, V>> selectiveEntryStream(Predicate<IDBoundingBox> predicate);

    public Iterator<K> keyIterator();

    public Iterator<V> valueIterator();

    public Iterator<Map.Entry<K, V>> entryIterator();

    public Iterator<K> selectiveKeyIterator(Predicate<IDBoundingBox> predicate);

    public Iterator<V> selectiveValueIterator(Predicate<IDBoundingBox> predicate);

    public Iterator<Map.Entry<K, V>> selectiveEntryIterator(Predicate<IDBoundingBox> predicate);

    public Collection<K> keys();

    public Collection<V> values();

    public Collection<Map.Entry<K, V>> entries();

    public boolean containsKey(K key);

    public boolean contains(K key, V value);

    public boolean containsValue(V value, Predicate<IDBoundingBox> pred);

    public boolean containsValue(V value);
}
