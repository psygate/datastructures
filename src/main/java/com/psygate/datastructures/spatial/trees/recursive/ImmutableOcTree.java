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
import com.psygate.datastructures.spatial.D3BoundingBox;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import com.psygate.datastructures.spatial.trees.BoundedSpatialTree;
import com.psygate.datastructures.spatial.ID3BoundingBox;
import com.psygate.datastructures.spatial.ID3Boundable;

/**
 * Immutable quad tree implementation. This tree cannot be modified after
 * construction and is safe to traverse concurrently.
 *
 * @author psygate (https://github.com/psygate)
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ImmutableOcTree<K extends ID3Boundable, V> implements BoundedSpatialTree<K, V, ID3BoundingBox, ID3BoundingBox> {

    private final OcNode<K, V> root;
    int size = 0;

    /**
     *
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    ImmutableOcTree(ID3BoundingBox bounds, int maxNodeSize) {
        this.root = new OcNode<>(new D3BoundingBox(bounds), maxNodeSize);
    }

    /**
     *
     * @param tree SpatialTree to copy.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    public ImmutableOcTree(BoundedSpatialTree<K, V, ID3BoundingBox, ID3BoundingBox> tree, int maxNodeSize) {
        this(tree.entryStream(), tree.getBounds(), maxNodeSize);
    }

    /**
     *
     * @param values Values to insert upon construction.
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    public ImmutableOcTree(Map<K, V> values, ID3BoundingBox bounds, int maxNodeSize) {
        this(values.entrySet(), bounds, maxNodeSize);
    }

    /**
     *
     * @param values Values to insert upon construction.
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    public ImmutableOcTree(Collection<? extends Map.Entry<K, V>> values, ID3BoundingBox bounds, int maxNodeSize) {
        this(values.stream(), bounds, maxNodeSize);
    }

    /**
     *
     * @param values Values to insert upon construction.
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    ImmutableOcTree(Stream<? extends Map.Entry<K, V>> values, ID3BoundingBox bounds, int maxNodeSize) {
        this.root = new OcNode<>(new D3BoundingBox(bounds), maxNodeSize);
        values.forEach((en) -> {
            root.add(new Pair<>(en));
            size++;
        });
    }

    @Override
    public ID3BoundingBox getBounds() {
        return root.getBounds();
    }

    @Override
    public boolean envelopes(K point) {
        return root.getBounds().contains(point);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Stream<Map.Entry<K, V>> selectiveEntryStream(Predicate<ID3BoundingBox> predicate) {
        return selectiveNodeStream(root, predicate)
                .filter((n) -> !n.isEmpty())
                .flatMap((n) -> n.getValues().stream());
    }

    /**
     *
     * @return Stream iterating over all nodes in the contained tree.
     */
    Stream<OcNode<K, V>> nodeStream() {
        return selectiveNodeStream(root, (ID3BoundingBox b) -> true);
    }

    /**
     *
     * @return Stream iterating over all nodes that satisfy
     * Predicate.test(node.getBounds()) == true.
     */
    Stream<OcNode<K, V>> selectiveNodeStream(OcNode<K, V> node, Predicate<ID3BoundingBox> pred) {
        return StreamSupport.stream(new NodeSpliterator(root, pred), false);
    }

    /**
     *
     * @return Spliterator iterating over all nodes that satisfy
     * Predicate.test(node.getBounds()) == true.
     */
    Spliterator<OcNode<K, V>> getSpliterator(OcNode<K, V> node, Predicate<ID3BoundingBox> pred) {
        return new NodeSpliterator(node, pred);
    }

    /**
     *
     * @return Iterator iterating over all nodes in the contained tree.
     */
    Iterator<OcNode<K, V>> nodeIterator() {
        return nodeStream().iterator();
    }

    /**
     *
     * @return Iterator iterating over all nodes that satisfy
     * Predicate.test(node.getBounds()) == true.
     */
    Iterator<OcNode<K, V>> nodeIterator(Predicate<ID3BoundingBox> pred) {
        return selectiveNodeStream(root, pred).iterator();
    }

    public int getMaxNodeSize() {
        return root.getMaxNodeSize();
    }

    /**
     *
     * @return Root node if this tree. Cannot be null.
     */
    OcNode<K, V> getRoot() {
        return root;
    }

    @Override
    public boolean containsKey(K key) {
        return selectiveKeyStream((ID3BoundingBox b) -> b.contains(key))
                .anyMatch((k) -> Objects.equals(k, key));
    }

    @Override
    public boolean contains(K key, V value) {
        return selectiveEntryStream((ID3BoundingBox b) -> b.contains(key))
                .anyMatch((en) -> Objects.equals(en.getKey(), key) && Objects.equals(en.getValue(), value));
    }

    @Override
    public boolean containsValue(V value, Predicate<ID3BoundingBox> pred) {
        return selectiveEntryStream(pred)
                .map(Map.Entry::getValue)
                .anyMatch((v) -> Objects.equals(v, value));
    }

    @Override
    public boolean containsValue(V value) {
        return valueStream()
                .anyMatch((v) -> Objects.equals(v, value));
    }

    /**
     * Spliterator iterating over all nodes of the tree.
     */
    final class NodeSpliterator implements Spliterator<OcNode<K, V>> {

        private final Queue<OcNode<K, V>> stack = new LinkedList<>();
        private final Predicate<ID3BoundingBox> predicate;

        protected NodeSpliterator() {
            this(root);
        }

        protected NodeSpliterator(OcNode<K, V> node) {
            this(node, (ID3BoundingBox t) -> true);
        }

        protected NodeSpliterator(OcNode<K, V> node, Predicate<ID3BoundingBox> predicate) {
            stack.add(Objects.requireNonNull(node));
            this.predicate = predicate;
        }

        @Override
        public boolean tryAdvance(Consumer<? super OcNode<K, V>> action) {
            if (stack.isEmpty()) {
                return false;
            } else {
                OcNode<K, V> selected = stack.remove();
                selected.getChildren().values().stream()
                        .filter((cn) -> predicate.test(cn.getBounds()))
                        .forEach((cn) -> stack.add(cn));
                action.accept(selected);
                return true;
            }
        }

        @Override
        public Spliterator<OcNode<K, V>> trySplit() {
            if (!stack.isEmpty()) {
                return new NodeSpliterator(stack.remove(), predicate);
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return size / 4;
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE;
        }

    }
}
