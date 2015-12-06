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

import com.psygate.datastructures.util.Pair;
import com.psygate.datastructures.spatial.trees.recursive.OcNode.Quadrant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.psygate.datastructures.spatial.ID3BoundingBox;
import com.psygate.datastructures.spatial.ID3Orderable;

/**
 * Default implementation of a simple quad tree node.
 *
 * @see ImmutableQuadTree
 * @see QuadTree
 * @author psygate (https://github.com/psygate)
 */
class OcNode<K extends ID3Orderable, V> extends AbstractSpatialNode<K, V, OcNode<K, V>, Quadrant> {

    enum Quadrant {
        UNW, UNE, USW, USE,
        DNW, DNE, DSW, DSE
    };

    private final ID3BoundingBox box;
    private final Map<ID3BoundingBox, Quadrant> subboxes = new HashMap<>();
//    private final ArrayList<QuadNode<K, V>> getChildren() = new ArrayList<>(4);
//    private boolean isSplit = false;

    OcNode(ID3BoundingBox box, int maxNodeSize) {
        super(new ArrayList<>(maxNodeSize), maxNodeSize);
        this.box = box;
        //Split into up and down.
        ID3BoundingBox[] split = box.splitMidY();

        ID3BoundingBox[] uxsplit = split[0].splitMidX();
        ID3BoundingBox[] uyupsplit = uxsplit[0].splitMidY();
        ID3BoundingBox[] uydownsplit = uxsplit[1].splitMidY();
        ID3BoundingBox unw = uyupsplit[0];
        ID3BoundingBox usw = uyupsplit[1];
        ID3BoundingBox une = uydownsplit[0];
        ID3BoundingBox use = uydownsplit[1];

        ID3BoundingBox[] dxsplit = split[0].splitMidX();
        ID3BoundingBox[] dyupsplit = dxsplit[0].splitMidY();
        ID3BoundingBox[] dydownsplit = dxsplit[1].splitMidY();
        ID3BoundingBox dnw = dyupsplit[0];
        ID3BoundingBox dsw = dyupsplit[1];
        ID3BoundingBox dne = dydownsplit[0];
        ID3BoundingBox dse = dydownsplit[1];

        subboxes.put(unw, Quadrant.UNW);
        subboxes.put(usw, Quadrant.USW);
        subboxes.put(une, Quadrant.UNE);
        subboxes.put(use, Quadrant.USE);

        subboxes.put(dnw, Quadrant.DNW);
        subboxes.put(dsw, Quadrant.DSW);
        subboxes.put(dne, Quadrant.DNE);
        subboxes.put(dse, Quadrant.DSE);
    }

    OcNode(ID3BoundingBox box, int maxNodeSize, Collection<Pair<K, V>> values) {
        this(box, maxNodeSize);
        values.stream().forEach((pair) -> add(pair));
    }

    @Override
    void add(Pair<K, V> newpair) {
        assert box.contains(newpair.getKey()) : "Not contained: " + box + " - " + newpair.getKey();
        OcNode<K, V> child = getChild(newpair.getKey());

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
    ID3BoundingBox getBounds() {
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
    OcNode<K, V> getChild(K key) {
        assert box.contains(key);

        if (isSplit()) {
            for (Map.Entry<ID3BoundingBox, Quadrant> entry : subboxes.entrySet()) {
                if (entry.getKey().contains(key)) {
                    if (!hasChild(entry.getValue())) {
                        setChild(entry.getValue(), new OcNode<>(entry.getKey(), getMaxNodeSize()));
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
    Collection<Pair<K, V>> subtreeRemoveValue(V value, Predicate<ID3BoundingBox> hint) {
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
        assert getMaxNodeSize() >= 1;
        assert subboxes != null;
        assert subboxes.size() == 8;
        assert subboxes.keySet().stream().noneMatch((b) -> b == null);
//        assert values.size() <= parent.getMaxNodeSize();
        assert getValues().stream().noneMatch((p) -> p == null);
        assert getValues().stream().allMatch((p) -> box.contains(p.getKey()));

        assert getChildren().values().stream().distinct().count() == getChildren().values().stream().filter((n) -> n != null).count();
        return true;
    }

    OcNode<K, V> construct(final ID3BoundingBox box) {
        return new OcNode<>(box, getMaxNodeSize());
    }
}
