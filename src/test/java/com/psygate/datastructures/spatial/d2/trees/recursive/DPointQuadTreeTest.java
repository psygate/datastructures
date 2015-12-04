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
package com.psygate.datastructures.spatial.d2.trees.recursive;

import com.psygate.datastructures.maps.Pair;
import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import com.psygate.datastructures.spatial.d2.trees.DBoundingBox;
import com.psygate.datastructures.spatial.d2.trees.DPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
public class DPointQuadTreeTest {

    private final int batchsize = 500;

    public DPointQuadTreeTest() {
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
     * Test of getBounds method, of class DPointQuadTree.
     */
    @Test
    public void testGetBounds() {
        DPointQuadTree<DPoint, DPoint> tree = newTree();
        assertTrue(IDBoundingBox.build(0, 0, 1, 1).same(tree.getBounds()));
        tree = new DPointQuadTree<>(25, new DBoundingBox(0, 0, 2, 2));
        assertFalse(IDBoundingBox.build(0, 0, 1, 1).same(tree.getBounds()));
        assertTrue(IDBoundingBox.build(0, 0, 2, 2).same(tree.getBounds()));
    }

    /**
     * Test of envelopes method, of class DPointQuadTree.
     */
    @Test
    public void testEnvelopes() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        assertTrue(toKeys(getPoints(batchsize)).stream().allMatch((p) -> tree.envelopes(p)));
        final DPointQuadTree<DPoint, DPoint> tree2 = newTree();
        assertTrue(toKeys(getPoints(batchsize)).stream().map((p) -> new DPoint(p.getX() - 1.5, p.getY())).noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize)).stream().map((p) -> new DPoint(p.getX() + 1.5, p.getY())).noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize)).stream().map((p) -> new DPoint(p.getX(), p.getY() - 1.5)).noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize)).stream().map((p) -> new DPoint(p.getX(), p.getY() + 1.5)).noneMatch((p) -> tree2.envelopes(p)));
    }

    /**
     * Test of remove method, of class DPointQuadTree.
     */
    @Test
    public void testPutRemove() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();

        List<Pair<DPoint, DPoint>> batch1 = getPoints(batchsize);
        List<Pair<DPoint, DPoint>> batch2 = getPoints(batchsize);
        List<Pair<DPoint, DPoint>> batch3 = getPoints(batchsize);
        List<Pair<DPoint, DPoint>> batch4 = getPoints(batchsize);
        LinkedList<Pair<DPoint, DPoint>> merged = new LinkedList<>();
        Stream.concat(batch1.stream(), Stream.concat(batch2.stream(), Stream.concat(batch3.stream(), batch4.stream())))
                .forEach((p) -> merged.add(p));

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());

        tree.putAll(batch1);
        assertEquals(batchsize, tree.size());
        assertEquals(batchsize, tree.valueStream().count());

        tree.putAll(batch2);
        assertEquals(batchsize * 2, tree.size());
        assertEquals(batchsize * 2, tree.valueStream().count());

        tree.putAll(batch3);
        assertEquals(batchsize * 3, tree.size());
        assertEquals(batchsize * 3, tree.valueStream().count());

        tree.putAll(batch4);
        assertEquals(batchsize * 4, tree.size());
        assertEquals(batchsize * 4, tree.valueStream().count());

        assertTrue(merged.stream().allMatch((p) -> tree.containsKey(p.getKey())));
        assertTrue(merged.stream().allMatch((p) -> tree.containsValue(p.getValue())));

        final DPointQuadTree<DPoint, DPoint> treecopy = new DPointQuadTree<>(tree);

//        treecopy.putAll(merged.stream().map((p) -> new Pair<>(p, invert(p))).collect(Collectors.toList()));
        assertTrue(tree.keyStream().allMatch((k) -> treecopy.containsKey(k)));
        assertTrue(tree.entryStream().allMatch((en) -> treecopy.contains(en.getKey(), en.getValue())));

        assertEquals(batchsize * 4, treecopy.size());
        assertEquals(batchsize * 4, treecopy.valueStream().count());

        assertTrue(merged.stream().allMatch((p) -> tree.containsKey(p.getKey())));

        while (!merged.isEmpty()) {
            Pair<DPoint, DPoint> p = merged.pop();
            assertTrue(treecopy.containsKey(p.getKey()));
            Collection<DPoint> rem = treecopy.remove(p.getKey());
            assertEquals(1, rem.size());
            assertTrue(rem.contains(p.getValue()));
            assertEquals(merged.size(), treecopy.size());
        }

        assertTrue(treecopy.isEmpty());

        final DPointQuadTree<DPoint, DPoint> treecopy2 = new DPointQuadTree<>(tree);

//        treecopy2.putAll(merged.stream().map((p) -> new Pair<>(p, invert(p))).collect(Collectors.toList()));
        assertTrue(tree.keyStream().allMatch((k) -> treecopy2.containsKey(k)));
        assertTrue(tree.entryStream().allMatch((en) -> treecopy2.contains(en.getKey(), en.getValue())));

        assertEquals(batchsize * 4, treecopy2.size());
        assertEquals(batchsize * 4, treecopy2.valueStream().count());

        assertTrue(merged.stream().allMatch((p) -> tree.containsKey(p.getKey())));

        while (!merged.isEmpty()) {
            Pair<DPoint, DPoint> p = merged.pop();
            assertTrue(treecopy2.containsKey(p.getKey()));
            Collection<DPoint> rem = treecopy2.removeValue(p.getValue());
            assertEquals(1, rem.size());
            assertTrue(rem.contains(p.getValue()));
            assertEquals(merged.size(), treecopy2.size());
        }
    }

    /**
     * Test of keyStream method, of class DPointQuadTree.
     */
    @Test
    public void testKeyStream() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();

        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        final Set<DPoint> keylist = new HashSet<>(toKeys(baselist));
        tree.putAll(baselist);
        assertTrue(tree.keyStream().allMatch((p) -> keylist.contains(p)));
    }

    /**
     * Test of valueStream method, of class DPointQuadTree.
     */
    @Test
    public void testValueStream() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        final Set<DPoint> values = new HashSet<>(toValues(baselist));
        tree.putAll(baselist);
        assertTrue(tree.valueStream().allMatch((p) -> values.contains(p)));
    }

    /**
     * Test of entryStream method, of class DPointQuadTree.
     */
    @Test
    public void testEntryStream() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        tree.putAll(baselist);
        assertTrue(tree.entryStream().allMatch(baselist::contains));
    }

    /**
     * Test of selectiveKeyStream method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveKeyStream() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final List<DPoint> baselist = toKeys(getPoints(batchsize));
        final Set<DPoint> keylist = new HashSet(baselist.parallelStream().filter(((p) -> new DBoundingBox(0, 0, 0.25, 0.25).contains(p))).collect(Collectors.toList()));
        assertFalse(keylist.isEmpty());
        tree.putAll(baselist.stream().map((p) -> new Pair<DPoint, DPoint>(p, invert(p))).collect(Collectors.toList()));
        List<DPoint> selectedKeys = tree
                .selectiveKeyStream((box) -> box.intersects(new DBoundingBox(0, 0, 0.25, 0.25)))
                .filter((p) -> new DBoundingBox(0, 0, 0.25, 0.25).contains(p))
                .collect(Collectors.toList());

        assertTrue(selectedKeys.stream().allMatch((p) -> keylist.contains(p)));
        assertTrue(keylist.containsAll(selectedKeys));
    }

    /**
     * Test of selectiveValueStream method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveValueStream() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final List<Pair<DPoint, DPoint>> baselist = getPoints(batchsize);
        final Set<DPoint> keylist = new HashSet(toKeys(baselist));
        final Set<DPoint> valuelist = new HashSet<>(toValues(baselist));

        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);
        List<DPoint> selectedValues = tree
                .selectiveValueStream((box) -> box.intersects(new DBoundingBox(0, 0, 0.25, 0.25)))
                .filter((p) -> new DBoundingBox(0, 0, 0.25, 0.25).contains(p))
                .collect(Collectors.toList());

        assertTrue(selectedValues.stream().allMatch((p) -> valuelist.contains(p)));
        assertTrue(valuelist.containsAll(selectedValues));
    }

    /**
     * Test of selectiveEntryStream method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveEntryStream() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final List<Pair<DPoint, DPoint>> baselist = getPoints(batchsize);
        final Set<DPoint> keylist = new HashSet(toKeys(baselist));
        final Set<Map.Entry<DPoint, DPoint>> entrylist = new HashSet<>(baselist);

        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);
        List<Map.Entry<DPoint, DPoint>> selectedValues = tree
                .selectiveEntryStream((box) -> box.intersects(new DBoundingBox(0, 0, 0.25, 0.25)))
                .filter((p) -> new DBoundingBox(0, 0, 0.25, 0.25).contains(p.getKey()))
                .collect(Collectors.toList());

        assertTrue(selectedValues.stream().allMatch((p) -> entrylist.contains(p)));
        assertTrue(entrylist.containsAll(selectedValues));
    }

    @Test
    public void testNodeIterator() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        tree.putAll(baselist);
        final Set<QuadNode<DPoint, DPoint>> visited = new HashSet<>();
        int size = 0;
        for (Iterator<QuadNode<DPoint, DPoint>> it = tree.nodeIterator(); it.hasNext();) {
            QuadNode<DPoint, DPoint> node = it.next();
            assertFalse(visited.contains(node));
            visited.add(node);
            size++;
        }

//        assertEquals(keylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of keyIterator method, of class DPointQuadTree.
     */
    @Test
    public void testKeyIterator() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final List<Pair<DPoint, DPoint>> baselist = getPoints(batchsize);
        final Set<DPoint> keylist = new HashSet(toKeys(baselist));

        tree.putAll(baselist);
        int size = 0;
        for (Iterator<DPoint> it = tree.keyIterator(); it.hasNext();) {
            assertTrue(keylist.contains(it.next()));
            size++;
        }

        assertEquals(keylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of valueIterator method, of class DPointQuadTree.
     */
    @Test
    public void testValueIterator() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final List<Pair<DPoint, DPoint>> baselist = getPoints(batchsize);
        final Set<DPoint> valueslist = new HashSet(toValues(baselist));
        tree.putAll(baselist);
        int size = 0;
        for (Iterator<DPoint> it = tree.valueIterator(); it.hasNext();) {
            assertTrue(valueslist.contains(it.next()));
            size++;
        }

        assertEquals(valueslist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of entryIterator method, of class DPointQuadTree.
     */
    @Test
    public void testEntryIterator() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final List<Pair<DPoint, DPoint>> baselist = getPoints(batchsize);
        final Set<Map.Entry<DPoint, DPoint>> entrylist = new HashSet(baselist);

        tree.putAll(entrylist);

        int size = 0;
        for (Iterator<Map.Entry<DPoint, DPoint>> it = tree.entryIterator(); it.hasNext();) {
            assertTrue(entrylist.contains(it.next()));
            size++;
        }

        assertEquals(entrylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of selectiveKeyIterator method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveKeyIterator() {
        final DBoundingBox box = new DBoundingBox(0, 0, 0.25, 0.25);
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        final Set<DPoint> keylist = new HashSet(baselist.stream().map((p) -> p.getKey()).filter((p) -> box.contains(p)).collect(Collectors.toList()));

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        int size = 0;
        for (Iterator<DPoint> it = tree.selectiveKeyIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            DPoint key = it.next();
            if (box.contains(key)) {
                assertTrue(keylist.contains(key));
                size++;
            }
        }

        assertEquals(keylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of selectiveValueIterator method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveValueIterator() {
        final DBoundingBox box = new DBoundingBox(0, 0, 0.25, 0.25);
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        final List<DPoint> values = toValues(baselist);

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<DPoint> it = tree.selectiveValueIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            DPoint entry = it.next();

            assertTrue(values.contains(entry));
        }
    }

    /**
     * Test of selectiveEntryIterator method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveNodeIterator() {
        final DBoundingBox box = new DBoundingBox(0, 0, 0.25, 0.25);
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<QuadNode<DPoint, DPoint>> it = tree.nodeIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            QuadNode<DPoint, DPoint> entry = it.next();
            assertTrue(entry.getBounds().intersects(box));
            assertTrue(box.intersects(entry.getBounds()));
        }
    }

    /**
     * Test of selectiveEntryIterator method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveEntryIterator() {
        final DBoundingBox box = new DBoundingBox(0, 0, 0.25, 0.25);
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        final Set<Map.Entry<DPoint, DPoint>> entrylist = new HashSet(baselist.stream().filter((p) -> box.contains(p.getKey())).collect(Collectors.toList()));

        final Set<Map.Entry<DPoint, DPoint>> found = new HashSet<>();

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<Map.Entry<DPoint, DPoint>> it = tree.selectiveEntryIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            Map.Entry<DPoint, DPoint> entry = it.next();
            if (box.contains(entry.getKey())) {
                assertTrue(entrylist.contains(entry));
                found.add(entry);
            }
        }

        assertTrue(found.containsAll(entrylist));
        assertTrue(entrylist.containsAll(found));

        assertEquals(entrylist.size(), found.size());
    }

    /**
     * Test of keys method, of class DPointQuadTree.
     */
    @Test
    public void testKeys() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        tree.putAll(baselist);

        assertEquals(baselist.size(), tree.keys().size());
        assertTrue(tree.keys().containsAll(toKeys(baselist)));
        assertTrue(toKeys(baselist).containsAll(tree.keys()));
    }

    /**
     * Test of values method, of class DPointQuadTree.
     */
    @Test
    public void testValues() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        tree.putAll(baselist);

        assertEquals(baselist.size(), tree.values().size());
        assertTrue(tree.values().containsAll(toValues(baselist)));
        assertTrue(toValues(baselist).containsAll(tree.values()));
    }

    /**
     * Test of entries method, of class DPointQuadTree.
     */
    @Test
    public void testEntries() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        tree.putAll(baselist);

        assertEquals(baselist.size(), tree.entries().size());
        assertTrue(tree.entries().containsAll(baselist));
        assertTrue(baselist.containsAll(tree.entries()));

    }

    /**
     * Test of containsKey method, of class DPointQuadTree.
     */
    @Test
    public void testContainsKey() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        tree.putAll(baselist);

        assertTrue(toKeys(baselist).stream().allMatch((key) -> tree.containsKey(key)));
    }

    /**
     * Test of containsValue method, of class DPointQuadTree.
     */
    @Test
    public void testContainsValue() {
        final DPointQuadTree<DPoint, DPoint> tree = newTree();
        final Set<Pair<DPoint, DPoint>> baselist = new HashSet(getPoints(batchsize));
        tree.putAll(baselist);

        assertTrue(toValues(baselist).stream().allMatch((key) -> tree.containsValue(key)));
    }

    private final Random rand = new Random(9327490235L);
    private final Set<DPoint> points = new HashSet<>();

    List<Pair<DPoint, DPoint>> getPoints(final int size) {
        ArrayList<Pair<DPoint, DPoint>> list = new ArrayList<>(size);
        while (list.size() < size) {
            DPoint p = getPoint();
            if (!points.contains(p)) {
                list.add(new Pair<>(p, getPoint()));
            }
        }

        points.addAll(list.parallelStream().map((p) -> p.getKey()).collect(Collectors.toList()));
        return Collections.unmodifiableList(list);
    }

    DPoint getPoint() {
        return new DPoint(rand.nextDouble(), rand.nextDouble());
    }

    List<DPoint> toKeys(final Collection<Pair<DPoint, DPoint>> inlist) {
        final ArrayList<DPoint> list = new ArrayList<>(inlist.size());

        inlist.stream().forEach((p) -> list.add(p.getKey()));

        return Collections.unmodifiableList(list);
    }

    List<DPoint> toValues(final Collection<Pair<DPoint, DPoint>> inlist) {
        final ArrayList<DPoint> list = new ArrayList<>(inlist.size());

        inlist.stream().forEach((p) -> list.add(p.getValue()));

        return Collections.unmodifiableList(list);
    }

    DPoint invert(DPoint p) {
        return new DPoint(-p.getX(), -p.getY());
    }

    DPointQuadTree<DPoint, DPoint> newTree() {
        return new DPointQuadTree<>(5, new DBoundingBox(0, 0, 1, 1));
    }

}
