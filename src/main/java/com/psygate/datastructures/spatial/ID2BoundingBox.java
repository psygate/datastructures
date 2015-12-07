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
 * Generalised two dimensional axis aligned bounding box interface containing
 * default implementations for the most common operations.
 *
 * @author psygate (https://github.com/psygate)
 */
public interface ID2BoundingBox extends ID2Boundable {

    /**
     *
     * @return Lower point of this bounding box. To satisfy the interface, the
     * lower point must satisfy lower.getX() &lt; upper.getX() &amp;&amp;
     * lower.getY() &lt; upper.getY();
     */
    public ID2Point getLower();

    /**
     *
     * @return Upper point of this bounding box. To satisfy the interface, the
     * lower point must satisfy lower.getX() &gt; upper.getX() &amp;&amp;
     * lower.getY() &gt; upper.getY();
     */
    public ID2Point getUpper();

    /**
     *
     * @return double representing the width of the bounding box.
     */
    default double getWidth() {
        assert getUpper().getX() >= getLower().getX();
        return getUpper().getX() - getLower().getX();
    }

    /**
     *
     * @return double representing the height of the bounding box.
     */
    default double getHeight() {
        assert getUpper().getY() >= getLower().getY();
        return getUpper().getY() - getLower().getY();
    }

    /**
     *
     * @return double representing the center point x axis coordinate.
     */
    default double getCenterX() {
        return (getUpper().getX() + getLower().getX()) / 2;
    }

    /**
     *
     * @return double representing the center point y axis coordinate.
     */
    default double getCenterY() {
        return (getUpper().getY() + getLower().getY()) / 2;
    }

    /**
     *
     * @return IDPoint representing the center point of this bounding box.
     */
    default ID2Point getCenter() {
        return ID2Point.build(getCenterX(), getCenterY());
    }

    /**
     * Checks if two BoundingBoxes intersect with each other. Intersection is
     * defined as atleast one edge of one bounding box being inside or touching
     * the edge of other.
     *
     * @param other Other bounding box.
     * @return True if the boxes intersect each other.
     */
    default boolean intersects(ID2BoundingBox other) {
        return (Math.abs(getCenterX() - other.getCenterX()) * 2 < (getWidth() + other.getWidth()))
                && (Math.abs(getCenterY() - other.getCenterY()) * 2 < (getHeight() + other.getHeight()));
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
    default boolean contains(ID2Boundable other) {
        return other.isInside(this);
    }

    /**
     * Checks if two bounding boxes represent the same space. They are the same
     * if both lower and upper points are the same.
     *
     * @param other Box to check against.
     * @return True if both boxes are the same.
     */
    default boolean same(ID2BoundingBox other) {
        return getLower().same(other.getLower()) && getUpper().same(other.getUpper());
    }

    @Override
    default boolean isInside(ID2BoundingBox other) {
        return getLower().getX() >= other.getLower().getX()
                && getUpper().getX() <= other.getUpper().getX()
                && getLower().getY() >= other.getLower().getY()
                && getUpper().getY() <= other.getUpper().getY();
    }

    /**
     * Merges this bounding box with the other, constructing a box that
     * envelopes both bounding boxes.
     *
     * @param other Box to merge with.
     * @return box that envelopes both bounding boxes.
     */
    default ID2BoundingBox merge(ID2BoundingBox other) {
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
    default ID2BoundingBox[] splitMidX() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double cx = getCenterX();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new ID2BoundingBox[]{
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
    default ID2BoundingBox[] splitMidY() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
//        final double cx = getCenterX();
        final double cy = getCenterY();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new ID2BoundingBox[]{
            build(lx, ly, ux, cy),
            build(lx, cy, ux, uy)
        };
    }

    @Override
    default boolean leftOf(double median, Axis2D axis) {
        return getLower().leftOf(median, axis) && getUpper().leftOf(median, axis);
    }

    @Override
    default boolean rightOf(double median, Axis2D axis) {
        return getLower().rightOf(median, axis) && getUpper().rightOf(median, axis);
    }

    /**
     *
     * @param lowerpoint Lower point of the new bounding box.
     * @param upperpoint Upper point of the new bounding box.
     * @return New bounding box spanning from the lower point to the upper
     * point.
     */
    public static ID2BoundingBox build(final ID2Point lowerpoint, final ID2Point upperpoint) {
        final ID2Point lower = ID2Point.build(lowerpoint);
        final ID2Point upper = ID2Point.build(upperpoint);
        return new ID2BoundingBox() {
            @Override
            public ID2Point getLower() {
                return lower;
            }

            @Override
            public ID2Point getUpper() {
                return upper;
            }
        };
    }

    /**
     *
     * @param box Box to copy.
     * @return An equal new bounding box.
     */
    public static ID2BoundingBox build(final ID2BoundingBox box) {
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
    public static ID2BoundingBox build(final double lx, final double ly, final double ux, final double uy) {
        return build(ID2Point.build(lx, ly), ID2Point.build(ux, uy));
    }
}
