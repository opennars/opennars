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
 * A {@link KeyAnalyzer} for {@link Long}s
 */
public class LongKeyAnalyzer extends AbstractKeyAnalyzer<Long> implements Serializable {
  
  private static final long serialVersionUID = -7611788114037795486L;

  /**
   * A singleton instance of {@link LongKeyAnalyzer}
   */
  public static final LongKeyAnalyzer INSTANCE = new LongKeyAnalyzer();
  
  /**
   * A bit mask where the first bit is 1 and the others are zero
   */
  private static final long MSB = 1L << Long.SIZE-1;
  
  /**
   * Returns a bit mask where the given bit is set
   */
  private static long mask(int bit) {
    return MSB >>> bit;
  }
  
  @Override
  public int lengthInBits(Long key) {
    return Long.SIZE;
  }

  @Override
  public boolean isBitSet(Long key, int bitIndex) {
    return (key & mask(bitIndex)) != 0;
  }

  @Override
  public int bitIndex(Long key, Long otherKey) {
    long keyValue = key.longValue();
    if (keyValue == 0) {
      return NULL_BIT_KEY;
    }

    long otherValue = otherKey.longValue();
    
    if (keyValue != otherValue) {
      long xorValue = keyValue ^ otherValue;
      for (int i = 0; i < Long.SIZE; i++) {
        if ((xorValue & mask(i)) != 0) {
          return i;
        }
      }
    }
    
    return KeyAnalyzer.EQUAL_BIT_KEY;
  }

  @Override
  public boolean isPrefix(Long key, Long prefix) {
    return key.equals(prefix);
  }
}
