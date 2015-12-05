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

/**
 * Interface to specify that the implementing tree has an internal node size
 * that can be queried.
 *
 * @author psygate (https://github.com/psygate)
 * @param <K> Key type for the spatial tree.
 * @param <V> Value type for the spatial tree.
 * @param <B> Area type for the spatial tree.
 */
public interface NodeSizeSpatialTree<K, V, B> extends SpatialTree<K, V, B> {

    /**
     *
     * @return Maximum internal node size (maximum number of values contained
     * inside the node).
     */
    public int getMaxNodeSize();
}
