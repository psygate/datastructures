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

import com.psygate.datastructures.spatial.D2BoundingBox;
import com.psygate.datastructures.spatial.ID2Point;
import com.psygate.datastructures.spatial.ID2BoundingBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class D2BoundingBoxTest {

    public D2BoundingBoxTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getLower method, of class IDBoundingBox.
     */
    @Test
    public void testGetLower() {
        ID2BoundingBox box = build(3, 4, 7, 8);
        assertEquals(box.getLower(), box.getLower());
    }

    /**
     * Test of getUpper method, of class IDBoundingBox.
     */
    @Test
    public void testGetUpper() {
        ID2BoundingBox box = build(3, 4, 7, 8);
        assertEquals(box.getUpper(), box.getUpper());
    }

    /**
     * Test of getWidth method, of class IDBoundingBox.
     */
    @Test
    public void testGetWidth() {
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 1, build(0, 1, i + 1, 2).getWidth(), 0);
            assertEquals((i + 1) * 2, build(-(i + 1), 1, i + 1, 2).getWidth(), 0);
        }
    }

    /**
     * Test of getHeight method, of class IDBoundingBox.
     */
    @Test
    public void testGetHeight() {
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 1, build(0, 0, 2, i + 1).getHeight(), 0);
            assertEquals((i + 1) * 2, build(0, -(i + 1), 2, i + 1).getHeight(), 0);
        }
    }

    /**
     * Test of getCenterX method, of class IDBoundingBox.
     */
    @Test
    public void testGetCenterX() {
        assertEquals(0.5, build(0, 0, 1, 1).getCenterX(), 0);
        assertEquals(1, build(0, 0, 2, 2).getCenterX(), 0);
        assertEquals(0.5, build(0, 0, 1, 2).getCenterX(), 0);
    }

    /**
     * Test of getCenterY method, of class IDBoundingBox.
     */
    @Test
    public void testGetCenterY() {
        assertEquals(0.5, build(0, 0, 1, 1).getCenterY(), 0);
        assertEquals(1, build(0, 0, 2, 2).getCenterY(), 0);
        assertEquals(1, build(0, 0, 1, 2).getCenterY(), 0);

    }

    /**
     * Test of getCenter method, of class IDBoundingBox.
     */
    @Test
    public void testGetCenter() {
        assertEquals(0.5, build(0, 0, 1, 1).getCenter().getX(), 0);
        assertEquals(1, build(0, 0, 2, 1).getCenter().getX(), 0);
        assertEquals(0.5, build(0, 0, 1, 1).getCenter().getX(), 0);
        assertEquals(0.5, build(0, 0, 1, 1).getCenter().getY(), 0);
        assertEquals(1, build(0, 0, 2, 2).getCenter().getY(), 0);
        assertEquals(1, build(0, 0, 1, 2).getCenter().getY(), 0);
    }

    /**
     * Test of intersects method, of class IDBoundingBox.
     */
    @Test
    public void testIntersects() {
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(2, 0, 3, 1)), build(0, 0, 1, 1).intersects(build(2, 0, 3, 1)));
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(0, 2, 1, 3)), build(0, 0, 1, 1).intersects(build(0, 2, 1, 3)));
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(-2, 0, 1, 1)), build(0, 0, 1, 1).intersects(build(-2, -1, -1, 0)));
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(0, -2, 1, -1)), build(0, 0, 1, 1).intersects(build(0, -2, 1, -1)));
    }

    /**
     * Test of contains method, of class IDBoundingBox.
     */
    @Test
    public void testContains_IDBoundingBox() {
        assertTrue(build(0, 0, 1, 1).contains(build(0.5, 0.5, 0.75, 0.75)));
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(2, 0, 3, 1)), build(0, 0, 1, 1).contains(build(2, 0, 3, 1)));
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(0, 2, 1, 3)), build(0, 0, 1, 1).contains(build(0, 2, 1, 3)));
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(-2, 0, 1, 1)), build(0, 0, 1, 1).contains(build(-2, -1, -1, 0)));
        assertFalse("Values: " + build(0, 0, 1, 1) + (build(0, -2, 1, -1)), build(0, 0, 1, 1).contains(build(0, -2, 1, -1)));
    }

    /**
     * Test of same method, of class IDBoundingBox.
     */
    @Test
    public void testSame() {
        for (int i = 0; i < 100; i++) {
            assertTrue(build(i, i, i + 1, i + 1).same(build(i, i, i + 1, i + 1)));
        }
    }

    /**
     * Test of contains method, of class IDBoundingBox.
     */
    @Test
    public void testContains_IDPoint() {
        ID2BoundingBox box = build(0, 0, 100, 100);
        for (int i = 0; i < 100; i++) {
            assertTrue("Point: " + build(i + 0.5, i + 0.5), box.contains(build(i + 0.5, i + 0.5)));
            assertFalse(box.contains(build(100 + i + 0.5, i)));
            assertFalse(box.contains(build(0 - i - 0.5, i)));
            assertFalse(box.contains(build(i, 100 + i + 0.5)));
            assertFalse(box.contains(build(i, 0 - i - 0.5)));
        }
    }

    /**
     * Test of merge method, of class IDBoundingBox.
     */
    @Test
    public void testMerge() {
        for (int i = 0; i < 100; i++) {
            ArrayList<ID2BoundingBox> boxes = new ArrayList<>();

            ID2BoundingBox box = build(0, 0, i, i);
            ID2BoundingBox[] vals = box.splitMidX();
            boxes.addAll(Arrays.stream(vals).flatMap((a) -> Arrays.stream(a.splitMidY())).collect(Collectors.toList()));

            ID2BoundingBox two = build(0, 0, 0, 0);
            for (ID2BoundingBox boxt : boxes) {
                two = two.merge(boxt);
            }

            assertTrue(box.same(two));
        }
    }

    /**
     * Test of splitMidX method, of class IDBoundingBox.
     */
    @Test
    public void testSplitMidX() {
        for (int i = 0; i < 100; i++) {
            ID2BoundingBox box = build(i, i, i + 1, i + 1);
            ID2BoundingBox[] splitted = box.splitMidX();
            assertEquals(i, splitted[0].getLower().getX(), 0);
            assertEquals(i, splitted[0].getLower().getY(), 0);
            assertEquals(i + 0.5, splitted[0].getUpper().getX(), 0);
            assertEquals(i + 1, splitted[0].getUpper().getY(), 0);

            assertEquals(i + 0.5, splitted[1].getLower().getX(), 0);
            assertEquals(i, splitted[1].getLower().getY(), 0);
            assertEquals(i + 1, splitted[1].getUpper().getX(), 0);
            assertEquals(i + 1, splitted[1].getUpper().getY(), 0);
        }
    }

    /**
     * Test of splitMidY method, of class IDBoundingBox.
     */
    @Test
    public void testSplitMidY() {
        for (int i = 0; i < 100; i++) {
            ID2BoundingBox box = build(i, i, i + 1, i + 1);
            ID2BoundingBox[] splitted = box.splitMidY();
            assertEquals(i, splitted[0].getLower().getX(), 0);
            assertEquals(i, splitted[0].getLower().getY(), 0);
            assertEquals(i + 1, splitted[0].getUpper().getX(), 0);
            assertEquals(i + 0.5, splitted[0].getUpper().getY(), 0);

            assertEquals(i, splitted[1].getLower().getX(), 0);
            assertEquals(i + 0.5, splitted[1].getLower().getY(), 0);
            assertEquals(i + 1, splitted[1].getUpper().getX(), 0);
            assertEquals(i + 1, splitted[1].getUpper().getY(), 0);
        }
    }

    private ID2Point build(double x, double y) {
        return new ID2Point() {
            @Override
            public double getX() {
                return x;
            }

            @Override
            public double getY() {
                return y;
            }

            @Override
            public String toString() {
                return "(" + getX() + ", " + getY() + ")";
            }

        };
    }

    private ID2BoundingBox build(double lx, double ly, double ux, double uy) {
        return new D2BoundingBox(lx, ly, ux, uy);
    }

}
