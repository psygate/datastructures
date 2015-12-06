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

import com.psygate.datastructures.spatial.ID3BoundingBox;
import com.psygate.datastructures.spatial.ID3Point;
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
public class ID3BoundingBoxTest {

    public ID3BoundingBoxTest() {
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
     * Test of getCenterX method, of class IDBoundingBox.
     */
    @Test
    public void testGetCenterX() {
        assertEquals(0.5, build(0, 0, 0, 1, 1, 1).getCenterX(), 0);
        assertEquals(1, build(0, 0, 0, 2, 2, 2).getCenterX(), 0);
        assertEquals(0.5, build(0, 0, 0, 1, 1, 2).getCenterX(), 0);
    }

    /**
     * Test of getCenterY method, of class IDBoundingBox.
     */
    @Test
    public void testGetCenterY() {
        assertEquals(0.5, build(0, 0, 0, 1, 1, 1).getCenterY(), 0);
        assertEquals(1, build(0, 0, 0, 1, 2, 2).getCenterY(), 0);
        assertEquals(1, build(0, 0, 0, 2, 2, 2).getCenterY(), 0);
    }

    /**
     * Test of getCenterZ method, of class IDBoundingBox.
     */
    @Test
    public void testGetCenterZ() {
        assertEquals(0.5, build(0, 0, 0, 1, 1, 1).getCenterZ(), 0);
        assertEquals(1, build(0, 0, 0, 1, 2, 2).getCenterZ(), 0);
        assertEquals(1, build(0, 0, 0, 2, 2, 2).getCenterZ(), 0);
    }

    /**
     * Test of getCenter method, of class IDBoundingBox.
     */
    @Test
    public void testGetCenter() {
        assertEquals(0.5, build(0, 0, 0, 1, 1, 1).getCenter().getX(), 0);
        assertEquals(1, build(0, 0, 0, 2, 2, 1).getCenter().getX(), 0);
        assertEquals(0.5, build(0, 0, 0, 1, 1, 1).getCenter().getX(), 0);
        assertEquals(0.5, build(0, 0, 0, 1, 1, 1).getCenter().getY(), 0);
        assertEquals(1, build(0, 0, 0, 1, 2, 2).getCenter().getY(), 0);
        assertEquals(1, build(0, 0, 0, 1, 1, 2).getCenter().getZ(), 0);
        assertEquals(0.5, build(0, 0, 0, 2, 2, 1).getCenter().getZ(), 0);
        assertEquals(2, build(0, 0, 0, 4, 1, 4).getCenter().getZ(), 0);
    }

    /**
     * Test of intersects method, of class IDBoundingBox.
     */
    @Test
    public void testIntersects() {
        assertFalse(build(0, 0, 0, 1, 1, 1).intersects(build(2, 0, 0, 3, 1, 1)));
        assertFalse(build(0, 0, 0, 1, 1, 1).intersects(build(0, 2, 0, 1, 3, 1)));
        assertFalse(build(0, 0, 0, 1, 1, 1).intersects(build(-2, -1, 0, -1, 0, 1)));
        assertFalse(build(0, 0, 0, 1, 1, 1).intersects(build(0, -2, 0, 1, -1, 1)));
    }

    /**
     * Test of contains method, of class IDBoundingBox.
     */
    @Test
    public void testContains_IDBoundingBox() {
        assertTrue(build(0, 0, 0, 1, 1, 1).contains(build(0.5, 0.5, 0.5, 0.75, 0.75, 0.75)));
        assertFalse(build(0, 0, 0, 1, 1, 1).contains(build(2, 0, 3, 3, 1, 4)));
        assertFalse(build(0, 0, 0, 1, 1, 1).contains(build(0, 2, 5, 1, 3, 7)));
        assertFalse(build(0, 0, 0, 1, 1, 1).contains(build(-2, -1, -2, -1, 0, -3)));
        assertFalse(build(0, 0, 0, 1, 1, 1).contains(build(0, -2, 0, 1, -1, 1)));
    }

    /**
     * Test of same method, of class IDBoundingBox.
     */
    @Test
    public void testSame() {
        for (int i = 0; i < 100; i++) {
            assertTrue(build(i, i, i, i + 1, i + 1, i + 1).same(build(i, i, i, i + 1, i + 1, i + 1)));
        }
    }

    /**
     * Test of contains method, of class IDBoundingBox.
     */
    @Test
    public void testContains_IDPoint() {
        ID3BoundingBox box = build(0, 0, 0, 100, 100, 100);
        for (int i = 0; i < 100; i++) {
            assertTrue("Point: " + build(i + 0.5, i + 0.5, i + 0.5), box.contains(build(i + 0.5, i + 0.5, i + 0.5)));
            assertFalse(box.contains(build(100 + i + 0.5, i, i)));
            assertFalse(box.contains(build(0 - i - 0.5, i, i)));
            assertFalse(box.contains(build(i, 100 + i + 0.5, i)));
            assertFalse(box.contains(build(i, 0 - i - 0.5, i)));
            assertFalse(box.contains(build(i, i, 100 + i + 0.5)));
            assertFalse(box.contains(build(i, i, 0 - i - 0.5)));
        }
    }

    /**
     * Test of merge method, of class IDBoundingBox.
     */
    @Test
    public void testMerge() {
        for (int i = 0; i < 100; i++) {
            ArrayList<ID3BoundingBox> boxes = new ArrayList<>();

            ID3BoundingBox box = build(0, 0, 0, i, i, i);
            ID3BoundingBox[] vals = box.splitMidX();
            boxes.addAll(Arrays.stream(vals).flatMap((a) -> Arrays.stream(a.splitMidY())).collect(Collectors.toList()));

            ID3BoundingBox two = build(0, 0, 0, 0, 0, 0);
            for (ID3BoundingBox boxt : boxes) {
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
            ID3BoundingBox box = build(i, i, i, i + 1, i + 1, i + 1);
            ID3BoundingBox[] splitted = box.splitMidX();
            assertEquals(i, splitted[0].getLower().getX(), 0);
            assertEquals(i, splitted[0].getLower().getY(), 0);
            assertEquals(i, splitted[0].getLower().getZ(), 0);
            assertEquals(i + 0.5, splitted[0].getUpper().getX(), 0);
            assertEquals(i + 1, splitted[0].getUpper().getY(), 0);
            assertEquals(i + 1, splitted[0].getUpper().getZ(), 0);

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
            ID3BoundingBox box = build(i, i, i, i + 1, i + 1, i + 1);
            ID3BoundingBox[] splitted = box.splitMidY();
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

    private ID3Point build(double x, double y, double z) {
        return new ID3Point() {
            @Override
            public double getX() {
                return x;
            }

            @Override
            public double getY() {
                return y;
            }

            @Override
            public double getZ() {
                return z;
            }

            @Override
            public String toString() {
                return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
            }

        };
    }

    private ID3BoundingBox build(double lx, double ly, double lz, double ux, double uy, double uz) {
        final ID3Point lower = build(lx, ly, lz);

        final ID3Point upper = build(ux, uy, uz);

        return new ID3BoundingBox() {
            @Override
            public ID3Point getLower() {
                return lower;
            }

            @Override
            public ID3Point getUpper() {
                return upper;
            }

            @Override
            public String toString() {
                return "[" + getLower() + "]-[" + getUpper() + "]";
            }

        };
    }

}
