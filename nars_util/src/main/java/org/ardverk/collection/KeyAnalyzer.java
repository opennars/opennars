/*
 * Copyright 2010 Roger Kapsi
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

import java.util.Comparator;

/**
 * The {@link KeyAnalyzer} provides bit-level access to keys
 * for the {@link PatriciaTrie}.
 */
public interface KeyAnalyzer<K> extends Comparator<K> {

  /**
   * Returned by {@link #bitIndex(Object, Object)} if a key's
   * bits were all zero (0).
   */
  public static final int NULL_BIT_KEY = -1;
  
  /** 
   * Returned by {@link #bitIndex(Object, Object)} if a the
   * bits of two keys were all equal.
   */
  public static final int EQUAL_BIT_KEY = -2;
  
  /**
   * Returned by {@link #bitIndex(Object, Object)} if a keys 
   * indices are out of bounds.
   */
  public static final int OUT_OF_BOUNDS_BIT_KEY = -3;
  
  /**
   * Returns the key's length in bits.
   */
  public int lengthInBits(K key);
  
  /**
   * Returns {@code true} if a key's bit it set at the given index.
   */
  public boolean isBitSet(K key, int bitIndex);
  
  /**
   * Returns the index of the first bit that is different in the two keys.
   */
  public int bitIndex(K key, K otherKey);
  
  /**
   * Returns {@code true} if the second argument is a 
   * prefix of the first argument.
   */
  public boolean isPrefix(K key, K prefix);
}
