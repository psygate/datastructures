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
import com.psygate.datastructures.spatial.ID2BoundingBox;
import com.psygate.datastructures.spatial.ID2Point;

/**
 * Implementation of the IDBoundingBox interface.
 *
 * @see ID2BoundingBox
 * @author psygate (https://github.com/psygate)
 */
public final class D2BoundingBox implements ID2BoundingBox {

    private final D2Point lower, upper, center;

    /**
     *
     * @param lower Lower point of the new bounding box.
     * @param upper Upper point of the new bounding box.
     */
    public D2BoundingBox(D2Point lower, D2Point upper) {
        this.lower = lower;
        this.upper = upper;
        this.center = new D2Point((lower.getX() + upper.getX()) / 2, (lower.getY() + upper.getY()) / 2);
    }

    /**
     *
     * @param lower Lower point of the new bounding box.
     * @param upper Upper point of the new bounding box.
     */
    public D2BoundingBox(ID2Point lower, ID2Point upper) {
        this(new D2Point(lower), new D2Point(upper));
    }

    /**
     *
     * @param box Box to copy so that this bounding box equals box.
     */
    public D2BoundingBox(ID2BoundingBox box) {
        this(new D2Point(box.getLower()), new D2Point(box.getUpper()));
    }

    /**
     *
     * @param lx Lower point x coordinate.
     * @param ly Lower point y coordinate.
     * @param ux Upper point x coordinate.
     * @param uy Upper point y coordinate.
     */
    public D2BoundingBox(final double lx, final double ly, final double ux, final double uy) {
        this(new D2Point(lx, ly), new D2Point(ux, uy));
    }

    @Override
    public D2Point getLower() {
        return lower;
    }

    @Override
    public D2Point getUpper() {
        return upper;
    }

    @Override
    public D2Point getCenter() {
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
    public ID2BoundingBox[] splitMidX() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double cx = getCenterX();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new D2BoundingBox[]{
            new D2BoundingBox(lx, ly, cx, uy),
            new D2BoundingBox(cx, ly, ux, uy)
        };
    }

    @Override
    public D2BoundingBox[] splitMidY() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double cy = getCenterY();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new D2BoundingBox[]{
            new D2BoundingBox(lx, ly, ux, cy),
            new D2BoundingBox(lx, cy, ux, uy)
        };
    }

    @Override
    public D2BoundingBox merge(ID2BoundingBox other) {
        return new D2BoundingBox(Math.min(lower.getX(), other.getLower().getX()),
                Math.min(lower.getY(), other.getLower().getY()),
                Math.max(upper.getX(), other.getUpper().getX()),
                Math.max(upper.getY(), other.getUpper().getY()));
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
        final D2BoundingBox other = (D2BoundingBox) obj;
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
        return "DBoundingBox{" + "lower=" + lower + ", upper=" + upper + '}';
    }

}
