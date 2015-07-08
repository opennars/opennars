/*
 * Copyright 2005-2012 Roger Kapsi, Sam Berlin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.collection;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

/**
 * This class provides some basic {@link Trie} functionality and
 * utility methods for actual {@link Trie} implementations.
 */
abstract class AbstractTrie<K, V> extends AbstractMap<K, V>
        implements Serializable, Trie<K, V> {

    private static final long serialVersionUID = -6358111100045408883L;

    /**
     * The {@link KeyAnalyzer} that's being used to build the
     * PATRICIA {@link Trie}
     */
    protected final KeyAnalyzer<? super K> keyAnalyzer;

    public AbstractTrie() {
        this(DefaultKeyAnalyzer.singleton());
    }

    /**
     * Constructs a new {@link Trie} using the given {@link KeyAnalyzer}
     */
    public AbstractTrie(KeyAnalyzer<? super K> keyAnalyzer) {
        this.keyAnalyzer = Tries.notNull(keyAnalyzer, "keyAnalyzer");
    }

    /**
     * Returns the {@link KeyAnalyzer} that constructed the {@link Trie}.
     */
    public KeyAnalyzer<? super K> getKeyAnalyzer() {
        return keyAnalyzer;
    }

    @Override
    public K selectKey(K key) {
        Map.Entry<K, V> entry = select(key);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public V selectValue(K key) {
        Map.Entry<K, V> entry = select(key);
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Trie[").append(size()).append("]={\n");
        for (Map.Entry<K, V> entry : entrySet()) {
            buffer.append("  ").append(entry).append('\n');
        }
        buffer.append("}\n");
        return buffer.toString();
    }

    public void print(PrintStream out) {
        for (Map.Entry<K, V> entry : entrySet()) {
            out.println(entry);
        }

    }
    /**
     * Returns the length of the given key in bits
     *
     * @see KeyAnalyzer#lengthInBits(Object)
     */
    final int lengthInBits(K key) {
        if (key == null) {
            return 0;
        }

        return keyAnalyzer.lengthInBits(key);
    }

    /**
     * Returns whether or not the given bit on the
     * key is set or false if the key is null.
     *
     * @see KeyAnalyzer#isBitSet(Object, int)
     */
    final boolean isBitSet(K key, int bitIndex) {
        if (key == null) { // root's might be null!
            return false;
        }
        return keyAnalyzer.isBitSet(key, bitIndex);
    }

    /**
     * Utility method for calling {@link KeyAnalyzer#bitIndex(Object, Object)}
     */
    final int bitIndex(K key, K otherKey) {
        if (key != null && otherKey != null) {
            return keyAnalyzer.bitIndex(key, otherKey);
        } else if (key != null && otherKey == null) {
            return bitIndex(key);
        } else if (key == null && otherKey != null) {
            return bitIndex(otherKey);
        }

        return KeyAnalyzer.NULL_BIT_KEY;
    }

    private int bitIndex(K key) {
        int lengthInBits = lengthInBits(key);
        for (int i = 0; i < lengthInBits; i++) {
            if (isBitSet(key, i)) {
                return i;
            }
        }

        return KeyAnalyzer.NULL_BIT_KEY;
    }

    /**
     * An utility method for calling {@link KeyAnalyzer#compare(Object, Object)}
     */
    final boolean compareKeys(K key, K other) {
        if (key == null) {
            return (other == null);
        } else if (other == null) {
            return (key == null);
        }

        return keyAnalyzer.compare(key, other) == 0;
    }

    /**
     * A basic implementation of {@link Entry}
     */
    abstract static class BasicEntry<K, V> implements Map.Entry<K, V>, Serializable {

        private static final long serialVersionUID = -944364551314110330L;

        protected K key;

        protected V value;

        private transient int hashCode = 0;

        public BasicEntry(K key) {
            this.key = key;
        }

        public BasicEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Replaces the current key and value with the provided
         * key &amp; value
         */
        public V setKeyValue(K key, V value) {
            this.key = key;
            this.hashCode = 0;
            return setValue(value);
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V previous = this.value;
            this.value = value;
            return previous;
        }

        @Override
        public int hashCode() {

            if (hashCode == 0) {
                hashCode = (key != null ? key.hashCode() : 0);
            }

            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Map.Entry<?, ?>)) {
                return false;
            }

            Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
            if (Tries.areEqual(key, other.getKey())
                    && Tries.areEqual(value, other.getValue())) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
