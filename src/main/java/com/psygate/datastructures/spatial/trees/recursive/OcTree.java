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
import com.psygate.datastructures.spatial.trees.MutableSpatialTree;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.psygate.datastructures.spatial.trees.BoundedSpatialTree;
import com.psygate.datastructures.spatial.ID3BoundingBox;
import com.psygate.datastructures.spatial.ID3Orderable;

/**
 *
 * @author psygate (https://github.com/psygate)
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class OcTree<K extends ID3Orderable, V> extends ImmutableOcTree<K, V> implements MutableSpatialTree<K, V, ID3BoundingBox> {

    private final AtomicLong modcnt = new AtomicLong(Long.MIN_VALUE);

    /**
     *
     * @param tree Tree to copy.
     */
    public OcTree(OcTree<K, V> tree) {
        super(tree, tree.getMaxNodeSize());
    }

    /**
     *
     * @param tree Tree to copy.
     * @param maxNodeSize Maximum node size.
     */
    public OcTree(BoundedSpatialTree<K, V, ID3BoundingBox, ID3BoundingBox> tree, int maxNodeSize) {
        super(tree, maxNodeSize);
    }

    /**
     *
     * @param bounds Bounds of the new quad tree.
     * @param maxNodeSize Maximum node size.
     */
    public OcTree(ID3BoundingBox bounds, int maxNodeSize) {
        super(bounds, maxNodeSize);
    }

    @Override
    public void put(Pair<K, V> pair) {
        if (!envelopes(pair.getKey())) {
            throw new IllegalArgumentException("Key outside bounds: " + getBounds() + " - " + pair.getKey());
        }
        modcnt.incrementAndGet();
        getRoot().add(pair);
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
    public Collection<V> removeValue(V value, Predicate<ID3BoundingBox> hint) {
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
    Spliterator<OcNode<K, V>> getSpliterator(OcNode<K, V> node, Predicate<ID3BoundingBox> pred) {
        return new CheckedNodeSpliterator(node, pred); //To change body of generated methods, choose Tools | Templates.
    }

    final class CheckedNodeSpliterator implements Spliterator<OcNode<K, V>> {

        private final long id = modcnt.get();
        private final NodeSpliterator it;

        protected CheckedNodeSpliterator() {
            it = new NodeSpliterator();
        }

        protected CheckedNodeSpliterator(OcNode<K, V> node) {
            it = new NodeSpliterator(node, (ID3BoundingBox t) -> true);
        }

        protected CheckedNodeSpliterator(OcNode<K, V> node, Predicate<ID3BoundingBox> predicate) {
            it = new NodeSpliterator(node, predicate);
        }

        @Override
        public boolean tryAdvance(Consumer<? super OcNode<K, V>> action) {
            checkState();
            return it.tryAdvance(action);
        }

        @Override
        public Spliterator<OcNode<K, V>> trySplit() {
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
