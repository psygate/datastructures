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
package com.psygate.datastructures.spatial.d2.trees;

import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import static com.psygate.datastructures.spatial.d2.IDBoundingBox.build;
import com.psygate.datastructures.spatial.d2.IDPoint;
import java.util.Objects;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public final class DBoundingBox implements IDBoundingBox {

    private final DPoint lower, upper, center;

    public DBoundingBox(DPoint lower, DPoint upper) {
        this.lower = lower;
        this.upper = upper;
        this.center = new DPoint((lower.getX() + upper.getX()) / 2, (lower.getY() + upper.getY()) / 2);
    }

    public DBoundingBox(IDPoint lower, IDPoint upper) {
        this(new DPoint(lower), new DPoint(upper));
    }

    public DBoundingBox(IDBoundingBox box) {
        this(new DPoint(box.getLower()), new DPoint(box.getUpper()));
    }

    public DBoundingBox(final double lx, final double ly, final double ux, final double uz) {
        this(new DPoint(lx, ly), new DPoint(ux, uz));
    }

    @Override
    public DPoint getLower() {
        return lower;
    }

    @Override
    public DPoint getUpper() {
        return upper;
    }

    @Override
    public DPoint getCenter() {
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
    public IDBoundingBox[] splitMidX() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
        final double cx = getCenterX();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new DBoundingBox[]{
            new DBoundingBox(lx, ly, cx, uy),
            new DBoundingBox(cx, ly, ux, uy)
        };
    }

    @Override
    public DBoundingBox[] splitMidY() {
        final double lx = getLower().getX();
        final double ly = getLower().getY();
//        final double cx = getCenterX();
        final double cy = getCenterY();
        final double ux = getUpper().getX();
        final double uy = getUpper().getY();

        return new DBoundingBox[]{
            new DBoundingBox(lx, ly, ux, cy),
            new DBoundingBox(lx, cy, ux, uy)
        };
    }

    @Override
    public DBoundingBox merge(IDBoundingBox other) {
        return new DBoundingBox(Math.min(lower.getX(), other.getLower().getX()),
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
        final DBoundingBox other = (DBoundingBox) obj;
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
