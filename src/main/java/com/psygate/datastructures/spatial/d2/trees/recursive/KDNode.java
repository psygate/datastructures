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
import com.psygate.datastructures.spatial.d2.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.psygate.datastructures.spatial.d2.IDOrderable;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class KDNode<K extends IDOrderable, V> extends ANode<K, V, KDNode<K, V>, KDNode.Subdivision> {

    enum Subdivision {
        LEFT, RIGHT
    };
    private final Axis splitplane;
    private final double median;

    KDNode(List<Pair<K, V>> values, int maxNodeSize) {
        this(maxNodeSize, Axis.X, 0);
        values.forEach((v) -> super.add(v));
    }

    KDNode(int maxNodeSize) {
        this(maxNodeSize, Axis.X, 0);
    }

    public KDNode(int maxNodeSize, Axis splitplane) {
        this(maxNodeSize, splitplane, 0);
    }

    public KDNode(int maxNodeSize, Axis splitplane, double median) {
        super(new ArrayList<>(maxNodeSize), maxNodeSize);
        this.splitplane = splitplane;
        this.median = median;
    }

    @Override
    void add(Pair<K, V> value) {
        KDNode<K, V> child = getChild(value.getKey());
        if (isSplit()) {
            if (child == this) {
                super.add(value);
            } else {
                child.add(value);
            }
        } else {
            super.add(value);
        }
    }

    KDNode<K, V> getChild(K key) {
        if (isSplit()) {
            if (key.leftOf(median, splitplane)) {
                if (!hasChild(Subdivision.LEFT)) {
                    setChild(Subdivision.LEFT, new KDNode<>(getMaxNodeSize()));
                }

                return getChild(Subdivision.LEFT);
            } else if (key.rightOf(median, splitplane)) {
                if (!hasChild(Subdivision.RIGHT)) {
                    setChild(Subdivision.RIGHT, new KDNode<>(getMaxNodeSize()));
                }

                return getChild(Subdivision.RIGHT);
            }
        }

        return this;
    }

    @Override
    void split() {
        setSplit(true);
        int size = getValues().size();
        List<Pair<K, V>> values = getValuesCopy();
        clearValues();
        values.forEach((v) -> add(v));
        assert subtreeValueCount() == size;
    }
}
