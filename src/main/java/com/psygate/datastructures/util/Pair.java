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
package com.psygate.datastructures.util;

import java.util.Map;
import java.util.Objects;

/**
 * A simple Map.Entry implementation.
 *
 * @author psygate (https://github.com/psygate)
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class Pair<K, V> implements Map.Entry<K, V> {

    private final K key;
    private V value;

    /**
     *
     * @param key Key for the new key -&gt; value association.
     * @param value Value for the new key -&gt; value association.
     */
    public Pair(K key, V value) {
        this.key = Objects.requireNonNull(key);
        this.value = value;
    }

    /**
     *
     * @param pair Copies key and value from the provided pair.
     */
    public Pair(Pair<K, V> pair) {
        this(Objects.requireNonNull(pair.key), pair.getValue());
    }

    /**
     *
     * @param entry Copies key and value from the provided entry.
     */
    public Pair(Map.Entry<K, V> entry) {
        this(Objects.requireNonNull(entry.getKey()), entry.getValue());
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V v = this.value;
        this.value = value;
        return v;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.key);
        hash = 29 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
