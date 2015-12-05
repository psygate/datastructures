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

import com.psygate.datastructures.spatial.d2.IDBoundingBox;
import com.psygate.datastructures.spatial.d2.IDBoundingBoxContainable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
class QuadNodeIterator<K extends IDBoundingBoxContainable, V> implements Iterator<QuadNode<K, V>> {

    private final Queue<QuadNode<K, V>> stack = new LinkedList<>();
    private final Predicate<IDBoundingBox> selector;

    public QuadNodeIterator(QuadNode<K, V> node) {
        stack.add(node);
        selector = (b) -> true;
    }

    public QuadNodeIterator(QuadNode<K, V> node, Predicate<IDBoundingBox> selector) {
        this.selector = selector;
        if (selector.test(node.getBounds())) {
            stack.add(node);
        }
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public QuadNode<K, V> next() {
        QuadNode<K, V> node = stack.remove();
        node.getChildren().stream()
                .filter((cn) -> cn != null && selector.test(cn.getBounds()))
                .forEach((cn) -> stack.add(cn));

        return node;
    }

}
