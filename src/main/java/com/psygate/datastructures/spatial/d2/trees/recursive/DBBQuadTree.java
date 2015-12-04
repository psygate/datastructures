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
import com.psygate.datastructures.spatial.d2.trees.IDBBQuadTree;
import com.psygate.datastructures.spatial.d2.trees.IDPointQuadTree;
import com.psygate.datastructures.spatial.d2.trees.IDPointQuadTree;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class DBBQuadTree<K extends IDBoundingBox, V> implements IDBBQuadTree<K, V> {

    private int size = 0;
    private final int maxNodeSize;
    private final DBBQuadNode<K, V> root;
    private final AtomicLong modcnt = new AtomicLong(Long.MIN_VALUE);

    public DBBQuadTree(DBBQuadTree<K, V> orig) {
        maxNodeSize = orig.getMaxNodeSize();
        root = new DBBQuadNode<>(new DBoundingBox(Objects.requireNonNull(orig.getBounds())), this);
        putAll(orig.entryStream());
    }

    public DBBQuadTree(int maxNodeSize, IDBoundingBox bounds) {
        if (maxNodeSize <= 0) {
            throw new IllegalArgumentException("Size cannot be smaller or equal to zero");
        }
        this.maxNodeSize = maxNodeSize;
        root = new DBBQuadNode<>(new DBoundingBox(Objects.requireNonNull(bounds)), this);
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
    public Collection<V> remove(K key) {

        if (!root.getBounds().contains(key)) {
            return new LinkedList<>();
        } else {
            List<Pair<K, V>> list = root.remove(key);
            size -= list.size();
            return list.stream().map((p) -> p.getValue()).collect(Collectors.toList());
        }
    }

    @Override
    public Collection<V> removeValue(V value) {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void put(K key, V value) {

        if (!envelopes(key)) {
            throw new IllegalArgumentException("Key outside bounds: " + getBounds() + " - " + key);
        }

        modcnt.incrementAndGet();
        root.put(new Pair<>(key, value));
        size++;
    }

    private void putAll(Stream<? extends Map.Entry<K, V>> stream) {

        stream.forEach((p) -> put(p));
    }

    @Override
    public int size() {

        assert size == root.countElements();
        return size;
    }

    @Override
    public boolean isEmpty() {

        return size() == 0;
    }

    @Override
    public Stream<K> keyStream() {

        return StreamSupport.stream(new TreeNodeSpliterator(), false).flatMap((n) -> n.getValues().stream()).map((n) -> n.getKey());
    }

    @Override
    public Stream<V> valueStream() {

        return StreamSupport.stream(new TreeNodeSpliterator(), false).flatMap((n) -> n.getValues().stream()).map((n) -> n.getValue());
    }

    @Override
    public Stream<Map.Entry<K, V>> entryStream() {

        return StreamSupport.stream(new TreeNodeSpliterator(), false).flatMap((n) -> n.getValues().stream());
    }

    @Override
    public Stream<K> selectiveKeyStream(Predicate<IDBoundingBox> predicate) {

        return StreamSupport.stream(new SelectiveTreeNodeSpliterator(predicate), false).flatMap((n) -> n.getValues().stream()).map((n) -> n.getKey());
    }

    @Override
    public Stream<V> selectiveValueStream(Predicate<IDBoundingBox> predicate) {

        return StreamSupport.stream(new SelectiveTreeNodeSpliterator(predicate), false).flatMap((n) -> n.getValues().stream()).map((n) -> n.getValue());
    }

    @Override
    public Stream<Map.Entry<K, V>> selectiveEntryStream(Predicate<IDBoundingBox> predicate) {

        return StreamSupport.stream(new SelectiveTreeNodeSpliterator(predicate), false).flatMap((n) -> n.getValues().stream());
    }

    @Override
    public Iterator<K> keyIterator() {

        return new KeyIterator();
    }

    @Override
    public Iterator<V> valueIterator() {

        return new ValueIterator();
    }

    @Override
    public Iterator<Map.Entry<K, V>> entryIterator() {

        return new EntryIterator();
    }

    @Override
    public Iterator<K> selectiveKeyIterator(Predicate<IDBoundingBox> predicate) {

        return new KeyIterator(predicate);
    }

    @Override
    public Iterator<V> selectiveValueIterator(Predicate<IDBoundingBox> predicate) {

        return new ValueIterator(predicate);
    }

    @Override
    public Iterator<Map.Entry<K, V>> selectiveEntryIterator(Predicate<IDBoundingBox> predicate) {

        return new EntryIterator(predicate);
    }

    @Override
    public Collection<K> keys() {

        return keyStream().collect(Collectors.toList());
    }

    @Override
    public Collection<V> values() {

        return valueStream().collect(Collectors.toList());
    }

    @Override
    public Collection<Map.Entry<K, V>> entries() {

        return entryStream().collect(Collectors.toList());
    }

    @Override
    public boolean containsKey(K key) {

        return selectiveKeyStream((box) -> box.contains(key)).anyMatch((p) -> Objects.equals(p, key));
    }

    @Override
    public boolean containsValue(V value) {

        return valueStream().anyMatch((p) -> Objects.equals(p, value));
    }

    @Override
    public boolean contains(K key, V value) {

        return selectiveEntryStream((b) -> b.contains(key)).anyMatch((en) -> Objects.equals(en.getKey(), key) && Objects.equals(en.getValue(), value));
    }

    @Override
    public boolean containsValue(V value, Predicate<IDBoundingBox> pred) {

        return selectiveEntryStream(pred).anyMatch((en) -> Objects.equals(en.getValue(), value));
    }

    @Override
    public int getMaxNodeSize() {
        return maxNodeSize;
    }

    @Override
    public void putAll(Map<K, V> values) {

        putAll(values.entrySet());
    }

    @Override
    public void putAll(Collection<? extends Map.Entry<K, V>> values) {

        values.forEach((p) -> put(p.getKey(), p.getValue()));
    }

    @Override
    public void put(Map.Entry<K, V> entry) {

        put(entry.getKey(), entry.getValue());
    }

    @Override
    public void clear() {

        root.clear();
        size = 0;
    }

    Iterator<DBBQuadNode<K, V>> nodeIterator() {

        return new NodeIterator();
    }

    Iterator<DBBQuadNode<K, V>> nodeIterator(Predicate<IDBoundingBox> pred) {

        return new NodeIterator(pred);
    }

    public boolean checkIntegrity() {
        assert root != null;
        Set<DBBQuadNode<K, V>> visited = new HashSet<>();
        Queue<DBBQuadNode<K, V>> stack = new LinkedList<>();
        stack.add(root);

        while (!stack.isEmpty()) {
            DBBQuadNode<K, V> node = stack.remove();
            node.checkIntegrity();
            if (visited.contains(node)) {
                return false;
            } else {
                visited.add(node);
            }

            stack.addAll(node.getChildren().stream().filter((n) -> n != null).collect(Collectors.toList()));
        }

        return root.countElements() == size;
    }

    private final class SelectiveTreeNodeSpliterator implements Spliterator<DBBQuadNode<K, V>> {

        private final LinkedList<DBBQuadNode<K, V>> stack = new LinkedList<>();
        private final long thiscount = modcnt.get();
        private final Predicate<IDBoundingBox> selector;

        SelectiveTreeNodeSpliterator(DBBQuadNode<K, V> node, Predicate<IDBoundingBox> selector) {
            stack.add(node);
            this.selector = selector;
        }

        SelectiveTreeNodeSpliterator(Predicate<IDBoundingBox> selector) {
            this(root, selector);
        }

        @Override
        public boolean tryAdvance(Consumer<? super DBBQuadNode<K, V>> action) {
            assertState();
            if (!stack.isEmpty()) {
                DBBQuadNode<K, V> node = stack.pop();
                node.getChildren().stream().filter((cn) -> (cn != null) && selector.test(cn.getBounds())).forEach((cn) -> {
                    stack.add(cn);
                });
                action.accept(node);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Spliterator<DBBQuadNode<K, V>> trySplit() {
            assertState();
            if (!stack.isEmpty()) {
                return new SelectiveTreeNodeSpliterator(stack.pop(), selector);
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.DISTINCT;
        }

        private void assertState() {
            if (thiscount != modcnt.get()) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private final class TreeNodeSpliterator implements Spliterator<DBBQuadNode<K, V>> {

        private final LinkedList<DBBQuadNode<K, V>> stack = new LinkedList<>();
        private final long thiscount = modcnt.get();

        TreeNodeSpliterator(DBBQuadNode<K, V> node) {
            stack.add(node);
        }

        TreeNodeSpliterator() {
            this(root);
        }

        @Override
        public boolean tryAdvance(Consumer<? super DBBQuadNode<K, V>> action) {
            assertState();
            if (!stack.isEmpty()) {
                DBBQuadNode<K, V> node = stack.pop();
                node.getChildren().stream().filter((cn) -> (cn != null)).forEach((cn) -> {
                    stack.add(cn);
                });
                action.accept(node);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Spliterator<DBBQuadNode<K, V>> trySplit() {
            assertState();
            if (!stack.isEmpty()) {
                return new TreeNodeSpliterator(stack.pop());
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.DISTINCT;
        }

        private void assertState() {
            if (thiscount != modcnt.get()) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private final class NodeIterator implements Iterator<DBBQuadNode<K, V>> {

        private final Deque<DBBQuadNode<K, V>> stack = new LinkedList<>();
        private final Predicate<IDBoundingBox> selector;
        private final long thiscount = modcnt.get();
        private final List<DBBQuadNode> visited = new LinkedList<>();

        private NodeIterator(DBBQuadNode<K, V> node, Predicate<IDBoundingBox> selector) {
            assert checkIntegrity();
            if (selector.test(node.getBounds())) {
                stack.add(node);
            }
            this.selector = selector;
        }

        private NodeIterator(Predicate<IDBoundingBox> selector) {
            this(root, selector);
        }

        private NodeIterator() {
            this((IDBoundingBox t) -> true);
        }

        @Override
        public boolean hasNext() {
            assertState();

            return !stack.isEmpty();
        }

        @Override
        public DBBQuadNode<K, V> next() {
            assertState();
            assert checkIntegrity();

            DBBQuadNode<K, V> next = stack.pop();

            next.getChildren().stream().filter((child) -> (child != null && selector.test(child.getBounds()))).forEach((child) -> {
                assert !visited.contains(child) : "Duplicate visit detected: " + child + " Last seen: " + visited.indexOf(child) + "/" + visited.size() + " Tree integrity: " + checkIntegrity();
                stack.push(child);
            });

            assert !visited.contains(next);
            assert visited.add(next);

            return next;
        }

//        QuadNode<K, V> peek() {
//            assertState();
//            return stack.peekFirst();
//        }
        private void assertState() {
            if (thiscount != modcnt.get()) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private final class KeyIterator implements Iterator<K> {

        private final EntryIterator it;

        KeyIterator(DBBQuadNode<K, V> node, Predicate<IDBoundingBox> selector) {
            it = new EntryIterator(node, selector);
        }

        KeyIterator(Predicate<IDBoundingBox> selector) {
            it = new EntryIterator(root, selector);
        }

        KeyIterator() {
            it = new EntryIterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public K next() {
            return it.next().getKey();
        }
    }

    private final class ValueIterator implements Iterator<V> {

        private final EntryIterator it;

        ValueIterator(DBBQuadNode<K, V> node, Predicate<IDBoundingBox> selector) {
            it = new EntryIterator(node, selector);
        }

        ValueIterator(Predicate<IDBoundingBox> selector) {
            it = new EntryIterator(root, selector);
        }

        ValueIterator() {
            it = new EntryIterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public V next() {
            return it.next().getValue();
        }
    }

    private final class EntryIterator implements Iterator<Map.Entry<K, V>> {

        private final NodeIterator it;
        private final LinkedList<Pair<K, V>> values = new LinkedList<>();

        EntryIterator(DBBQuadNode<K, V> node, Predicate<IDBoundingBox> selector) {
            it = new NodeIterator(node, selector);
        }

        EntryIterator(Predicate<IDBoundingBox> selector) {
            it = new NodeIterator(root, selector);
        }

        EntryIterator() {
            it = new NodeIterator();
        }

        @Override
        public boolean hasNext() {
            if (values.isEmpty()) {
                while (it.hasNext()) {
                    DBBQuadNode<K, V> node = it.next();
                    if (!node.getValues().isEmpty()) {
                        values.addAll(node.getValues());
                        break;
                    }
                }
            }
//            while (it.hasNext() && values.isEmpty()) {
//                values.addAll(it.next().getValues().stream().collect(Collectors.toList()));
//            }

            return !values.isEmpty();
        }

        @Override
        public Map.Entry<K, V> next() {
            return values.pop();
        }
    }
}
