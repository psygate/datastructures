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
public interface IDBoundingBox extends IDBoundingBoxContainable {

    public IDPoint getLower();

    public IDPoint getUpper();

    default double getWidth() {
        assert getUpper().getX() >= getLower().getX();
        return getUpper().getX() - getLower().getX();
    }

    default double getHeight() {
        assert getUpper().getY() >= getLower().getY();
        return getUpper().getY() - getLower().getY();
    }

    default double getCenterX() {
        return (getUpper().getX() + getLower().getX()) / 2;
    }

    default double getCenterY() {
        return (getUpper().getY() + getLower().getY()) / 2;
    }

    default IDPoint getCenter() {
        return IDPoint.build(getCenterX(), getCenterY());
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
//        return !(getLower().getX() > other.getUpper().getX()
//                || getUpper().getX() < other.getLower().getX()
//                || getLower().getY() > other.getUpper().getY()
//                || getUpper().getY() < other.getUpper().getY());
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
    default boolean contains(IDBoundingBoxContainable other) {
        return other.isInside(this);
//        return getLower().getX() <= other.getLower().getX()
//                && getUpper().getX() >= other.getUpper().getX()
//                && getLower().getY() <= other.getLower().getY()
//                && getUpper().getY() >= other.getUpper().getY();
    }

    /**
     * Checks if two bounding boxes represent the same space. They are the same
     * if both lower and upper points are the same.
     *
     * @param other Box to check against.
     * @return True if both boxes are the same.
     */
    default boolean same(IDBoundingBox other) {
        return getLower().same(other.getLower()) && getUpper().same(other.getUpper());
    }
    
    @Override
    default boolean isInside(IDBoundingBox other) {
        return getLower().getX() >= other.getLower().getX()
                && getUpper().getX() <= other.getUpper().getX()
                && getLower().getY() >= other.getLower().getY()
                && getUpper().getY() <= other.getUpper().getY();
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

    public static IDBoundingBox build(final IDPoint loweri, final IDPoint upperi) {
        final IDPoint lower = IDPoint.build(loweri);
        final IDPoint upper = IDPoint.build(upperi);
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

    public static IDBoundingBox build(final IDBoundingBox box) {
        return build(box.getLower(), box.getUpper());
    }

    public static IDBoundingBox build(final double lx, final double ly, final double ux, final double uz) {
        return build(IDPoint.build(lx, ly), IDPoint.build(ux, uz));
    }
}
