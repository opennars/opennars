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

import java.io.Serializable;

/**
 * An implementation of {@link KeyAnalyzer} that assumes all keys
 * have the {@link Key} interface implemented.
 */
public class DefaultKeyAnalyzer<K extends Key<K>> 
    extends AbstractKeyAnalyzer<K> implements Serializable {
  
  private static final long serialVersionUID = 5340568481346940964L;
  
  @SuppressWarnings("rawtypes")
  private static final DefaultKeyAnalyzer INSTANCE = new DefaultKeyAnalyzer();
  
  @SuppressWarnings("unchecked")
  public static <K> KeyAnalyzer<K> singleton() {
    return (KeyAnalyzer<K>)INSTANCE;
  }
  
  @Override
  public int lengthInBits(K key) {
    return key.lengthInBits();
  }

  @Override
  public boolean isBitSet(K key, int bitIndex) {
    return key.isBitSet(bitIndex);
  }
  
  @Override
  public int bitIndex(K key, K otherKey) {
    return key.bitIndex(otherKey);
  }

  @Override
  public boolean isPrefix(K key, K prefix) {
    return key.isPrefixedBy(prefix);
  }
}
