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
 * Generalised N-dimensional axis aligned bounding box interface containing
 * default implementations for the most common operations.
 *
 * @author psygate (https://github.com/psygate)
 */
public interface IDBoundingBox extends IDBoundingBoxContainable, Dimensioned {

    /**
     *
     * @param axisindex Axis of the lower point.
     * @return Lower point of this bounding box. To satisfy the interface, the
     * lower point must satisfy lower.getX() &lt; upper.getX() &amp;&amp;
     * lower.getY() &lt; upper.getY();
     */
    default IDPoint getLower(int axisindex) {
        return getLower()[axisindex];
    }

    /**
     *
     * @return Array containing all lower points of this bounding box.
     */
    public IDPoint[] getLower();

    /**
     * @param axisindex Axis of the lower point.
     * @return Upper point of this bounding box. To satisfy the interface, the
     * lower point must satisfy lower.getX() &gt; upper.getX() &amp;&amp;
     * lower.getY() &gt; upper.getY();
     */
    default IDPoint getUpper(int axisindex) {
        return getUpper()[axisindex];
    }

    /**
     *
     * @return Array containing all upper points of this bounding box.
     */
    public IDPoint[] getUpper();

    default double length(int axisindex) {
        return getUpper(axisindex).get(axisindex) - getLower(axisindex).get(axisindex);
    }

    /**
     * @param axisindex Axis of the lower point.
     * @return double representing the center point x axis coordinate.
     */
    default double getCenter(int axisindex) {
        return (getUpper(axisindex).get(axisindex) + getLower(axisindex).get(axisindex)) / 2;
    }

    /**
     *
     * @return IDPoint representing the center point of this bounding box.
     */
    default IDPoint getCenter() {
        double[] centers = new double[getDimensions()];
        for (int i = 0; i < getDimensions(); i++) {
            centers[i] = getCenter(i);
        }
        return IDPoint.build(centers);
    }

    /**
     * Checks if two BoundingBoxes intersect with each other. Intersection is
     * defined as atleast one edge of one bounding box being inside or touching
     * the edge of other.
     *
     * @param other Other bounding box.
     * @return True if the boxes intersect each other.
     */
    default boolean intersects(IDBoundingBox other) {
        if (other.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch.");
        }

        IDPoint center = getCenter();
        IDPoint others = other.getCenter();

        for (int i = 0; i < getDimensions(); i++) {
            if (Math.abs(center.get(i) - others.get(i)) * 2 > (length(i) + other.length(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this bounding box contains the other bounding box. Contains is
     * defined as all edges of other laying inside or on edges of this bounding
     * box.
     *
     *
     * @param other Bounding box to check if inside this bounding box.
     * @return True if the other bounding box is inside this bounding box.f
     */
    default boolean contains(IDBoundingBoxContainable other) {
        return other.isInside(this);
    }

    /**
     * Checks if two bounding boxes represent the same space. They are the same
     * if both lower and upper points are the same.
     *
     * @param other Box to check against.
     * @return True if both boxes are the same.
     */
    default boolean same(IDBoundingBox other) {

        if (other.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch.");
        }

        for (int i = 0; i < getDimensions(); i++) {
            if (!(getLower(i).same(other.getLower(i)) || !(getUpper(i).same(other.getUpper(i))))) {
                return false;
            }
        }

        return true;
    }

    @Override
    default boolean isInside(IDBoundingBox other) {

        if (other.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch.");
        }

        for (int i = 0; i < getDimensions(); i++) {
            if (!(getLower(i).get(i) >= other.getLower(i).get(i)) || !(getUpper(i).get(i) <= other.getUpper(i).get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Merges this box with another, creating a bounding box that envelopes both
     * boxes.
     *
     * @param other Box to merge with.
     * @return Merged bounding box.
     */
    default IDBoundingBox merge(IDBoundingBox other) {

        if (other.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch.");
        }

        IDPoint[] lowers = new IDPoint[other.getDimensions()];
        IDPoint[] uppers = new IDPoint[other.getDimensions()];

        for (int i = 0; i < getDimensions(); i++) {
            double[] lowercoords = new double[getDimensions()];
            double[] uppercoords = new double[getDimensions()];

            for (int j = 0; j < getDimensions(); j++) {
                lowercoords[i] = Math.min(getLower(i).get(j), other.getLower(i).get(j));
                uppercoords[i] = Math.max(getUpper(i).get(j), other.getUpper(i).get(j));
            }

            lowers[i] = IDPoint.build(lowercoords);
            uppers[i] = IDPoint.build(uppercoords);
        }

        return build(lowers, uppers);
    }

    /**
     *
     * @param lowerpoints Lower points of the new bounding box.
     * @param upperpoints Upper points of the new bounding box.
     * @return New bounding box spanning from the lower point to the upper
     * point.
     */
    public static IDBoundingBox build(final IDPoint[] lowerpoints, final IDPoint[] upperpoints) {
        if (lowerpoints.length != upperpoints.length) {
            throw new IllegalArgumentException("Dimension mismatch.");
        }

        IDPoint[] lowers = new IDPoint[lowerpoints.length];
        IDPoint[] uppers = new IDPoint[upperpoints.length];

        for (int i = 0; i < lowerpoints.length; i++) {
            lowers[i] = IDPoint.build(lowerpoints[i]);
            uppers[i] = IDPoint.build(upperpoints[i]);
        }

        return new IDBoundingBox() {
            @Override
            public IDPoint[] getLower() {
                return lowers;
            }

            @Override
            public IDPoint[] getUpper() {
                return uppers;
            }

            @Override
            public int getDimensions() {
                return lowers.length;
            }

        };
    }

    /**
     *
     * @param box Box to copy.
     * @return An equal new bounding box.
     */
    public static IDBoundingBox build(final IDBoundingBox box) {
        return build(box.getLower(), box.getUpper());
    }
}
