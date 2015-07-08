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
 * A {@link KeyAnalyzer} for {@link Byte}s
 */
public class ByteKeyAnalyzer extends AbstractKeyAnalyzer<Byte> implements Serializable {
  
  private static final long serialVersionUID = -5294514513354687850L;

  /**
   * A singleton instance of {@link ByteKeyAnalyzer}
   */
  public static final ByteKeyAnalyzer INSTANCE = new ByteKeyAnalyzer();
  
  /**
   * A bit mask where the first bit is 1 and the others are zero
   */
  private static final int MSB = 1 << Byte.SIZE-1;
  
  /**
   * Returns a bit mask where the given bit is set
   */
  private static int mask(int bit) {
    return MSB >>> bit;
  }
  
  @Override
  public int lengthInBits(Byte key) {
    return Byte.SIZE;
  }

  @Override
  public boolean isBitSet(Byte key, int bitIndex) {
    return (key & mask(bitIndex)) != 0;
  }

  @Override
  public int bitIndex(Byte key, Byte otherKey) {
    byte keyValue = key.byteValue();
    if (keyValue == 0) {
      return NULL_BIT_KEY;
    }

    byte otherValue = otherKey.byteValue();
    
    if (keyValue != otherValue) {
      int xorValue = keyValue ^ otherValue;
      for (int i = 0; i < Byte.SIZE; i++) {
        if ((xorValue & mask(i)) != 0) {
          return i;
        }
      }
    }
    
    return KeyAnalyzer.EQUAL_BIT_KEY;
  }

  @Override
  public boolean isPrefix(Byte key, Byte prefix) {
    return key.equals(prefix);
  }
}