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

import com.psygate.datastructures.spatial.trees.recursive.OcNode;
import com.psygate.datastructures.spatial.trees.recursive.OcTree;
import com.psygate.datastructures.util.Pair;
import com.psygate.datastructures.spatial.D3BoundingBox;
import com.psygate.datastructures.spatial.D3Point;
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
import com.psygate.datastructures.spatial.ID3BoundingBox;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class DPointOcTreeTest {

    private final int batchsize = 500;

    public DPointOcTreeTest() {
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
     * Test of getBounds method, of class DPointOcTree.
     */
    @Test
    public void testGetBounds() {
        OcTree<ID3BoundingBox, D3Point> tree = newTree();
        assertTrue(ID3BoundingBox.build(0, 0, 0, 1, 1, 1).same(tree.getBounds()));
        tree = newTree();
//        assertFalse(IDBoundingBox.build(0, 0, 1, 1).same(tree.getBounds()));
        assertFalse(ID3BoundingBox.build(0, 0, 0, 2, 2, 2).same(tree.getBounds()));
    }

    /**
     * Test of envelopes method, of class DPointOcTree.
     */
    @Test
    public void testEnvelopes() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().allMatch((p) -> tree.envelopes(p)));
        final OcTree<ID3BoundingBox, D3Point> tree2 = newTree(4, 4, 4, 5, 5, 5);
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
        assertTrue(toKeys(getPoints(batchsize, tree.getBounds())).stream().noneMatch((p) -> tree2.envelopes(p)));
    }

    /**
     * Test of remove method, of class DPointOcTree.
     */
    @Test
    public void testPutRemove() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();

        List<Pair<ID3BoundingBox, D3Point>> batch1 = getPoints(batchsize, tree.getBounds());
        List<Pair<ID3BoundingBox, D3Point>> batch2 = getPoints(batchsize, tree.getBounds());
        List<Pair<ID3BoundingBox, D3Point>> batch3 = getPoints(batchsize, tree.getBounds());
        List<Pair<ID3BoundingBox, D3Point>> batch4 = getPoints(batchsize, tree.getBounds());
        LinkedList<Pair<ID3BoundingBox, D3Point>> merged = new LinkedList<>();
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

        final OcTree<ID3BoundingBox, D3Point> treecopy = new OcTree<>(tree);

//        treecopy.putAll(merged.stream().map((p) -> new Pair<>(p, invert(p))).collect(Collectors.toList()));
        assertTrue(tree.keyStream().allMatch((k) -> treecopy.containsKey(k)));
        assertTrue(tree.entryStream().allMatch((en) -> treecopy.contains(en.getKey(), en.getValue())));

        assertEquals(batchsize * 4, treecopy.size());
        assertEquals(batchsize * 4, treecopy.valueStream().count());

        assertTrue(merged.stream().allMatch((p) -> tree.containsKey(p.getKey())));

        while (!merged.isEmpty()) {
            Pair<ID3BoundingBox, D3Point> p = merged.pop();
            assertTrue(treecopy.containsKey(p.getKey()));
            Collection<D3Point> rem = treecopy.remove(p.getKey());
            assertEquals(1, rem.size());
            assertTrue(rem.contains(p.getValue()));
            assertEquals(merged.size(), treecopy.size());
        }

        assertTrue(treecopy.isEmpty());

        final OcTree<ID3BoundingBox, D3Point> treecopy2 = new OcTree<>(tree);

//        treecopy2.putAll(merged.stream().map((p) -> new Pair<>(p, invert(p))).collect(Collectors.toList()));
        assertTrue(tree.keyStream().allMatch((k) -> treecopy2.containsKey(k)));
        assertTrue(tree.entryStream().allMatch((en) -> treecopy2.contains(en.getKey(), en.getValue())));

        assertEquals(batchsize * 4, treecopy2.size());
        assertEquals(batchsize * 4, treecopy2.valueStream().count());

        assertTrue(merged.stream().allMatch((p) -> tree.containsKey(p.getKey())));

        while (!merged.isEmpty()) {
            Pair<ID3BoundingBox, D3Point> p = merged.pop();
            assertTrue(treecopy2.containsKey(p.getKey()));
            Collection<D3Point> rem = treecopy2.removeValue(p.getValue());
            assertEquals(1, rem.size());
            assertTrue(rem.contains(p.getValue()));
            assertEquals(merged.size(), treecopy2.size());
        }
    }

    /**
     * Test of keyStream method, of class DPointOcTree.
     */
    @Test
    public void testKeyStream() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();

        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final Set<ID3BoundingBox> keylist = new HashSet<>(toKeys(baselist));
        tree.putAll(baselist);
        assertTrue(tree.keyStream().allMatch((p) -> keylist.contains(p)));
    }

    /**
     * Test of valueStream method, of class DPointOcTree.
     */
    @Test
    public void testValueStream() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final Set<D3Point> values = new HashSet<>(toValues(baselist));
        tree.putAll(baselist);
        assertTrue(tree.valueStream().allMatch((p) -> values.contains(p)));
    }

    /**
     * Test of entryStream method, of class DPointOcTree.
     */
    @Test
    public void testEntryStream() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);
        assertTrue(tree.entryStream().allMatch(baselist::contains));
    }

    /**
     * Test of selectiveKeyStream method, of class DPointOcTree.
     */
    @Test
    public void testSelectiveKeyStream() {
        final ID3BoundingBox bb = new D3BoundingBox(0, 0, 0, 0.25, 0.25, 0.25);
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        baselist.addAll(getPoints(batchsize, bb));
        final Set<ID3BoundingBox> keylist = new HashSet(toKeys(baselist).parallelStream().filter(((p) -> bb.contains(p))).collect(Collectors.toList()));

        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);

        List<ID3BoundingBox> selectedKeys = tree
                .selectiveKeyStream((box) -> box.intersects(bb))
                .filter((p) -> bb.contains(p))
                .collect(Collectors.toList());

        assertTrue(selectedKeys.stream().allMatch((p) -> keylist.contains(p)));
        assertTrue(keylist.containsAll(selectedKeys));
    }

    /**
     * Test of selectiveValueStream method, of class DPointOcTree.
     */
    @Test
    public void testSelectiveValueStream() {
        final ID3BoundingBox bb = new D3BoundingBox(0, 0, 0, 0.25, 0.25, 0.25);
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final List<Pair<ID3BoundingBox, D3Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<ID3BoundingBox> keylist = new HashSet(toKeys(baselist));
        final Set<D3Point> valuelist = new HashSet<>(toValues(baselist));

        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);
        List<D3Point> selectedValues = tree
                .selectiveValueStream((box) -> box.intersects(bb))
                .filter((p) -> bb.contains(p))
                .collect(Collectors.toList());

        assertTrue(selectedValues.stream().allMatch((p) -> valuelist.contains(p)));
        assertTrue(valuelist.containsAll(selectedValues));
    }

    /**
     * Test of selectiveEntryStream method, of class DPointOcTree.
     */
    @Test
    public void testSelectiveEntryStream() {
        final ID3BoundingBox bb = new D3BoundingBox(0, 0, 0, 0.25, 0.25, 0.25);
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final List<Pair<ID3BoundingBox, D3Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<ID3BoundingBox> keylist = new HashSet(toKeys(baselist));
        final Set<Map.Entry<ID3BoundingBox, D3Point>> entrylist = new HashSet<>(baselist);

        assertFalse(keylist.isEmpty());
        tree.putAll(baselist);
        List<Map.Entry<ID3BoundingBox, D3Point>> selectedValues = tree
                .selectiveEntryStream((box) -> box.intersects(bb))
                .filter((p) -> bb.contains(p.getKey()))
                .collect(Collectors.toList());

        assertTrue(selectedValues.stream().allMatch((p) -> entrylist.contains(p)));
        assertTrue(entrylist.containsAll(selectedValues));
    }

    @Test
    public void testNodeIterator() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);
        final Set<OcNode<ID3BoundingBox, D3Point>> visited = new HashSet<>();
        int size = 0;
        for (Iterator<OcNode<ID3BoundingBox, D3Point>> it = tree.nodeIterator(); it.hasNext();) {
            OcNode<ID3BoundingBox, D3Point> node = it.next();
            assertFalse(visited.contains(node));
            visited.add(node);
            size++;
        }

//        assertEquals(keylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of keyIterator method, of class DPointOcTree.
     */
    @Test
    public void testKeyIterator() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final List<Pair<ID3BoundingBox, D3Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<ID3BoundingBox> keylist = new HashSet(toKeys(baselist));

        tree.putAll(baselist);
        int size = 0;
        for (Iterator<ID3BoundingBox> it = tree.keyIterator(); it.hasNext();) {
            assertTrue(keylist.contains(it.next()));
            size++;
        }

        assertEquals(keylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of valueIterator method, of class DPointOcTree.
     */
    @Test
    public void testValueIterator() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final List<Pair<ID3BoundingBox, D3Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<D3Point> valueslist = new HashSet(toValues(baselist));
        tree.putAll(baselist);
        int size = 0;
        for (Iterator<D3Point> it = tree.valueIterator(); it.hasNext();) {
            assertTrue(valueslist.contains(it.next()));
            size++;
        }

        assertEquals(valueslist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of entryIterator method, of class DPointOcTree.
     */
    @Test
    public void testEntryIterator() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final List<Pair<ID3BoundingBox, D3Point>> baselist = getPoints(batchsize, tree.getBounds());
        final Set<Map.Entry<ID3BoundingBox, D3Point>> entrylist = new HashSet(baselist);

        tree.putAll(entrylist);

        int size = 0;
        for (Iterator<Map.Entry<ID3BoundingBox, D3Point>> it = tree.entryIterator(); it.hasNext();) {
            assertTrue(entrylist.contains(it.next()));
            size++;
        }

        assertEquals(entrylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of selectiveKeyIterator method, of class DPointOcTree.
     */
    @Test
    public void testSelectiveKeyIterator() {
        final D3BoundingBox box = new D3BoundingBox(0, 0, 0, 0.25, 0.25, 0.25);
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        baselist.addAll(getPoints(batchsize, box));
        final Set<ID3BoundingBox> keylist = new HashSet(baselist.stream().map((p) -> p.getKey()).filter((p) -> box.contains(p)).collect(Collectors.toList()));

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        int size = 0;
        for (Iterator<ID3BoundingBox> it = tree.selectiveKeyIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            ID3BoundingBox key = it.next();
            if (box.contains(key)) {
                assertTrue(keylist.contains(key));
                size++;
            }
        }

        assertEquals(keylist.size(), size);
        assertTrue(size > 0);
    }

    /**
     * Test of selectiveValueIterator method, of class DPointOcTree.
     */
    @Test
    public void testSelectiveValueIterator() {
        final D3BoundingBox box = new D3BoundingBox(0, 0, 0, 0.25, 0.25, 0.25);
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final List<D3Point> values = toValues(baselist);

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<D3Point> it = tree.selectiveValueIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            D3Point entry = it.next();

            assertTrue(values.contains(entry));
        }
    }

    /**
     * Test of selectiveEntryIterator method, of class DPointOcTree.
     */
    @Test
    public void testSelectiveNodeIterator() {
        final D3BoundingBox box = new D3BoundingBox(0, 0, 0, 0.25, 0.25, 0.25);
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<OcNode<ID3BoundingBox, D3Point>> it = tree.nodeIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            OcNode<ID3BoundingBox, D3Point> entry = it.next();
            assertTrue(entry.getBounds().intersects(box));
            assertTrue(box.intersects(entry.getBounds()));
        }
    }

    /**
     * Test of selectiveEntryIterator method, of class DPointOcTree.
     */
    @Test
    public void testSelectiveEntryIterator() {
        final D3BoundingBox box = new D3BoundingBox(0, 0, 0, 0.25, 0.25, 0.25);
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        final Set<Map.Entry<D3Point, D3Point>> entrylist = new HashSet(baselist.stream().filter((p) -> box.contains(p.getKey())).collect(Collectors.toList()));

        final Set<Map.Entry<ID3BoundingBox, D3Point>> found = new HashSet<>();

        tree.putAll(baselist);
        assertFalse(tree.isEmpty());
        assertEquals(baselist.size(), tree.size());

        for (Iterator<Map.Entry<ID3BoundingBox, D3Point>> it = tree.selectiveEntryIterator((bb) -> box.intersects(bb)); it.hasNext();) {
            Map.Entry<ID3BoundingBox, D3Point> entry = it.next();
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
     * Test of keys method, of class DPointOcTree.
     */
    @Test
    public void testKeys() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);

        assertEquals(baselist.size(), tree.keys().size());
        assertTrue(tree.keys().containsAll(toKeys(baselist)));
        assertTrue(toKeys(baselist).containsAll(tree.keys()));
    }

    /**
     * Test of values method, of class DPointOcTree.
     */
    @Test
    public void testValues() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);

        assertEquals(baselist.size(), tree.values().size());
        assertTrue(tree.values().containsAll(toValues(baselist)));
        assertTrue(toValues(baselist).containsAll(tree.values()));
    }

    /**
     * Test of entries method, of class DPointOcTree.
     */
    @Test
    public void testEntries() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);

        assertEquals(baselist.size(), tree.entries().size());
        assertTrue(tree.entries().containsAll(baselist));
        assertTrue(baselist.containsAll(tree.entries()));

    }

    /**
     * Test of containsKey method, of class DPointOcTree.
     */
    @Test
    public void testContainsKey() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);

        assertTrue(toKeys(baselist).stream().allMatch((key) -> tree.containsKey(key)));
    }

    /**
     * Test of containsValue method, of class DPointOcTree.
     */
    @Test
    public void testContainsValue() {
        final OcTree<ID3BoundingBox, D3Point> tree = newTree();
        final Set<Pair<ID3BoundingBox, D3Point>> baselist = new HashSet(getPoints(batchsize, tree.getBounds()));
        tree.putAll(baselist);

        assertTrue(toValues(baselist).stream().allMatch((key) -> tree.containsValue(key)));
    }

    private final Random rand = new Random(9327490235L);
    private final Set<ID3BoundingBox> points = new HashSet<>();

    List<Pair<ID3BoundingBox, D3Point>> getPoints(final int size, ID3BoundingBox box) {
        ArrayList<Pair<ID3BoundingBox, D3Point>> list = new ArrayList<>(size);
        while (list.size() < size) {
            D3Point p = getPoint(box);

            final double lx = box.getLower().getX() + rand.nextDouble() * box.getWidth();
            final double ly = box.getLower().getY() + rand.nextDouble() * box.getHeight();
            final double lz = box.getLower().getZ() + rand.nextDouble() * box.getDepth();
            final double ux = box.getLower().getX() + rand.nextDouble() * box.getWidth();
            final double uy = box.getLower().getY() + rand.nextDouble() * box.getHeight();
            final double uz = box.getLower().getZ() + rand.nextDouble() * box.getDepth();

            D3BoundingBox bb = new D3BoundingBox(Math.min(lx, ux), Math.min(ly, uy), Math.min(lz, uz), Math.max(lx, ux), Math.max(ly, uy), Math.max(lz, uz));
            if (!points.contains(bb)) {
                list.add(new Pair<>(bb, getPoint(box)));
            }
        }

        points.addAll(list.parallelStream().map((p) -> p.getKey()).collect(Collectors.toList()));
        return Collections.unmodifiableList(list);
    }

    D3Point getPoint(ID3BoundingBox box) {
        final double lx = box.getLower().getX() + rand.nextDouble() * box.getWidth();
        final double ly = box.getLower().getY() + rand.nextDouble() * box.getHeight();
        final double lz = box.getLower().getZ() + rand.nextDouble() * box.getDepth();
        return new D3Point(lx, ly, lz);
    }

    List<ID3BoundingBox> toKeys(final Collection<Pair<ID3BoundingBox, D3Point>> inlist) {
        final ArrayList<ID3BoundingBox> list = new ArrayList<>(inlist.size());

        inlist.stream().forEach((p) -> list.add(p.getKey()));

        return Collections.unmodifiableList(list);
    }

    List<D3Point> toValues(final Collection<Pair<ID3BoundingBox, D3Point>> inlist) {
        final ArrayList<D3Point> list = new ArrayList<>(inlist.size());

        inlist.stream().forEach((p) -> list.add(p.getValue()));

        return Collections.unmodifiableList(list);
    }

    D3Point invert(D3Point p) {
        return new D3Point(-p.getX(), -p.getY(), -p.getZ());
    }

    OcTree<ID3BoundingBox, D3Point> newTree() {
        return newTree(0, 0, 0, 1, 1, 1);
    }

    OcTree<ID3BoundingBox, D3Point> newTree(double x, double y, double z, double xx, double yy, double zz) {
        return new OcTree<>(new D3BoundingBox(x, y, z, xx, yy, zz), 5);
    }

}
