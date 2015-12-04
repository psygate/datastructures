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
import com.psygate.datastructures.spatial.d2.IDPoint;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Box box = getBox();

        Box next = box;

        long maxcycles = 10000000;

        for (long i = 0; i < maxcycles; i++) {
            final long start = System.nanoTime();
            if (i % 2 == 0) {
                next = next.splitMidX()[0];
            } else {
                next = next.splitMidY()[0];
            }
            if (i % 1000000 == 0) {
                System.out.println("Splittime [Warmup][" + System.currentTimeMillis() + "]: " + (System.nanoTime() - start));
            }
        }

        Box box2 = getBox();

        Box next2 = box2;

        for (long i = 0; i < maxcycles; i++) {
            final long start = System.nanoTime();
            if (i % 2 == 0) {
                next2 = next2.splitMidX()[0];
            } else {
                next2 = next2.splitMidY()[0];
            }
            if (i % 1000000 == 0) {
                System.out.println("Splittime [Actual][" + System.currentTimeMillis() + "]: " + (System.nanoTime() - start));
            }
        }
    }

    private static Box getBox() {
        return new Box(new Point(Double.MIN_VALUE, Double.MIN_VALUE), new Point(Double.MAX_VALUE, Double.MAX_VALUE));
    }

    private final static class Box implements IDBoundingBox {

        private Point lower, upper;

        public Box(Point lower, Point upper) {
            this.lower = lower;
            this.upper = upper;
        }

        private Box(IDBoundingBox box) {
            this.lower = new Point(box.getLower());
            this.upper = new Point(box.getUpper());
        }

        @Override
        public IDPoint getLower() {
            return lower;
        }

        @Override
        public IDPoint getUpper() {
            return upper;
        }

        @Override
        public IDPoint getCenter() {
            return new Point(getCenterX(), getCenterY());
        }

        public void setLower(Point lower) {
            this.lower = lower;
        }

        public void setUpper(Point upper) {
            this.upper = upper;
        }

        @Override
        public Box[] splitMidX() {
            return new Box[]{
                new Box(
                new Point(lower.getX(), lower.getY()),
                new Point(getCenterX(), upper.getY())
                ),
                new Box(
                new Point(getCenterX(), lower.getY()),
                new Point(upper.getX(), upper.getY())
                )
            };
        }

        @Override
        public Box[] splitMidY() {
            return new Box[]{
                new Box(
                new Point(lower.getX(), lower.getY()),
                new Point(upper.getX(), getCenterY())
                ),
                new Box(
                new Point(lower.getX(), getCenterY()),
                new Point(upper.getX(), upper.getY())
                )
            };
        }

    }

    private final static class Point implements IDPoint {

        private final double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        private Point(IDPoint point) {
            this.x = point.getX();
            this.y = point.getY();
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }
    }
}
