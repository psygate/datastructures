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
import com.psygate.datastructures.spatial.SpatialTree;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author psygate (https://github.com/psygate)
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class QuadTree<K extends IDBoundingBoxContainable, V> extends ImmutableQuadTree<K, V> implements MutableSpatialTree<K, V, IDBoundingBox> {

    private final AtomicLong modcnt = new AtomicLong(Long.MIN_VALUE);

    /**
     *
     * @param tree Tree to copy.
     */
    public QuadTree(NodeSizeSpatialTree<K, V, IDBoundingBox> tree) {
        super(tree);
    }

    /**
     *
     * @param tree Tree to copy.
     * @param maxNodeSize Maximum node size.
     */
    public QuadTree(SpatialTree<K, V, IDBoundingBox> tree, int maxNodeSize) {
        super(tree, maxNodeSize);
    }

    /**
     *
     * @param bounds Bounds of the new quad tree.
     * @param maxNodeSize Maximum node size.
     */
    public QuadTree(IDBoundingBox bounds, int maxNodeSize) {
        super(bounds, maxNodeSize);
    }

    @Override
    public void put(Pair<K, V> pair) {
        if (!envelopes(pair.getKey())) {
            throw new IllegalArgumentException("Key outside bounds: " + getBounds() + " - " + pair.getKey());
        }
        modcnt.incrementAndGet();
        getRoot().put(pair);
        size++;
    }
    
    @Override
    public Collection<V> remove(K key) {
        if (!getRoot().getBounds().contains(Objects.requireNonNull(key))) {
            return new LinkedList<>();
        } else {
            List<Pair<K, V>> list = getRoot().subtreeRemove(key);
            size -= list.size();
            return list.stream().map((p) -> p.getValue()).collect(Collectors.toList());
        }
    }

    @Override
    public Collection<V> remove(K key, V value) {
        Collection<V> col = getRoot().subtreeRemove(key, value).stream()
                .map(Pair::getValue)
                .collect(Collectors.toList());

        size -= col.size();
        return col;
    }

    @Override
    public Collection<V> removeValue(V value, Predicate<IDBoundingBox> hint) {
        Collection<V> col = getRoot().subtreeRemoveValue(value, hint).stream()
                .map(Pair::getValue)
                .collect(Collectors.toList());

        size -= col.size();
        return col;
    }

    @Override
    public void clear() {
        getRoot().clear();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    Spliterator<QuadNode<K, V>> getSpliterator(QuadNode<K, V> node, Predicate<IDBoundingBox> pred) {
        return new CheckedNodeSpliterator(node, pred); //To change body of generated methods, choose Tools | Templates.
    }

    final class CheckedNodeSpliterator implements Spliterator<QuadNode<K, V>> {

        private final long id = modcnt.get();
        private final NodeSpliterator it;

        protected CheckedNodeSpliterator() {
            it = new NodeSpliterator();
        }

        protected CheckedNodeSpliterator(QuadNode<K, V> node) {
            it = new NodeSpliterator(node, (IDBoundingBox t) -> true);
        }

        protected CheckedNodeSpliterator(QuadNode<K, V> node, Predicate<IDBoundingBox> predicate) {
            it = new NodeSpliterator(node, predicate);
        }

        @Override
        public boolean tryAdvance(Consumer<? super QuadNode<K, V>> action) {
            checkState();
            return it.tryAdvance(action);
        }

        @Override
        public Spliterator<QuadNode<K, V>> trySplit() {
            checkState();
            return it.trySplit();
        }

        @Override
        public long estimateSize() {
            checkState();
            return it.estimateSize();
        }

        @Override
        public int characteristics() {
            checkState();
            return it.characteristics();
        }

        void checkState() {
            if (id != modcnt.get()) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
