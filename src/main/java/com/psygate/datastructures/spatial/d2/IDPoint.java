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
public interface IDPoint {

    public double getX();

    public double getY();

    default double distSqr(IDPoint other) {
        final double xdist = (getX() - other.getX());
        final double ydist = (getY() - other.getY());

        return xdist * xdist + ydist * ydist;
    }

    default double dist(IDPoint other) {
        return Math.sqrt(distSqr(other));
    }

    default double get(Axis axis) {
        if (axis == null) {
            throw new IllegalArgumentException("Unknown axis: " + axis);
        }
        switch (axis) {
            case X:
                return getX();
            case Y:
                return getY();
            default:
                throw new IllegalArgumentException("Unknown axis: " + axis);
        }
    }

    /**
     * Checks if two points are the same.
     *
     * @param point Point to check against.
     * @return True if both points represent the same coordinates. (x == other.x
     * && y == other.y)
     */
    default boolean same(IDPoint point) {
        return point.getX() == getX() && point.getY() == getY();
    }

    public static IDPoint build(IDPoint point) {
        final double x = point.getX();
        final double y = point.getY();

        return new IDPoint() {
            @Override
            public double getX() {
                return x;
            }

            @Override
            public double getY() {
                return y;
            }
        };
    }

    public static IDPoint build(final double x, final double y) {
        return new IDPoint() {
            @Override
            public double getX() {
                return x;
            }

            @Override
            public double getY() {
                return y;
            }
        };
    }
}
