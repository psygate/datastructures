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
    public IDPoint getLower(int axisindex);

    /**
     * @param axisindex Axis of the lower point.
     * @return Upper point of this bounding box. To satisfy the interface, the
     * lower point must satisfy lower.getX() &gt; upper.getX() &amp;&amp;
     * lower.getY() &gt; upper.getY();
     */
    public IDPoint getUpper(int axisindex);

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
        
        
    }

    /**
     * Merges this box with another, creating a bounding box that envelopes both
     * boxes.
     *
     * @param other Box to merge with.
     * @return Merged bounding box.
     */
    default IDBoundingBox merge(IDBoundingBox other) {
        return build(
                Math.min(other.getLower().getX(), getLower().getX()),
                Math.min(other.getLower().getY(), getLower().getY()),
                Math.max(other.getUpper().getX(), getUpper().getX()),
                Math.max(other.getUpper().getY(), getUpper().getY())
        );
    }

    /**
     * This method splits the bounding box in half on the x axis.<br>
     *
     * @return Array containing two bounding boxes. The bounding box at index 0
     * represents the lower part of the split, the bounding box at index 1
     * represents the upper part of the split.<br>
     * This method must be overwritten if a lot of splits are performed, as only
     * referential bounding boxes are generated the traversal of the split chain
     * can be very long, leading to slow operation. Additionally, all preceding
     * bounding boxes are not eligible for garbage collection, as long as a
     * split version of them is still reachable. If the call stack becomes too
     * big, a {@link java.lang.StackOverflowError} might be thrown.
     */
    default IDBoundingBox[] splitMidX() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double cx = getCenterX();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new IDBoundingBox[]{
            build(lx, ly, cx, uy),
            build(cx, ly, ux, uy)
        };
    }

    /**
     * This method splits the bounding box in half on the y axis.<br>
     *
     * @return Array containing two bounding boxes. The bounding box at index 0
     * represents the lower part of the split, the bounding box at index 1
     * represents the upper part of the split.<br>
     * This method must be overwritten if a lot of splits are performed, as only
     * referential bounding boxes are generated the traversal of the split chain
     * can be very long, leading to slow operation. Additionally, all preceding
     * bounding boxes are not eligible for garbage collection, as long as a
     * split version of them is still reachable. If the call stack becomes too
     * big, a {@link java.lang.StackOverflowError} might be thrown.
     */
    default IDBoundingBox[] splitMidY() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
//        final double cx = getCenterX();
        final double cy = getCenterY();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new IDBoundingBox[]{
            build(lx, ly, ux, cy),
            build(lx, cy, ux, uy)
        };
    }

    /**
     *
     * @param lowerpoint Lower point of the new bounding box.
     * @param upperpoint Upper point of the new bounding box.
     * @return New bounding box spanning from the lower point to the upper
     * point.
     */
    public static IDBoundingBox build(final IDPoint lowerpoint, final IDPoint upperpoint) {
        final IDPoint lower = IDPoint.build(lowerpoint);
        final IDPoint upper = IDPoint.build(upperpoint);
        return new IDBoundingBox() {
            @Override
            public IDPoint getLower() {
                return lower;
            }

            @Override
            public IDPoint getUpper() {
                return upper;
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

    /**
     *
     * @param lx Lower point x coordinate.
     * @param ly Lower point y coordinate.
     * @param ux Upper point x coordinate.
     * @param uy Upper point y coordinate.
     * @return Bounding box with IDPoint(lx,ly) as lower point and
     * IDPoint(ux,uy) as upper point.
     */
    public static IDBoundingBox build(final double lx, final double ly, final double ux, final double uy) {
        return build(IDPoint.build(lx, ly), IDPoint.build(ux, uy));
    }
}
