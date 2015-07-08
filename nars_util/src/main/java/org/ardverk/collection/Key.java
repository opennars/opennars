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

/**
 * An interface that {@link PatriciaTrie} keys may implement.
 * 
 * @see KeyAnalyzer
 * @see DefaultKeyAnalyzer
 */
public interface Key<K> {
  
  /**
   * Returns the key's length in bits.
   */
  public int lengthInBits();
  
  /**
   * Returns {@code true} if the given bit is set.
   */
  public boolean isBitSet(int bitIndex);
  
  /**
   * Returns the index of the first bit that is different in the two keys.
   */
  public int bitIndex(K otherKey);
  
  /**
   * Returns {@code true} if this key is prefixed by the given key.
   */
  public boolean isPrefixedBy(K prefix);
}
