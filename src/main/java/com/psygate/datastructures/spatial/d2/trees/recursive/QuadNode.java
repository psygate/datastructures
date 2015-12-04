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
class QuadNode<K extends IDPoint, V> {

    private final IDBoundingBox box;
    private final IDBoundingBox[] subboxes;
    private final SizeableQuadTree parent;
    private final List<Pair<K, V>> values;
    private final ArrayList<QuadNode<K, V>> children = new ArrayList<>(4);

    QuadNode(IDBoundingBox box, SizeableQuadTree parent) {
        this.box = box;
        this.parent = parent;
        values = new ArrayList<>(parent.getMaxNodeSize());
        subboxes = subsplit(box);
        children.addAll(Arrays.asList(new QuadNode[]{null, null, null, null}));
    }

    private static IDBoundingBox[] subsplit(IDBoundingBox box) {
        IDBoundingBox[] out = Arrays.stream(box.splitMidX())
                .flatMap((b) -> Arrays.stream(b.splitMidY()))
                .collect(Collectors.toList()).toArray(new IDBoundingBox[4]);

        return out;
    }

    QuadNode(IDBoundingBox box, SizeableQuadTree parent, Collection<Pair<K, V>> values) {
        this(box, parent);
//        this.values.addAll(values);
//TODO
    }

    int countElements() {

        return values.size() + children.stream().filter((cn) -> cn != null).mapToInt(QuadNode::countElements).sum();
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

        values.stream().forEach((p) -> {
            getChild(p.getKey()).put(p);
        });

        assert !children.contains(this);
        assert children.stream().filter((n) -> n != null).distinct().count() == children.stream().filter((n) -> n != null).count();
        values.clear();

    }

    void put(Pair<K, V> p) {
        assert box.contains(p.getKey()) : "Not contained: " + box + " - " + p.getKey();
        if (hasChildren()) {
            getChild(p.getKey()).put(p);
        } else {
            values.add(p);
            if (values.size() > parent.getMaxNodeSize()) {
                split();
                assert !children.contains(this);
            }
            assert values.size() <= parent.getMaxNodeSize() : "Values is larger than max size. " + values.size();
        }

    }

    QuadNode<K, V> getChild(K key) {
        final int selected = selectChild(key);
        assert selected >= 0 && selected < 4 : "Selected node invalid: " + selected;

        if (children.get(selected) == null) {
            children.set(selected, new QuadNode<>(subboxes[selected], parent));
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

        throw new IllegalArgumentException("Selected node index invalid.");
    }

    List<QuadNode<K, V>> getChildren() {

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
        assert values.size() <= parent.getMaxNodeSize();
        assert values.stream().noneMatch((p) -> p == null);

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
