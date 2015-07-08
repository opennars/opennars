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
 * A {@link KeyAnalyzer} for {@link Short}s
 */
public class ShortKeyAnalyzer extends AbstractKeyAnalyzer<Short> implements Serializable {
  
  private static final long serialVersionUID = 5263816158638832817L;

  /**
   * A singleton instance of {@link ShortKeyAnalyzer}
   */
  public static final ShortKeyAnalyzer INSTANCE = new ShortKeyAnalyzer();
  
  /**
   * A bit mask where the first bit is 1 and the others are zero
   */
  private static final int MSB = 1 << Short.SIZE-1;
  
  /**
   * Returns a bit mask where the given bit is set
   */
  private static int mask(int bit) {
    return MSB >>> bit;
  }
  
  @Override
  public int lengthInBits(Short key) {
    return Byte.SIZE;
  }

  @Override
  public boolean isBitSet(Short key, int bitIndex) {
    return (key & mask(bitIndex)) != 0;
  }

  @Override
  public int bitIndex(Short key, Short otherKey) {
    short keyValue = key.shortValue();
    if (keyValue == 0) {
      return NULL_BIT_KEY;
    }

    short otherValue = otherKey.shortValue();
    
    if (keyValue != otherValue) {
      int xorValue = keyValue ^ otherValue;
      for (int i = 0; i < Short.SIZE; i++) {
        if ((xorValue & mask(i)) != 0) {
          return i;
        }
      }
    }
    
    return KeyAnalyzer.EQUAL_BIT_KEY;
  }

  @Override
  public boolean isPrefix(Short key, Short prefix) {
    return key.equals(prefix);
  }
}