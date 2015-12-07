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
 * Generalised two dimensional point interface containing default
 * implementations for the most common operations.
 *
 * @author psygate (https://github.com/psygate)
 */
public interface ID3Point extends ID3Boundable {

    /**
     *
     * @return X coordinate of the point.
     */
    public double getX();

    /**
     *
     * @return Y coordinate of the point.
     */
    public double getY();

    /**
     *
     * @return Y coordinate of the point.
     */
    public double getZ();

    /**
     *
     * @param other Point to check against.
     * @return
     * <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Squared
     * euclidian distance</a> between this and the other point.
     */
    default double distSqr(ID3Point other) {
        final double xdist = (getX() - other.getX());
        final double ydist = (getY() - other.getY());
        final double zdist = (getZ() - other.getZ());

        return xdist * xdist + ydist * ydist + zdist * zdist;
    }

    /**
     *
     * @param other Point to check against.
     * @return
     * <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Euclidian
     * distance</a> between this and the other point.
     */
    default double dist(ID3Point other) {
        return Math.sqrt(distSqr(other));
    }

    /**
     *
     * @param axis Axis for which the coordinate should be returned.
     * @return Double representing the coordinate of the point on the provided
     * axis.
     * @throws IllegalArgumentException if the axis is null or unknown.
     */
    default double get(Axis3D axis) {
        if (axis == null) {
            throw new IllegalArgumentException("Unknown axis: " + axis);
        }
        switch (axis) {
            case X:
                return getX();
            case Y:
                return getY();
            case Z:
                return getZ();
            default:
                throw new IllegalArgumentException("Unknown axis: " + axis);
        }
    }

    @Override
    default boolean leftOf(double median, Axis3D axis) {
        return get(axis) <= median;
    }

    @Override
    default boolean rightOf(double median, Axis3D axis) {
        return get(axis) > median;
    }

    @Override
    default boolean isInside(ID3BoundingBox box) {
        return box.getLower().getX() <= getX() && box.getUpper().getX() >= getX()
                && box.getLower().getY() <= getY() && box.getUpper().getY() >= getY()
                && box.getLower().getZ() <= getZ() && box.getUpper().getZ() >= getZ();
    }

    /**
     * Checks if two points are the same.
     *
     * @param point Point to check against.
     * @return True if both points represent the same coordinates. (x == other.x
     * &amp;&amp; y == other.y)
     */
    default boolean same(ID3Point point) {
        return point.getX() == getX() && point.getY() == getY() && point.getZ() == getZ();
    }

    @Override
    public default ID3BoundingBox merge(ID3BoundingBox box) {
        if (box.contains(this)) {
            return box;
        } else {
            return ID3BoundingBox.build(
                    Math.min(getX(), box.getLower().getX()),
                    Math.min(getY(), box.getLower().getY()),
                    Math.min(getZ(), box.getLower().getZ()),
                    Math.max(getX(), box.getUpper().getX()),
                    Math.max(getY(), box.getUpper().getY()),
                    Math.max(getZ(), box.getUpper().getZ())
            );
        }
    }

    /**
     *
     * @param point Point to be copied.
     * @return New IDPoint instance that equals the provided point.
     */
    public static ID3Point build(ID3Point point) {
        final double x = point.getX();
        final double y = point.getY();
        final double z = point.getZ();

        return new ID3Point() {
            @Override
            public double getX() {
                return x;
            }

            @Override
            public double getY() {
                return y;
            }

            @Override
            public double getZ() {
                return z;
            }
        };
    }

    /**
     *
     * @param x X coordinate of the new point.
     * @param y Y coordinate of the new point.
     * @return New IDPoint instance that equals the provided coordinates.
     */
    public static ID3Point build(final double x, final double y, final double z) {
        return new ID3Point() {
            @Override
            public double getX() {
                return x;
            }

            @Override
            public double getY() {
                return y;
            }

            @Override
            public double getZ() {
                return z;
            }
        };
    }
}
