/*
 * Copyright 2005-2012 Roger Kapsi
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
 * A {@link KeyAnalyzer} for {@link Integer}s.
 */
public class IntegerKeyAnalyzer extends AbstractKeyAnalyzer<Integer> implements Serializable {
  
  private static final long serialVersionUID = 8805465126366464399L;

  public static final IntegerKeyAnalyzer INSTANCE = new IntegerKeyAnalyzer();

  /**
   * A bit mask where the first bit is 1 and the others are zero
   */
  private static final int MSB = 1 << Integer.SIZE-1;
  
  /**
   * Returns a bit mask where the given bit is set
   */
  private static int mask(int bit) {
    return MSB >>> bit;
  }
  
  @Override
  public int lengthInBits(Integer key) {
    return Integer.SIZE;
  }

  @Override
  public boolean isBitSet(Integer key, int bitIndex) {
    return (key & mask(bitIndex)) != 0;
  }

  @Override
  public int bitIndex(Integer key, Integer otherKey) {
    int keyValue = key.intValue();
    if (keyValue == 0) {
      return NULL_BIT_KEY;
    }

    int otherValue = otherKey.intValue();
    
    if (keyValue != otherValue) {
      int xorValue = keyValue ^ otherValue;
      for (int i = 0; i < Integer.SIZE; i++) {
        if ((xorValue & mask(i)) != 0) {
          return i;
        }
      }
    }
    
    return KeyAnalyzer.EQUAL_BIT_KEY;
  }

  @Override
  public boolean isPrefix(Integer key, Integer prefix) {
    return key.equals(prefix);
  }
}
