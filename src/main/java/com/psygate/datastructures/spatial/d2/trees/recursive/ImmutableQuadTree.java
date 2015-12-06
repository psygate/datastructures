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
import com.psygate.datastructures.spatial.d2.DBoundingBox;
import com.psygate.datastructures.spatial.MutableSpatialTree;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import com.psygate.datastructures.spatial.NodeSizeSpatialTree;
import com.psygate.datastructures.spatial.SpatialTree;
import com.psygate.datastructures.spatial.d2.trees.BoundingBoxTree;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import com.psygate.datastructures.spatial.d2.IDOrderable;

/**
 * Immutable quad tree implementation. This tree cannot be modified after
 * construction and is safe to traverse concurrently.
 *
 * @author psygate (https://github.com/psygate)
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ImmutableQuadTree<K extends IDOrderable, V> implements NodeSizeSpatialTree<K, V, IDBoundingBox>, BoundingBoxTree<K, V, IDBoundingBox> {

    private final QuadNode<K, V> root;
    int size = 0;

    /**
     *
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    ImmutableQuadTree(IDBoundingBox bounds, int maxNodeSize) {
        this.root = new QuadNode<>(new DBoundingBox(bounds), maxNodeSize);
    }

    /**
     *
     * @param tree SpatialTree to copy.
     */
    public ImmutableQuadTree(NodeSizeSpatialTree<K, V, IDBoundingBox> tree) {
        this(tree, tree.getMaxNodeSize());
    }

    /**
     *
     * @param tree SpatialTree to copy.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    public ImmutableQuadTree(SpatialTree<K, V, IDBoundingBox> tree, int maxNodeSize) {
        this(tree.entryStream(), tree.getBounds(), maxNodeSize);
    }

    /**
     *
     * @param values Values to insert upon construction.
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    public ImmutableQuadTree(Map<K, V> values, IDBoundingBox bounds, int maxNodeSize) {
        this(values.entrySet(), bounds, maxNodeSize);
    }

    /**
     *
     * @param values Values to insert upon construction.
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    public ImmutableQuadTree(Collection<? extends Map.Entry<K, V>> values, IDBoundingBox bounds, int maxNodeSize) {
        this(values.stream(), bounds, maxNodeSize);
    }

    /**
     *
     * @param values Values to insert upon construction.
     * @param bounds Bounds of the new tree.
     * @param maxNodeSize Maximum node size of the new tree.
     */
    ImmutableQuadTree(Stream<? extends Map.Entry<K, V>> values, IDBoundingBox bounds, int maxNodeSize) {
        this.root = new QuadNode<>(new DBoundingBox(bounds), maxNodeSize);
        values.forEach((en) -> {
            root.add(new Pair<>(en));
            size++;
        });
    }

    @Override
    public IDBoundingBox getBounds() {
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
    public Stream<Map.Entry<K, V>> selectiveEntryStream(Predicate<IDBoundingBox> predicate) {
        return selectiveNodeStream(root, predicate)
                .filter((n) -> !n.isEmpty())
                .flatMap((n) -> n.getValues().stream());
    }

    /**
     *
     * @return Stream iterating over all nodes in the contained tree.
     */
    Stream<QuadNode<K, V>> nodeStream() {
        return selectiveNodeStream(root, (IDBoundingBox b) -> true);
    }

    /**
     *
     * @return Stream iterating over all nodes that satisfy
     * Predicate.test(node.getBounds()) == true.
     */
    Stream<QuadNode<K, V>> selectiveNodeStream(QuadNode<K, V> node, Predicate<IDBoundingBox> pred) {
        return StreamSupport.stream(new NodeSpliterator(root, pred), false);
    }

    /**
     *
     * @return Spliterator iterating over all nodes that satisfy
     * Predicate.test(node.getBounds()) == true.
     */
    Spliterator<QuadNode<K, V>> getSpliterator(QuadNode<K, V> node, Predicate<IDBoundingBox> pred) {
        return new NodeSpliterator(node, pred);
    }

    /**
     *
     * @return Iterator iterating over all nodes in the contained tree.
     */
    Iterator<QuadNode<K, V>> nodeIterator() {
        return nodeStream().iterator();
    }

    /**
     *
     * @return Iterator iterating over all nodes that satisfy
     * Predicate.test(node.getBounds()) == true.
     */
    Iterator<QuadNode<K, V>> nodeIterator(Predicate<IDBoundingBox> pred) {
        return selectiveNodeStream(root, pred).iterator();
    }

    @Override
    public int getMaxNodeSize() {
        return root.getMaxNodeSize();
    }

    /**
     *
     * @return Root node if this tree. Cannot be null.
     */
    QuadNode<K, V> getRoot() {
        return root;
    }

    /**
     * Spliterator iterating over all nodes of the tree.
     */
    final class NodeSpliterator implements Spliterator<QuadNode<K, V>> {

        private final Queue<QuadNode<K, V>> stack = new LinkedList<>();
        private final Predicate<IDBoundingBox> predicate;

        protected NodeSpliterator() {
            this(root);
        }

        protected NodeSpliterator(QuadNode<K, V> node) {
            this(node, (IDBoundingBox t) -> true);
        }

        protected NodeSpliterator(QuadNode<K, V> node, Predicate<IDBoundingBox> predicate) {
            stack.add(Objects.requireNonNull(node));
            this.predicate = predicate;
        }

        @Override
        public boolean tryAdvance(Consumer<? super QuadNode<K, V>> action) {
            if (stack.isEmpty()) {
                return false;
            } else {
                QuadNode<K, V> selected = stack.remove();
                selected.getChildren().values().stream()
                        .filter((cn) -> predicate.test(cn.getBounds()))
                        .forEach((cn) -> stack.add(cn));
                action.accept(selected);
                return true;
            }
        }

        @Override
        public Spliterator<QuadNode<K, V>> trySplit() {
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
