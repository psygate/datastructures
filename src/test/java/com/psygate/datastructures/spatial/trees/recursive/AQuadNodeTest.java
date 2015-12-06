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
package com.psygate.datastructures.spatial.trees.recursive;

import com.psygate.datastructures.spatial.trees.recursive.QuadNode;
import com.psygate.datastructures.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.psygate.datastructures.spatial.ID2BoundingBox;
import com.psygate.datastructures.spatial.ID2Orderable;
import com.psygate.datastructures.spatial.ID2Point;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class AQuadNodeTest {

    public AQuadNodeTest() {
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
     * Test of split method, of class QuadNode.
     */
    @Test
    public void testNode() {
        QuadNode<ID2Point, Object> node = new QuadNode<>(ID2BoundingBox.build(0, 0, 100, 100), 4);
        assertTrue(node.isEmpty());
        assertEquals(0, node.size());
        assertFalse(node.hasChildren());
        for (int i = 0; i < 4; i++) {
            node.add(new Pair<>(ID2Point.build(i, i), new Object()));
            assertFalse(node.isEmpty());
            assertEquals(i + 1, node.size());
            assertFalse(node.hasChildren());
        }

        node.add(new Pair<>(ID2Point.build(3, 3), new Object()));
        assertTrue(node.isEmpty());
        assertEquals(0, node.size());
        assertTrue(node.hasChildren());
        assertEquals(8, node.subtreeSize());
        node.add(new Pair<>(ID2Point.build(3, 3), new Object()));
        assertTrue(node.isEmpty());
        assertEquals(0, node.size());
        assertTrue(node.hasChildren());
        assertEquals(8, node.subtreeSize());
    }
}
