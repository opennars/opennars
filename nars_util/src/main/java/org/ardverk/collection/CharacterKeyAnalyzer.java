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
 * A {@link KeyAnalyzer} for {@link Character}s.
 */
public class CharacterKeyAnalyzer extends AbstractKeyAnalyzer<Character> 
    implements Serializable {
  
  private static final long serialVersionUID = 5267330596735811400L;

  /**
   * A {@link CharacterKeyAnalyzer} that uses all bits (16) of a {@code char}.
   */
  public static final CharacterKeyAnalyzer CHAR = new CharacterKeyAnalyzer(Character.SIZE);

  /**
   * A {@link CharacterKeyAnalyzer} that uses only the lower 8bits of a {@code char}.
   */
  public static final CharacterKeyAnalyzer BYTE = new CharacterKeyAnalyzer(Byte.SIZE);
  
  @Deprecated
  public static final CharacterKeyAnalyzer INSTANCE = CHAR;
  
  private final int size;
  
  private final int msb;
  
  private CharacterKeyAnalyzer(int size) {
    this(size, 1 << size-1);
  }
  
  private CharacterKeyAnalyzer(int size, int msb) {
    this.size = size;
    this.msb = msb;
  }
  
  /**
   * Returns a bit mask where the given bit is set
   */
  private int mask(int bit) {
    return msb >>> bit;
  }
  
  private char valueOf(Character ch) {
    char value = ch.charValue();
    if (size == Byte.SIZE) {
      value &= 0xFF;
    }
    return value;
  }
  
  @Override
  public int lengthInBits(Character key) {
    return size;
  }

  @Override
  public boolean isBitSet(Character key, int bitIndex) {
    return (key & mask(bitIndex)) != 0;
  }

  @Override
  public int bitIndex(Character key, Character otherKey) {
    char ch1 = valueOf(key);
    char ch2 = valueOf(otherKey);
    
    if (ch1 == 0) {
      return NULL_BIT_KEY;
    }
    
    if (ch1 != ch2) {
      int xor = ch1 ^ ch2;
      for (int i = 0; i < size; i++) {
        if ((xor & mask(i)) != 0) {
          return i;
        }
      }
    }
    
    return KeyAnalyzer.EQUAL_BIT_KEY;
  }
  
  @Override
  public boolean isPrefix(Character key, Character prefix) {
    return key.equals(prefix);
  }
}
