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

import java.util.Objects;

/**
 * Implementation of the IDBoundingBox interface.
 *
 * @see ID3BoundingBox
 * @author psygate (https://github.com/psygate)
 */
public final class D3BoundingBox implements ID3BoundingBox {

    private final D3Point lower, upper, center;

    /**
     *
     * @param lower Lower point of the new bounding box.
     * @param upper Upper point of the new bounding box.
     */
    public D3BoundingBox(D3Point lower, D3Point upper) {
        this.lower = lower;
        this.upper = upper;
        this.center = new D3Point((lower.getX() + upper.getX()) / 2, (lower.getY() + upper.getY()) / 2, (lower.getZ() + upper.getZ()) / 2);
    }

    /**
     *
     * @param center Center point of the bounding box.
     * @param radius Radius from the center.
     */
    public D3BoundingBox(ID3Point center, double radius) {
        this.lower = new D3Point(center.getX() - radius, center.getY() - radius, center.getZ() - radius);
        this.upper = new D3Point(center.getX() + radius, center.getY() + radius, center.getZ() + radius);
        this.center = new D3Point((lower.getX() + upper.getX()) / 2, (lower.getY() + upper.getY()) / 2, (lower.getZ() + upper.getZ()) / 2);
    }

    /**
     *
     * @param lower Lower point of the new bounding box.
     * @param upper Upper point of the new bounding box.
     */
    public D3BoundingBox(ID3Point lower, ID3Point upper) {
        this(new D3Point(lower), new D3Point(upper));
    }

    /**
     *
     * @param box Box to copy so that this bounding box equals box.
     */
    public D3BoundingBox(ID3BoundingBox box) {
        this(new D3Point(box.getLower()), new D3Point(box.getUpper()));
    }

    /**
     *
     * @param lx Lower point x coordinate.
     * @param ly Lower point y coordinate.
     * @param ux Upper point x coordinate.
     * @param uy Upper point y coordinate.
     */
    public D3BoundingBox(final double lx, final double ly, final double lz, final double ux, final double uy, final double uz) {
        this(new D3Point(lx, ly, lz), new D3Point(ux, uy, uz));
    }

    @Override
    public D3Point getLower() {
        return lower;
    }

    @Override
    public D3Point getUpper() {
        return upper;
    }

    @Override
    public D3Point getCenter() {
        return center;
    }

    @Override
    public double getCenterX() {
        return center.getX();
    }

    @Override
    public double getCenterY() {
        return center.getY();
    }

    @Override
    public D3BoundingBox[] splitMidX() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double lz = getLower().getZ();
        final double cx = getCenterX();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();
        final double uz = getUpper().getZ();
        return new D3BoundingBox[]{
            new D3BoundingBox(lx, ly, lz, cx, uy, uz),
            new D3BoundingBox(cx, ly, lz, ux, uy, uz)
        };
    }

    @Override
    public D3BoundingBox[] splitMidY() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double lz = getLower().getZ();
//        final double cx = getCenterX();
        final double cy = getCenterY();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();
        final double uz = getUpper().getZ();

        return new D3BoundingBox[]{
            new D3BoundingBox(lx, ly, lz, ux, cy, uz),
            new D3BoundingBox(lx, cy, lz, ux, uy, uz)
        };
    }

    @Override
    public D3BoundingBox[] splitMidZ() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double lz = getLower().getZ();
//        final double cx = getCenterX();
//        final double cy = getCenterY();
        final double cz = getCenterZ();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();
        final double uz = getUpper().getZ();

        return new D3BoundingBox[]{
            new D3BoundingBox(lx, ly, cz, ux, uy, lz),
            new D3BoundingBox(lx, ly, cz, ux, uy, uz)
        };
    }

    @Override
    public D3BoundingBox merge(ID3BoundingBox other) {
        return new D3BoundingBox(Math.min(lower.getX(), other.getLower().getX()),
                Math.min(lower.getY(), other.getLower().getY()),
                Math.min(lower.getZ(), other.getLower().getZ()),
                Math.max(upper.getX(), other.getUpper().getX()),
                Math.max(upper.getY(), other.getUpper().getY()),
                Math.max(upper.getZ(), other.getUpper().getZ()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.lower);
        hash = 61 * hash + Objects.hashCode(this.upper);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final D3BoundingBox other = (D3BoundingBox) obj;
        if (!Objects.equals(this.lower, other.lower)) {
            return false;
        }
        if (!Objects.equals(this.upper, other.upper)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DBoundingBox{" + lower + ", " + upper + '}';
    }

}
