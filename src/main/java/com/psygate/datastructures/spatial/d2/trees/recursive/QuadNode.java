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
import com.psygate.datastructures.spatial.d2.trees.recursive.QuadNode.Quadrant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.psygate.datastructures.spatial.d2.IDOrderable;

/**
 * Default implementation of a simple quad tree node.
 *
 * @see ImmutableQuadTree
 * @see QuadTree
 * @author psygate (https://github.com/psygate)
 */
class QuadNode<K extends IDOrderable, V> extends ANode<K, V, QuadNode<K, V>, Quadrant> {

    enum Quadrant {
        NW, NE, SW, SE
    };

    private final int maxNodeSize;
    private final IDBoundingBox box;
    private final Map<IDBoundingBox, Quadrant> subboxes = new HashMap<>();
//    private final ArrayList<QuadNode<K, V>> getChildren() = new ArrayList<>(4);
//    private boolean isSplit = false;

    QuadNode(IDBoundingBox box, int maxNodeSize) {
        super(new ArrayList<>(maxNodeSize), maxNodeSize);
        this.box = box;
        IDBoundingBox[] xsplit = box.splitMidX();
        IDBoundingBox[] yupsplit = xsplit[0].splitMidY();
        IDBoundingBox[] ydownsplit = xsplit[1].splitMidY();
        IDBoundingBox nw = yupsplit[0];
        IDBoundingBox sw = yupsplit[1];
        IDBoundingBox ne = ydownsplit[0];
        IDBoundingBox se = ydownsplit[1];

        subboxes.put(nw, Quadrant.NW);
        subboxes.put(sw, Quadrant.SW);
        subboxes.put(ne, Quadrant.NE);
        subboxes.put(se, Quadrant.SE);

        this.maxNodeSize = maxNodeSize;
    }

    QuadNode(IDBoundingBox box, int maxNodeSize, Collection<Pair<K, V>> values) {
        this(box, maxNodeSize);
        values.stream().forEach((pair) -> add(pair));
    }

    @Override
    void add(Pair<K, V> newpair) {
        assert box.contains(newpair.getKey()) : "Not contained: " + box + " - " + newpair.getKey();
        QuadNode<K, V> child = getChild(newpair.getKey());

        if (child == this) {
            super.add(newpair);
        } else {
            child.add(newpair);
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

    @Override
    void split() {
        setSplit(true);
        int size = getValues().size();
        List<Pair<K, V>> values = getValuesCopy();
        clearValues();
        values.forEach((v) -> add(v));
        assert subtreeValueCount() == size;
//        }
    }

    /**
     * Returns the child that contains the key, or this node if no child
     * contains the key.
     *
     * @param key Key to query for child containment.
     * @return A child node if the key fits into a child node or this node, if
     * the key doesn't fit within any child node.
     */
    QuadNode<K, V> getChild(K key) {
        assert box.contains(key);
        
        if (isSplit()) {
            for (Map.Entry<IDBoundingBox, Quadrant> entry : subboxes.entrySet()) {
                if (entry.getKey().contains(key)) {
                    if (!hasChild(entry.getValue())) {
                        setChild(entry.getValue(), new QuadNode<>(entry.getKey(), maxNodeSize));
                    }

                    return getChild(entry.getValue());
                }
            }
        }

        return this;
    }

    /**
     * Removes all values in this subtree associated with the provided key.
     *
     * @param key Key to search for.
     * @return A list containing all removed values.
     */
    List<Pair<K, V>> subtreeRemove(K key) {
        List<Pair<K, V>> vals = getValues().stream().filter((p) -> Objects.equals(p.getKey(), key)).collect(Collectors.toList());
        getValues().removeAll(vals);
        getChildren().values().stream()
                .filter((cn) -> cn.getBounds().contains(key))
                .map((cn) -> cn.subtreeRemove(key))
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
        List<Pair<K, V>> vals = getValues().stream()
                .filter((p) -> Objects.equals(p.getKey(), key) && Objects.equals(p.getValue(), value))
                .collect(Collectors.toList());

        getValues().removeAll(vals);

        getChildren().values().stream()
                .filter((cn) -> cn.getBounds().contains(key))
                .map((cn) -> cn.subtreeRemove(key, value))
                .forEach((list) -> vals.addAll(list));

        return vals;
    }

    /**
     * Removes all values in this subtree that equal the provided value.
     *
     * @param value Value to search for.
     * @param hint Predicate used to prematurely remove nodes that should not be
     * searched for the value.
     * @return A list containing all removed values.
     */
    Collection<Pair<K, V>> subtreeRemoveValue(V value, Predicate<IDBoundingBox> hint) {
        List<Pair<K, V>> vals = getValues().stream().filter((p) -> Objects.equals(p.getValue(), value)).collect(Collectors.toList());
        getValues().removeAll(vals);
        getChildren().values().stream()
                .filter((cn) -> hint.test(cn.getBounds()))
                .map((cn) -> cn.subtreeRemoveValue(value, hint))
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
        assert subboxes.size() == 4;
        assert subboxes.keySet().stream().noneMatch((b) -> b == null);
//        assert values.size() <= parent.getMaxNodeSize();
        assert getValues().stream().noneMatch((p) -> p == null);
        assert getValues().stream().allMatch((p) -> box.contains(p.getKey()));

        assert getChildren().values().stream().distinct().count() == getChildren().values().stream().filter((n) -> n != null).count();
        return true;
    }

    QuadNode<K, V> construct(final IDBoundingBox box) {
        return new QuadNode<>(box, maxNodeSize);
    }

    public int getMaxNodeSize() {
        return maxNodeSize;
    }
}
