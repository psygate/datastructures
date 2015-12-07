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
import com.psygate.datastructures.spatial.trees.recursive.QuadTree;
import com.psygate.datastructures.util.Pair;
import com.psygate.datastructures.spatial.D2BoundingBox;
import com.psygate.datastructures.spatial.D2Point;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.psygate.datastructures.spatial.ID2BoundingBox;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class DBBQuadTreeTest {

    private final int batchsize = 500;

    public DBBQuadTreeTest() {
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
        QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        assertTrue(ID2BoundingBox.build(0, 0, 1, 1).same(tree.getBounds()));
        tree = newTree();
//        assertFalse(IDBoundingBox.build(0, 0, 1, 1).same(tree.getBounds()));
        assertFalse(ID2BoundingBox.build(0, 0, 2, 2).same(tree.getBounds()));
    }

    /**
     * Test of envelopes method, of class DPointQuadTree.
     */
    @Test
    public void testEnvelopes() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().allMatch((p) -> tree.envelopes(p)));
        final QuadTree<ID2BoundingBox, D2Point> tree2 = newTree(4, 4, 5, 5);
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
    }

    /**
     * Test of remove method, of class DPointQuadTree.
     */
    @Test
    public void testPutRemove() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();

        List<Pair<ID2BoundingBox, D2Point>> batch1 = getPoints(batchsize, tree.getBounds());
        List<Pair<ID2BoundingBox, D2Point>> batch2 = getPoints(batchsize, tree.getBounds());
        List<Pair<ID2BoundingBox, D2Point>> batch3 = getPoints(batchsize, tree.getBounds());
        List<Pair<ID2BoundingBox, D2Point>> batch4 = getPoints(batchsize, tree.getBounds());
        LinkedList<Pair<ID2BoundingBox, D2Point>> merged = new LinkedList<>();
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

        final QuadTree<ID2BoundingBox, D2Point> treecopy = new QuadTree<>(tree);

        assertTrue(tree.keyStream().allMatch((k) -> treecopy.containsKey(k)));
        assertTrue(tree.entryStream().allMatch((en) -> treecopy.contains(en.getKey(), en.getValue())));

        assertEquals(batchsize * 4, treecopy.size());
        assertEquals(batchsize * 4, treecopy.valueStream().count());

        assertTrue(merged.stream().allMatch((p) -> tree.containsKey(p.getKey())));

        while (!merged.isEmpty()) {
            Pair<ID2BoundingBox, D2Point> p = merged.pop();
            assertTrue(treecopy.containsKey(p.getKey()));
            Collection<D2Point> rem = treecopy.remove(p.getKey());
            assertEquals(1, rem.size());
            assertTrue(rem.contains(p.getValue()));
            assertEquals(merged.size(), treecopy.size());
        }

        assertTrue(treecopy.isEmpty());

        final QuadTree<ID2BoundingBox, D2Point> treecopy2 = new QuadTree<>(tree);

//        treecopy2.putAll(merged.stream().map((p) -> new Pair<>(p, invert(p))).collect(Collectors.toList()));
        assertTrue(tree.keyStream().allMatch((k) -> treecopy2.containsKey(k)));
        assertTrue(tree.entryStream().allMatch((en) -> treecopy2.contains(en.getKey(), en.getValue())));

        assertEquals(batchsize * 4, treecopy2.size());
        assertEquals(batchsize * 4, treecopy2.valueStream().count());

        assertTrue(merged.stream().allMatch((p) -> tree.containsKey(p.getKey())));

        while (!merged.isEmpty()) {
            Pair<ID2BoundingBox, D2Point> p = merged.pop();
            assertTrue(treecopy2.containsKey(p.getKey()));
            Collection<D2Point> rem = treecopy2.removeValue(p.getValue());
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();

        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final Set<ID2BoundingBox> keylist = new HashSet<>(toKeys(baselist));
        tree.putAll(baselist);
        assertTrue(tree.keyStream().allMatch((p) -> keylist.contains(p)));
    }

    /**
     * Test of valueStream method, of class DPointQuadTree.
     */
    @Test
    public void testValueStream() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final Set<D2Point> values = new HashSet<>(toValues(baselist));
        tree.putAll(baselist);
        assertTrue(tree.valueStream().allMatch((p) -> values.contains(p)));
    }

    /**
     * Test of entryStream method, of class DPointQuadTree.
     */
    @Test
    public void testEntryStream() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);
        assertTrue(tree.entryStream().allMatch(baselist::contains));
    }

    /**
     * Test of selectiveKeyStream method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveKeyStream() {
        final ID2BoundingBox bb = new D2BoundingBox(0, 0, 0.25, 0.25);
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final List<Pair<ID2BoundingBox, D2Point>> baselist = new LinkedList(getPoints(batchsize, tree.getBounds()));
        baselist.addAll(getPoints(batchsize, bb));
        final Set<ID2BoundingBox> keylist = new HashSet(toKeys(baselist).parallelStream().filter(((p) -> bb.contains(p))).collect(Collectors.toList()));
        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);

        List<ID2BoundingBox> selectedKeys = tree
                .selectiveKeyStream((box) -> box.intersects(bb))
                .filter((p) -> new D2BoundingBox(0, 0, 0.25, 0.25).contains(p))
                .collect(Collectors.toList());

        assertTrue(selectedKeys.stream().allMatch((p) -> keylist.contains(p)));
        assertTrue(keylist.containsAll(selectedKeys));
    }

    /**
     * Test of selectiveValueStream method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveValueStream() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final List<Pair<ID2BoundingBox, D2Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<ID2BoundingBox> keylist = new HashSet(toKeys(baselist));
        final Set<D2Point> valuelist = new HashSet<>(toValues(baselist));

        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);
        List<D2Point> selectedValues = tree
                .selectiveValueStream((box) -> box.intersects(new D2BoundingBox(0, 0, 0.25, 0.25)))
                .filter((p) -> new D2BoundingBox(0, 0, 0.25, 0.25).contains(p))
                .collect(Collectors.toList());

        assertTrue(selectedValues.stream().allMatch((p) -> valuelist.contains(p)));
        assertTrue(valuelist.containsAll(selectedValues));
    }

    /**
     * Test of selectiveEntryStream method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveEntryStream() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final List<Pair<ID2BoundingBox, D2Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<ID2BoundingBox> keylist = new HashSet(toKeys(baselist));
        final Set<Map.Entry<ID2BoundingBox, D2Point>> entrylist = new HashSet<>(baselist);

        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);
        List<Map.Entry<ID2BoundingBox, D2Point>> selectedValues = tree
                .selectiveEntryStream((box) -> box.intersects(new D2BoundingBox(0, 0, 0.25, 0.25)))
                .filter((p) -> new D2BoundingBox(0, 0, 0.25, 0.25).contains(p.getKey()))
                .collect(Collectors.toList());

        assertTrue(selectedValues.stream().allMatch((p) -> entrylist.contains(p)));
        assertTrue(entrylist.containsAll(selectedValues));
    }

    @Test
    public void testNodeIterator() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);
        final Set<QuadNode<ID2BoundingBox, D2Point>> visited = new HashSet<>();
        int size = 0;
        for (Iterator<QuadNode<ID2BoundingBox, D2Point>> it = tree.nodeIterator(); it.hasNext();) {
            QuadNode<ID2BoundingBox, D2Point> node = it.next();
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final List<Pair<ID2BoundingBox, D2Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<ID2BoundingBox> keylist = new HashSet(toKeys(baselist));

        tree.putAll(baselist);
        int size = 0;
        for (Iterator<ID2BoundingBox> it = tree.keyIterator(); it.hasNext();) {
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final List<Pair<ID2BoundingBox, D2Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<D2Point> valueslist = new HashSet(toValues(baselist));
        tree.putAll(baselist);
        int size = 0;
        for (Iterator<D2Point> it = tree.valueIterator(); it.hasNext();) {
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final List<Pair<ID2BoundingBox, D2Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<Map.Entry<ID2BoundingBox, D2Point>> entrylist = new HashSet(baselist);

        tree.putAll(entrylist);

        int size = 0;
        for (Iterator<Map.Entry<ID2BoundingBox, D2Point>> it = tree.entryIterator(); it.hasNext();) {
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
        final D2BoundingBox box = new D2BoundingBox(0, 0, 0.25, 0.25);
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        baselist.addAll(getPoints(batchsize, box));

        final Set<ID2BoundingBox> keylist = new HashSet(baselist.stream().map((p) -> p.getKey()).filter((p) -> box.contains(p)).collect(Collectors.toList()));
        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        int size = 0;
        for (Iterator<ID2BoundingBox> it = tree.selectiveKeyIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            ID2BoundingBox key = it.next();
            if (box.contains(key)) {
                assertTrue(keylist.contains(key));
                size++;
            }
        }
        assertTrue(size > 0);
        assertEquals("keys found: " + size + " of " + keylist.size(), keylist.size(), size);
    }

    /**
     * Test of selectiveValueIterator method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveValueIterator() {
        final D2BoundingBox box = new D2BoundingBox(0, 0, 0.25, 0.25);
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final List<D2Point> values = toValues(baselist);

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<D2Point> it = tree.selectiveValueIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            D2Point entry = it.next();

            assertTrue(values.contains(entry));
        }
    }

    /**
     * Test of selectiveEntryIterator method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveNodeIterator() {
        final D2BoundingBox box = new D2BoundingBox(0, 0, 0.25, 0.25);
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<QuadNode<ID2BoundingBox, D2Point>> it = tree.nodeIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            QuadNode<ID2BoundingBox, D2Point> entry = it.next();
            assertTrue(entry.getBounds().intersects(box));
            assertTrue(box.intersects(entry.getBounds()));
        }
    }

    /**
     * Test of selectiveEntryIterator method, of class DPointQuadTree.
     */
    @Test
    public void testSelectiveEntryIterator() {
        final D2BoundingBox box = new D2BoundingBox(0, 0, 0.25, 0.25);
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final Set<Map.Entry<D2Point, D2Point>> entrylist = new HashSet(baselist.stream().filter((p) -> box.contains(p.getKey())).collect(Collectors.toList()));

        final Set<Map.Entry<ID2BoundingBox, D2Point>> found = new HashSet<>();

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<Map.Entry<ID2BoundingBox, D2Point>> it = tree.selectiveEntryIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            Map.Entry<ID2BoundingBox, D2Point> entry = it.next();
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
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
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);

        assertTrue(toKeys(baselist).stream().allMatch((key) -> tree.containsKey(key)));
    }

    /**
     * Test of containsValue method, of class DPointQuadTree.
     */
    @Test
    public void testContainsValue() {
        final QuadTree<ID2BoundingBox, D2Point> tree = newTree();
        final Set<Pair<ID2BoundingBox, D2Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);

        assertTrue(toValues(baselist).stream().allMatch((key) -> tree.containsValue(key)));
    }

    private final Random rand = new Random(9327490235L);
    private final Set<ID2BoundingBox> boxes = new HashSet<>();

    List<Pair<ID2BoundingBox, D2Point>> getPoints(final int size, ID2BoundingBox box) {
        ArrayList<Pair<ID2BoundingBox, D2Point>> list = new ArrayList<>(size);
        while (list.size() < size) {
//            D2Point p = getPoint();

            double lx = box.getLower().getX() + rand.nextDouble() * box.getWidth();
            double ly = box.getLower().getY() + rand.nextDouble() * box.getHeight();
            double ux = box.getLower().getX() + rand.nextDouble() * box.getWidth();
            double uy = box.getLower().getY() + rand.nextDouble() * box.getHeight();

            D2BoundingBox bb = new D2BoundingBox(Math.min(lx, ux), Math.min(ly, uy), Math.max(lx, ux), Math.max(ly, uy));
            if (!boxes.contains(bb)) {
                list.add(new Pair<>(bb, getPoint(box)));
            }
        }

        boxes.addAll(list.parallelStream().map((p) -> p.getKey()).collect(Collectors.toList()));
        return Collections.unmodifiableList(list);
    }

    D2Point getPoint(ID2BoundingBox box) {
        double lx = box.getLower().getX() + rand.nextDouble() * box.getWidth();
        double ly = box.getLower().getY() + rand.nextDouble() * box.getHeight();
        return new D2Point(lx, ly);
    }

    List<ID2BoundingBox> toKeys(final Collection<Pair<ID2BoundingBox, D2Point>> inlist) {
        final ArrayList<ID2BoundingBox> list = new ArrayList<>(inlist.size());

        inlist.stream().forEach((p) -> list.add(p.getKey()));

        return Collections.unmodifiableList(list);
    }

    List<D2Point> toValues(final Collection<Pair<ID2BoundingBox, D2Point>> inlist) {
        final ArrayList<D2Point> list = new ArrayList<>(inlist.size());

        inlist.stream().forEach((p) -> list.add(p.getValue()));

        return Collections.unmodifiableList(list);
    }

    D2Point invert(D2Point p) {
        return new D2Point(-p.getX(), -p.getY());
    }

    QuadTree<ID2BoundingBox, D2Point> newTree() {
        return newTree(0, 0, 1, 1);
    }

    QuadTree<ID2BoundingBox, D2Point> newTree(double x, double y, double xx, double yy) {
        return new QuadTree<>(new D2BoundingBox(x, y, xx, yy), 5);
    }

}
