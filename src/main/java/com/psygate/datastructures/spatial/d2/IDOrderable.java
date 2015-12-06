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
package com.psygate.datastructures.spatial.d2;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public interface IDOrderable {

    /**
     * Checks if this is inside the provided bounding box.
     *
     * @param box Bounding box to check against.
     * @return True if this is inside the box, else false.
     */
    boolean isInside(IDBoundingBox box);

    /**
     * Checks if this object is left or on a pivot provided.
     *
     * @param median The pivot point.
     * @param axis Axis on which to check.
     * @return True if the object is left of the pivot.
     */
    boolean leftOf(double median, Axis axis);

    /**
     * Checks if this object is right of a pivot provided.
     *
     * @param median The pivot point.
     * @param axis Axis on which to check.
     * @return True if the object is right of the pivot.
     */
    boolean rightOf(double median, Axis axis);
}
