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
 * Generalised three dimensional axis aligned bounding box interface containing
 * default implementations for the most common operations.
 *
 * @author psygate (https://github.com/psygate)
 */
public interface ID3BoundingBox extends ID3Boundable {

    /**
     *
     * @return Lower point of this bounding box. To satisfy the interface, the
     * lower point must satisfy lower.getX() &lt; upper.getX() &amp;&amp;
     * lower.getY() &lt; upper.getY()&amp;&amp; lower.getZ() &lt; upper.getZ();
     */
    public ID3Point getLower();

    /**
     *
     * @return Upper point of this bounding box. To satisfy the interface, the
     * lower point must satisfy lower.getX() &gt; upper.getX() &amp;&amp;
     * lower.getY() &gt; upper.getY();
     */
    public ID3Point getUpper();

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
     * @return double representing the height of the bounding box.
     */
    default double getDepth() {
        assert getUpper().getZ() >= getLower().getZ();
        return getUpper().getZ() - getLower().getZ();
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
     * @return double representing the center point y axis coordinate.
     */
    default double getCenterZ() {
        return (getUpper().getZ() + getLower().getZ()) / 2;
    }

    /**
     *
     * @return IDPoint representing the center point of this bounding box.
     */
    default ID3Point getCenter() {
        return ID3Point.build(getCenterX(), getCenterY(), getCenterZ());
    }

    /**
     * Checks if two BoundingBoxes intersect with each other. Intersection is
     * defined as atleast one edge of one bounding box being inside or touching
     * the edge of other.
     *
     * @param other Other bounding box.
     * @return True if the boxes intersect each other.
     */
    default boolean intersects(ID3BoundingBox other) {
        return (Math.abs(getCenterX() - other.getCenterX()) * 2 < (getWidth() + other.getWidth()))
                && (Math.abs(getCenterY() - other.getCenterY()) * 2 < (getHeight() + other.getHeight())
                && (Math.abs(getCenterZ() - other.getCenterZ()) * 2 < (getDepth() + other.getDepth())));
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
    default boolean contains(ID3Boundable other) {
        return other.isInside(this);
    }

    /**
     * Checks if two bounding boxes represent the same space. They are the same
     * if both lower and upper points are the same.
     *
     * @param other Box to check against.
     * @return True if both boxes are the same.
     */
    default boolean same(ID3BoundingBox other) {
        return getLower().same(other.getLower()) && getUpper().same(other.getUpper());
    }

    @Override
    default boolean isInside(ID3BoundingBox other) {
        return getLower().getX() >= other.getLower().getX()
                && getUpper().getX() <= other.getUpper().getX()
                && getLower().getY() >= other.getLower().getY()
                && getUpper().getY() <= other.getUpper().getY()
                && getLower().getZ() >= other.getLower().getZ()
                && getUpper().getZ() <= other.getUpper().getZ();
    }

    /**
     * Merges this box with another, creating a bounding box that envelopes both
     * boxes.
     *
     * @param other Box to merge with.
     * @return Merged bounding box.
     */
    default ID3BoundingBox merge(ID3BoundingBox other) {
        return build(
                Math.min(other.getLower().getX(), getLower().getX()),
                Math.min(other.getLower().getY(), getLower().getY()),
                Math.min(other.getLower().getZ(), getLower().getZ()),
                Math.max(other.getUpper().getX(), getUpper().getX()),
                Math.max(other.getUpper().getY(), getUpper().getY()),
                Math.max(other.getUpper().getZ(), getUpper().getZ())
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
    default ID3BoundingBox[] splitMidX() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double lz = getLower().getZ();
        final double cx = getCenterX();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();
        final double uz = getUpper().getZ();
        return new ID3BoundingBox[]{
            build(lx, ly, lz, cx, uy, uz),
            build(cx, ly, lz, ux, uy, uz)
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
    default ID3BoundingBox[] splitMidY() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double lz = getLower().getZ();
//        final double cx = getCenterX();
        final double cy = getCenterY();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();
        final double uz = getUpper().getZ();

        return new ID3BoundingBox[]{
            build(lx, ly, lz, ux, cy, uz),
            build(lx, cy, lz, ux, uy, uz)
        };
    }

    /**
     * This method splits the bounding box in half on the z axis.<br>
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
    default ID3BoundingBox[] splitMidZ() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double lz = getLower().getZ();
//        final double cx = getCenterX();
//        final double cy = getCenterY();
        final double cz = getCenterZ();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();
        final double uz = getUpper().getZ();

        return new ID3BoundingBox[]{
            build(lx, ly, cz, ux, uy, lz),
            build(lx, ly, cz, ux, uy, uz)
        };
    }

    @Override
    default boolean leftOf(double median, Axis3D axis) {
        return getLower().leftOf(median, axis) && getUpper().leftOf(median, axis);
    }

    @Override
    default boolean rightOf(double median, Axis3D axis) {
        return getLower().rightOf(median, axis) && getUpper().rightOf(median, axis);
    }

    /**
     *
     * @param lowerpoint Lower point of the new bounding box.
     * @param upperpoint Upper point of the new bounding box.
     * @return New bounding box spanning from the lower point to the upper
     * point.
     */
    public static ID3BoundingBox build(final ID3Point lowerpoint, final ID3Point upperpoint) {
        final ID3Point lower = ID3Point.build(lowerpoint);
        final ID3Point upper = ID3Point.build(upperpoint);
        return new ID3BoundingBox() {
            @Override
            public ID3Point getLower() {
                return lower;
            }

            @Override
            public ID3Point getUpper() {
                return upper;
            }
        };
    }

    /**
     *
     * @param box Box to copy.
     * @return An equal new bounding box.
     */
    public static ID3BoundingBox build(final ID3BoundingBox box) {
        return build(box.getLower(), box.getUpper());
    }

    /**
     *
     * @param lx Lower point x coordinate.
     * @param ly Lower point y coordinate.
     * @param lz Lower point z coordinate.
     * @param ux Upper point x coordinate.
     * @param uy Upper point y coordinate.
     * @param uz Upper point z coordinate.
     * @return Bounding box with IDPoint(lx, ly, lz) as lower point and
     * IDPoint(ux, uy, uz) as upper point.
     */
    public static ID3BoundingBox build(final double lx, final double ly, final double lz, final double ux, final double uy, final double uz) {
        return build(ID3Point.build(lx, ly, lz), ID3Point.build(ux, uy, uz));
    }
}
