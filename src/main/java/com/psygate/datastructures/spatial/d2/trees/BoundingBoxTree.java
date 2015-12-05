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

import com.psygate.datastructures.spatial.SpatialTree;
/**
 *
 * @author psygate (https://github.com/psygate)
 */
import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import com.psygate.datastructures.spatial.d2.IDBoundingBoxContainable;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Generalised interface implementing a two dimensional BoundingBox tree.
 * Provides useful default methods for two dimensional trees that use bounding
 * boxes.
 *
 * @author psygate (https://github.com/psygate)
 *
 * @param <K> Key type for the spatial tree.
 * @param <V> Value type for the spatial tree.
 * @param <B> Area type for the spatial tree.
 */
public interface BoundingBoxTree<K extends IDBoundingBoxContainable, V, B extends IDBoundingBox> extends SpatialTree<K, V, B> {

    @Override
    default boolean containsKey(K key) {
        return selectiveKeyStream((bb) -> bb.contains(key))
                .anyMatch((candidate) -> Objects.equals(candidate, key));
    }

    @Override
    default boolean contains(K key, V value) {
        return selectiveEntryStream((bb) -> bb.contains(key))
                .anyMatch((candidate)
                        -> Objects.equals(candidate.getKey(), key)
                        && Objects.equals(candidate.getValue(), value));
    }

    @Override
    default boolean containsValue(V value, Predicate<B> pred) {
        return selectiveValueStream(pred)
                .anyMatch((candidate)
                        -> Objects.equals(candidate, value));
    }

    @Override
    default boolean containsValue(V value) {
        return valueStream()
                .anyMatch((candidate)
                        -> Objects.equals(candidate, value));
    }
}
