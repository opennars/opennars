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
 * A {@link KeyAnalyzer} for {@code byte[]}s
 */
public abstract class ByteArrayKeyAnalyzer extends AbstractKeyAnalyzer<byte[]> 
    implements Serializable {
  
  private static final long serialVersionUID = 6496047734419335722L;

  /**
   * A {@link ByteArrayKeyAnalyzer} for constant length {@code byte[]}.
   */
  public static final ByteArrayKeyAnalyzer CONSTANT = new Constant();
  
  /**
   * A {@link ByteArrayKeyAnalyzer} for variable length {@code byte[]}.
   */
  public static final ByteArrayKeyAnalyzer VARIABLE = new Variable();
  
  @Deprecated
  public static final ByteArrayKeyAnalyzer INSTANCE = CONSTANT;
  
  /**
   * A bit mask where the first bit is 1 and the others are zero
   */
  private static final int MSB = 1 << Byte.SIZE-1;
  
  /**
   * Creates and returns a {@link ByteArrayKeyAnalyzer} for 
   * fixed-length keys. The maximum length of a key is defined
   * in bytes.
   */
  public static ByteArrayKeyAnalyzer create(int maxLength) {
    return new Constant(maxLength);
  }
  
  @Override
  public int compare(byte[] o1, byte[] o2) {
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
  public int lengthInBits(byte[] key) {
    return key.length * Byte.SIZE;
  }
  
  @Override
  public boolean isPrefix(byte[] key, byte[] prefix) {
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
  private static int mask(int bit) {
    return MSB >>> bit;
  }
  
  /**
   * Returns the {@code byte} value at the given index.
   */
  private static byte valueAt(byte[] values, int index) {
    if (index >= 0 && index < values.length) {
      return values[index];
    }
    return 0;
  }
  
  /**
   * A {@link ByteArrayKeyAnalyzer} for variable length {@code byte[]}.
   */
  private static class Variable extends ByteArrayKeyAnalyzer {
    
    private static final long serialVersionUID = 5360165640553653434L;

    @Override
    public boolean isBitSet(byte[] key, int bitIndex) {
      if (bitIndex >= lengthInBits(key)) {
        return false;
      }
      
      int index = (int)(bitIndex / Byte.SIZE);
      int bit = (int)(bitIndex % Byte.SIZE);
      return (key[index] & mask(bit)) != 0;
    }

    @Override
    public int bitIndex(byte[] key, byte[] otherKey) {
      
      int length = Math.max(key.length, otherKey.length);
      
      boolean allNull = true;
      for (int i = 0; i < length; i++) {
        byte b1 = valueAt(key, i);
        byte b2 = valueAt(otherKey, i);
        
        if (b1 != b2) {
          int xor = b1 ^ b2;
          for (int j = 0; j < Byte.SIZE; j++) {
            if ((xor & mask(j)) != 0) {
              return (i * Byte.SIZE) + j;
            }
          }
        }
         
        if (b1 != 0) {
          allNull = false;
        }
      }
      
      if (allNull) {
        return KeyAnalyzer.NULL_BIT_KEY;
      }
      
      return KeyAnalyzer.EQUAL_BIT_KEY;
    }
  }
  
  /**
   * A {@link ByteArrayKeyAnalyzer} for constant length {@code byte[]}.
   */
  private static class Constant extends ByteArrayKeyAnalyzer {
    
    private static final long serialVersionUID = 6464528643075848768L;

    private static final int DEFAULT_LENGTH = Integer.MAX_VALUE / Byte.SIZE;
    
    private final int maxLength;
    
    public Constant() {
      this(DEFAULT_LENGTH);
    }
    
    public Constant(int maxLength) {
      this.maxLength = maxLength;
    }
    
    @Override
    public boolean isBitSet(byte[] key, int bitIndex) {
      
      if (maxLength < key.length) {
        throw new IllegalArgumentException();
      }
      
      int lengthInBits = lengthInBits(key);
      int prefix = (maxLength * Byte.SIZE) - lengthInBits;
      int keyBitIndex = bitIndex - prefix;
      
      if (keyBitIndex >= lengthInBits || keyBitIndex < 0) {
        return false;
      }
      
      int index = (int)(keyBitIndex / Byte.SIZE);
      int bit = (int)(keyBitIndex % Byte.SIZE);
      return (key[index] & mask(bit)) != 0;
    }

    @Override
    public int bitIndex(byte[] key, byte[] otherKey) {
      
      // NOTE: We don't have to check the otherKey because
      // it's a key that is already in the Trie. It has 
      // already passed this test when it was added to the 
      // Trie back in the days...
      if (maxLength < key.length) {
        throw new IllegalArgumentException();
      }
      
      boolean allNull = true;
      int length = Math.max(key.length, otherKey.length);
      int prefixBits = (maxLength - length) * Byte.SIZE;
      
      for (int i = 0; i < length; i++) {
        byte b1 = valueAt(key, key.length - length + i);
        byte b2 = valueAt(otherKey, otherKey.length - length + i);
        
        if (b1 != b2) {
          int xor = b1 ^ b2;
          for (int j = 0; j < Byte.SIZE; j++) {
            if ((xor & mask(j)) != 0) {
              return prefixBits + (i * Byte.SIZE) + j;
            }
          }
        }
         
        if (b1 != 0) {
          allNull = false;
        }
      }
      
      if (allNull) {
        return KeyAnalyzer.NULL_BIT_KEY;
      }
      
      return KeyAnalyzer.EQUAL_BIT_KEY;
    }
  }
}
