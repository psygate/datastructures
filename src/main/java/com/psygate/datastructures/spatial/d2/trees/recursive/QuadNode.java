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
import com.psygate.datastructures.spatial.d2.IDBoundingBoxContainable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default implementation of a simple quad tree node.
 *
 * @see ImmutableQuadTree
 * @see QuadTree
 * @author psygate (https://github.com/psygate)
 */
class QuadNode<K extends IDBoundingBoxContainable, V> {

    private final int maxNodeSize;
    private final IDBoundingBox box;
    private final IDBoundingBox[] subboxes;
    private final List<Pair<K, V>> values;
    private final ArrayList<QuadNode<K, V>> children = new ArrayList<>(4);
    private boolean isForked = false;

    QuadNode(IDBoundingBox box, int maxNodeSize) {
        this.box = box;
        values = new ArrayList<>(maxNodeSize);
        subboxes = Arrays.stream(box.splitMidX())
                .flatMap((b) -> Arrays.stream(b.splitMidY()))
                .collect(Collectors.toList()).toArray(new IDBoundingBox[4]);;
        this.maxNodeSize = maxNodeSize;
        children.addAll(Arrays.asList(new QuadNode[]{null, null, null, null}));
    }

    QuadNode(IDBoundingBox box, int maxNodeSize, Collection<Pair<K, V>> values) {
        this(box, maxNodeSize);
        values.stream().forEach((pair) -> put(pair));
    }

    /**
     * Size of the quadnode value list.
     *
     * @return Size of the quadnode value list.
     */
    int size() {
        return values.size();
    }

    /**
     * Size of the subtree value list, including all child nodes and their child
     * nodes.
     *
     * @return
     */
    int subtreeValueCount() {
        return size() + children.stream().filter((cn) -> cn != null).mapToInt(QuadNode::subtreeValueCount).sum();
    }

    /**
     * Size of the subtree.
     *
     * @return Count of nodes in the subtree.
     */
    int subtreeSize() {
        return 1 + children.stream().filter((cn) -> cn != null).mapToInt(QuadNode::subtreeSize).sum();
    }

    /**
     * Inserts a value pair into the node. The value key must be contained in
     * this nodes bounding box.
     *
     * @param newpair Value to be inserted.
     */
    void put(Pair<K, V> newpair) {
        assert box.contains(newpair.getKey()) : "Not contained: " + box + " - " + newpair.getKey();
        if (hasChildren() && Arrays.stream(subboxes).anyMatch((bb) -> bb.contains(newpair.getKey()))) {
            getChild(newpair.getKey()).put(newpair);
        } else {
            values.add(newpair);
            if (values.size() > maxNodeSize && !isForked) {
                split();
                assert !children.contains(this);
            }
//            assert values.size() <= parent.getMaxNodeSize() : "Values is larger than max size. " + values.size();
        }

    }

    /**
     * Bounds of the node.
     *
     * @return Bounds of the node.
     */
    IDBoundingBox getBounds() {
        return box;
    }

    /**
     * Values in the node.
     *
     * @return Value list containing all value pairs in this node.
     */
    List<Pair<K, V>> getValues() {
        return values;
    }

    /**
     * Splits the node into subnodes and moves all value pairs that are
     * contained within a subnode to the corresponding subnode.
     */
    void split() {
        for (Iterator<Pair<K, V>> it = values.iterator(); it.hasNext();) {
            Pair<K, V> p = it.next();
            QuadNode<K, V> child = getChild(p.getKey());
            if (child != null) {
                getChild(p.getKey()).put(p);
                it.remove();
            }
        }
        isForked = true;
    }

    /**
     * Returns the child that contains the key, or null if no child contains the
     * key.
     *
     * @param key Key to query for child containment.
     * @return A childnode if the key fits into a child node or null, if the key
     * doesn't fit within any child node.
     */
    QuadNode<K, V> getChild(K key) {
        final int selected = selectChild(key);

        if (selected < 0 || selected > 3) {
            return null;
        }

        if (children.get(selected) == null) {
            children.set(selected, new QuadNode<>(subboxes[selected], maxNodeSize));
        }

        return children.get(selected);
    }

    /**
     * Select the index of the child that contains the key, or -1 if the key
     * fits into no child.
     *
     * @param key Key to query child containment for.
     * @return Index of the child in the children list or -1 if no child
     * contains the key.
     */
    int selectChild(K key) {
        assert box.contains(key);
        for (int i = 0; i < subboxes.length; i++) {
            if (subboxes[i].contains(key)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * All children of this node. Modifying this list may lead to undefined
     * behaviour.
     *
     * @return All children of this node.
     */
    List<QuadNode<K, V>> getChildren() {
        return children;
    }

    /**
     * True if no values are contained within this node.
     *
     * @return True if no values are contained within this node.
     */
    boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * True if the subtree starting at this node contains no values.
     *
     * @return True if the subtree starting at this node contains no values.
     */
    boolean isSubtreeEmpty() {
        return isEmpty() && children.stream().allMatch((child) -> child != null && child.isEmpty());
    }

    /**
     * True if this node has children.
     *
     * @return True if this node has children.
     */
    boolean hasChildren() {
        return isForked && children.stream().anyMatch((p) -> p != null);
    }

    /**
     * Clears this node, removing all values and children.
     */
    void clear() {
        values.clear();
        children.clear();

        for (int i = 0; i < 4; i++) {
            children.add(null);
        }
    }

    /**
     * Removes all values in this subtree associated with the provided key.
     *
     * @param key Key to search for.
     * @return A list containing all removed values.
     */
    List<Pair<K, V>> subtreeRemove(K key) {
        List<Pair<K, V>> vals = values.stream().filter((p) -> Objects.equals(p.getKey(), key)).collect(Collectors.toList());
        values.removeAll(vals);
        children.stream()
                .filter((cn) -> cn != null && cn.getBounds().contains(key))
                .map((cn) -> cn.subtreeRemove(key))
                .forEach((list) -> vals.addAll(list));

        return vals;
    }

    public Collection<Pair<K, V>> subtreeRemoveValue(V value, Predicate<IDBoundingBox> hint) {
        List<Pair<K, V>> vals = values.stream().filter((p) -> Objects.equals(p.getValue(), value)).collect(Collectors.toList());
        values.removeAll(vals);
        children.stream()
                .filter((cn) -> cn != null && hint.test(cn.getBounds()))
                .map((cn) -> cn.subtreeRemoveValue(value, hint))
                .forEach((list) -> vals.addAll(list));

        return vals;
    }

    /**
     * Removes all keys associated with the provided value from the subtree.
     *
     * @param key Key to search for.
     * @param value Value to search for.
     * @return List of values that have been removed.
     */
    List<Pair<K, V>> subtreeRemove(K key, V value) {
        List<Pair<K, V>> vals = values.stream()
                .filter((p) -> Objects.equals(p.getKey(), key) && Objects.equals(p.getValue(), value))
                .collect(Collectors.toList());

        values.removeAll(vals);

        children.stream()
                .filter((cn) -> cn != null && cn.getBounds().contains(key))
                .map((cn) -> cn.subtreeRemove(key, value))
                .forEach((list) -> vals.addAll(list));

        return vals;
    }

    /**
     * True if this nodes integrity (all conditions that are required for a
     * quadnode) are true.
     *
     * @return True if this nodes integrity (all conditions that are required
     * for a quadnode) are true.
     */
    boolean checkIntegrity() {
        assert maxNodeSize >= 1;
        assert subboxes != null;
        assert subboxes.length == 4;
        assert Arrays.stream(subboxes).noneMatch((b) -> b == null);
//        assert values.size() <= parent.getMaxNodeSize();
        assert values.stream().noneMatch((p) -> p == null);
        assert values.stream().allMatch((p) -> box.contains(p.getKey()));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    continue;
                }

                assert !subboxes[i].same(subboxes[j]);
            }
        }

        assert children.stream().filter((n) -> n != null).distinct().count() == children.stream().filter((n) -> n != null).count();
        return true;
    }

    QuadNode<K, V> construct(final IDBoundingBox box) {
        return new QuadNode<>(box, maxNodeSize);
    }

    public int getMaxNodeSize() {
        return maxNodeSize;
    }
}
