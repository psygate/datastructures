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

import com.psygate.datastructures.spatial.trees.SpatialTree;
/**
 *
 * @author psygate (https://github.com/psygate)
 */

/**
 * Generalised interface implementing a two dimensional BoundingBox tree.
 * Provides useful default methods for two dimensional trees that use bounding
 * boxes.
 *
 * @author psygate (https://github.com/psygate)
 *
 * @param <K> Key type for the spatial tree.
 * @param <V> Value type for the spatial tree.
 * @param <Q> Area type for the spatial tree.
 * @param <L> Bound type for the spatial tree.
 */
public interface BoundedSpatialTree<K, V, Q, L> extends SpatialTree<K, V, Q> {

    public L getBounds();

    @Override
    public boolean envelopes(K key);
}
