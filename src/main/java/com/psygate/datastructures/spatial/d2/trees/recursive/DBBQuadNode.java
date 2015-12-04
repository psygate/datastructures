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
import com.psygate.datastructures.spatial.d2.IDPoint;
import com.psygate.datastructures.spatial.d2.trees.DBoundingBox;
import com.psygate.datastructures.spatial.d2.trees.SizeableQuadTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
class DBBQuadNode<K extends IDBoundingBox, V> {

    private final IDBoundingBox box;
    private final IDBoundingBox[] subboxes;
    private final SizeableQuadTree parent;
    private final List<Pair<K, V>> values;
    private final ArrayList<DBBQuadNode<K, V>> children = new ArrayList<>(4);
    private boolean isForked = false;

    DBBQuadNode(IDBoundingBox box, SizeableQuadTree parent) {
        this.box = box;
        this.parent = parent;
        values = new ArrayList<>(parent.getMaxNodeSize());
        subboxes = subsplit(box);
        children.addAll(Arrays.asList(new DBBQuadNode[]{null, null, null, null}));
    }

    private static IDBoundingBox[] subsplit(IDBoundingBox box) {
        IDBoundingBox[] out = Arrays.stream(box.splitMidX())
                .flatMap((b) -> Arrays.stream(b.splitMidY()))
                .collect(Collectors.toList()).toArray(new IDBoundingBox[4]);

        return out;
    }

    DBBQuadNode(IDBoundingBox box, SizeableQuadTree parent, Collection<Pair<K, V>> values) {
        this(box, parent);
//        this.values.addAll(values);
//TODO
    }

    int countElements() {
        return values.size() + children.stream().filter((cn) -> cn != null).mapToInt(DBBQuadNode::countElements).sum();
    }

    IDBoundingBox getBounds() {

        return box;
    }

    List<Pair<K, V>> getValues() {

        return values;
    }

    void split() {
        assert values.size() >= parent.getMaxNodeSize();
        assert !values.isEmpty();

        for (Iterator<Pair<K, V>> it = values.iterator(); it.hasNext();) {
            Pair<K, V> p = it.next();
            DBBQuadNode<K, V> child = getChild(p.getKey());
            if (child != null) {
                getChild(p.getKey()).put(p);
                it.remove();
            }
        }
        isForked = true;
//        assert children.stream().filter((n) -> n != null).mapToInt((v) -> v.getValues().size()).sum() == values.size();
        assert !children.contains(this);
        assert children.stream().filter((n) -> n != null).distinct().count() == children.stream().filter((n) -> n != null).count();
    }

    void put(Pair<K, V> p) {
        assert box.contains(p.getKey()) : "Not contained: " + box + " - " + p.getKey();
        if (hasChildren() && Arrays.stream(subboxes).anyMatch((bb) -> bb.contains(p.getKey()))) {
            getChild(p.getKey()).put(p);
        } else {
            values.add(p);
            if (values.size() > parent.getMaxNodeSize() && !isForked) {
                split();
                assert !children.contains(this);
            }
//            assert values.size() <= parent.getMaxNodeSize() : "Values is larger than max size. " + values.size();
        }

    }

    DBBQuadNode<K, V> getChild(K key) {
        final int selected = selectChild(key);

        if (selected == -1) {
            return null;
        }

        if (children.get(selected) == null) {
            children.set(selected, new DBBQuadNode<>(subboxes[selected], parent));
        }

        return children.get(selected);
    }

    int selectChild(K key) {
        assert box.contains(key);
        for (int i = 0; i < subboxes.length; i++) {
            if (subboxes[i].contains(key)) {
                return i;
            }
        }

        return -1;
    }

    List<DBBQuadNode<K, V>> getChildren() {

        assert !children.contains(this);
        return children;
    }

    int subtreeSize() {
        return values.size() + children.stream().filter((v) -> v != null).mapToInt((v) -> v.subtreeSize()).sum();
    }

    int size() {

        return values.size();
    }

    boolean isEmpty() {

        return values.isEmpty();
    }

    boolean hasChildren() {

        return children.stream().anyMatch((p) -> p != null);
    }

    void clear() {
        values.clear();
        children.clear();

        for (int i = 0; i < 4; i++) {
            children.add(null);
        }
    }

    List<Pair<K, V>> remove(K key) {

        List<Pair<K, V>> vals = values.stream().filter((p) -> Objects.equals(p.getKey(), key)).collect(Collectors.toList());
        values.removeAll(vals);
//        values.addAll(vals.stream().map((p) -> p.getValue()).collect(Collectors.toList()));
        children.stream()
                .filter((cn) -> cn != null && cn.getBounds().contains(key))
                .map((cn) -> cn.remove(key))
                .forEach((list) -> vals.addAll(list));

        return vals;
    }

    boolean checkIntegrity() {
        assert parent != null;
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
}
