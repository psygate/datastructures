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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An abstract node that can hold values.
 *
 * @author psygate (https://github.com/psygate)
 */
abstract class ANode<K, V, Q extends ANode, T> {

    private final List<Pair<K, V>> values;
    private final Map<T, Q> children;
    private final int maxNodeSize;
    private boolean split = false;

    public ANode(List<Pair<K, V>> values, int maxNodeSize) {
        this.values = values;
        this.children = new HashMap<>();
        this.maxNodeSize = maxNodeSize;
    }

    /**
     * Size of the AValuedNode value list.
     *
     * @return Size of the AValuedNode value list.
     */
    int size() {
        return values.size();
    }

    /**
     * Inserts a value pair into the node. The value key must be contained in
     * this nodes bounding box.
     *
     * @param newpair Value to be inserted.
     */
    void add(Pair<K, V> value) {
        values.add(value);

        if (size() > maxNodeSize && !isSplit()) {
            split();
        }
    }

    /**
     * Splits the node into sub nodes and moves all value pairs that are
     * contained within a sub node to the corresponding sub node.
     */
    abstract void split();

    /**
     * Values in the node.
     *
     * @return Value list containing all value pairs in this node.
     */
    List<Pair<K, V>> getValues() {
        return values;
    }

    /**
     * True if no values are contained within this node.
     *
     * @return True if no values are contained within this node.
     */
    boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     *
     * @return True if this node has children.
     */
    boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Clears this node, removing all values and children.
     */
    void clear() {
        values.clear();
        children.clear();
    }

    boolean isSplit() {
        return split;
    }

    void setSplit(boolean split) {
        this.split = split;
    }

    /**
     * All getChildren() of this node. Modifying this list may lead to undefined
     * behaviour.
     *
     * @return All getChildren() of this node.
     */
    Map<T, Q> getChildren() {
        return children;
    }

    void setChild(T id, Q child) {
        children.put(id, child);
    }

    Q getChild(T id) {
        return children.get(id);
    }

    boolean hasChild(T id) {
        return children.containsKey(id);
    }

    void clearValues() {
        values.clear();
    }

    List<Pair<K, V>> getValuesCopy() {
        return new LinkedList<>(values);
    }

    public int getMaxNodeSize() {
        return maxNodeSize;
    }

    /**
     * Size of the subtree value list, including all child nodes and their child
     * nodes.
     *
     * @return
     */
    int subtreeValueCount() {
        return size() + getChildren().values().stream().mapToInt(ANode::subtreeValueCount).sum();
    }

    /**
     * Size of the subtree.
     *
     * @return Count of nodes in the subtree.
     */
    int subtreeSize() {
        return 1 + getChildren().values().stream().mapToInt(ANode::subtreeSize).sum();
    }

    /**
     * True if the subtree starting at this node contains no values.
     *
     * @return True if the subtree starting at this node contains no values.
     */
    boolean isSubtreeEmpty() {
        return isEmpty() && getChildren().values().stream().allMatch(ANode::isSubtreeEmpty);
    }
}
