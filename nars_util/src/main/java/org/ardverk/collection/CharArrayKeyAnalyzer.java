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

import java.io.Serializable;

/**
 * An {@link KeyAnalyzer} for {@code char[]}s
 */
public class CharArrayKeyAnalyzer extends AbstractKeyAnalyzer<char[]> 
    implements Serializable {
  
  private static final long serialVersionUID = 683256578013018792L;

  /**
   * A {@link CharArrayKeyAnalyzer} that uses all bits (16) of a {@code char}.
   */
  public static final CharArrayKeyAnalyzer CHAR = new CharArrayKeyAnalyzer(Character.SIZE);
  
  /**
   * A {@link CharArrayKeyAnalyzer} that uses only the lower 8 bits of a {@code char}.
   */
  public static final CharArrayKeyAnalyzer BYTE = new CharArrayKeyAnalyzer(Byte.SIZE);
  
  @Deprecated
  public static final CharArrayKeyAnalyzer INSTANCE = CHAR;
  
  private final int size;
  
  private final int msb;
  
  protected CharArrayKeyAnalyzer(int size) {
    this(size, 1<<size-1);
  }
  
  protected CharArrayKeyAnalyzer(int size, int msb) {
    this.size = size;
    this.msb = msb;
  }
  
  @Override
  public int compare(char[] o1, char[] o2) {
    if (o1 == null) {
      return (o2 == null) ? 0 : -1;
    } else if (o2 == null) {
      return (o1 == null) ? 0 : 1;
    }
    
    if (o1.length != o2.length) {
      return o1.length - o2.length;
    }
    
    for (int i = 0; i < o1.length; i++) {
      int diff = (o1[i] & 0xFF) - (o2[i] & 0xFF);
      if (diff != 0) {
        return diff;
      }
    }

    return 0;
  }

  @Override
  public int lengthInBits(char[] key) {
    return key.length * size;
  }

  @Override
  public boolean isBitSet(char[] key, int bitIndex) {
    if (bitIndex >= lengthInBits(key)) {
      return false;
    }
    
    int index = (int)(bitIndex / size);
    int bit = (int)(bitIndex % size);
    return (key[index] & mask(bit)) != 0;
  }

  @Override
  public int bitIndex(char[] key, char[] otherKey) {
    
    int length = Math.max(key.length, otherKey.length);
    
    boolean allNull = true;
    for (int i = 0; i < length; i++) {
      char ch1 = valueAt(key, i);
      char ch2 = valueAt(otherKey, i);
        
      if (ch1 != ch2) {
        int xor = ch1 ^ ch2;
        for (int j = 0; j < size; j++) {
          if ((xor & mask(j)) != 0) {
            return (i * size) + j;
          }
        }
      }
      
      if (ch1 != 0) {
        allNull = false;
      }
    }
    
    if (allNull) {
      return KeyAnalyzer.NULL_BIT_KEY;
    }
    
    return KeyAnalyzer.EQUAL_BIT_KEY;
  }
  
  @Override
  public boolean isPrefix(char[] key, char[] prefix) {
    if (key.length < prefix.length) {
      return false;
    }
    
    for (int i = 0; i < prefix.length; i++) {
      if (key[i] != prefix[i]) {
        return false;
      }
    }
    
    return true;
  }

  /**
   * Returns a bit mask where the given bit is set
   */
  private int mask(int bit) {
    return msb >>> bit;
  }
  
  private char valueAt(char[] values, int index) {
    if (index < values.length) {
      char value = values[index];
      if (size == Byte.SIZE) {
        value &= 0xFF;
      }
      return value;
    }
    return 0;
  }
}