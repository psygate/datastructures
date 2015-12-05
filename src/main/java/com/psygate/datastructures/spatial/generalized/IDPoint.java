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
package com.psygate.datastructures.spatial.generalized;

/**
 * Generalised N dimensional point interface containing default implementations
 * for the most common operations.
 *
 * @author psygate (https://github.com/psygate)
 */
public interface IDPoint extends IDBoundingBoxContainable, Dimensioned {

    /**
     *
     * @param axisindex Index of the axis. (f.i.: x-&gt;0, y-&gt;1). Must be
     * &gt;=0 and &lt;getDimensions()
     * @return Coordinate of the point on the provided axis.
     */
    default double get(int axisindex) {
        return get()[axisindex];
    }

    /**
     *
     * @return Array representing all coordinates on all axis for this point.
     */
    public double[] get();

    /**
     *
     * @param other Point to check against.
     * @return
     * <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Squared
     * euclidian distance</a> between this and the other point.
     */
    default double distSqr(IDPoint other) {

        double sum = 0;
        for (int i = 0; i < getDimensions(); i++) {
            sum += (get(i) - other.get(i)) * (get(i) - other.get(i));
        }

        return sum;
    }

    /**
     *
     * @param other Point to check against.
     * @return
     * <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Euclidian
     * distance</a> between this and the other point.
     */
    default double dist(IDPoint other) {
        return Math.sqrt(distSqr(other));
    }

    @Override
    default boolean isInside(IDBoundingBox box) {
        if (box.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch.");
        }

        for (int i = 0; i < getDimensions(); i++) {
            IDPoint lower = box.getLower(i);
            IDPoint upper = box.getUpper(i);
            if (!(lower.get(i) <= get(i) && upper.get(i) >= get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if two points are the same.
     *
     * @param point Point to check against.
     * @return True if both points represent the same coordinates. (x == other.x
     * &amp;&amp; y == other.y)
     */
    default boolean same(IDPoint point) {
        if (point.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch.");
        }

        for (int i = 0; i < getDimensions(); i++) {
            if (!(point.get(i) == get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param point Point to be copied.
     * @return New IDPoint instance that equals the provided point.
     */
    public static IDPoint build(IDPoint point) {
        final double[] values = new double[point.getDimensions()];

        for (int i = 0; i < point.getDimensions(); i++) {
            values[i] = point.get(i);
        }

        return new IDPoint() {
            @Override
            public double[] get() {
                return values;
            }

            @Override
            public int getDimensions() {
                return values.length;
            }
        };
    }

    /**
     *
     * @param values Coordinate values of the new point.
     * @return New IDPoint instance that equals the provided coordinates.
     */
    public static IDPoint build(final double... values) {
        return new IDPoint() {

            @Override
            public double[] get() {
                return values;
            }

            @Override
            public int getDimensions() {
                return values.length;
            }
        };
    }
}
