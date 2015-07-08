package org.ardverk.collection;

import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

public class ByteArrayKeyAnalyzerTest {

  private static final int SIZE = 20000;
  
  @Test
  public void bitSet() {
    byte[] key = toByteArray("10100110", 2);
    ByteArrayKeyAnalyzer ka = ByteArrayKeyAnalyzer.create(key.length);
    
    TestCase.assertTrue(ka.isBitSet(key, 0));
    TestCase.assertFalse(ka.isBitSet(key, 1));
    TestCase.assertTrue(ka.isBitSet(key, 2));
    TestCase.assertFalse(ka.isBitSet(key, 3));
    TestCase.assertFalse(ka.isBitSet(key, 4));
    TestCase.assertTrue(ka.isBitSet(key, 5));
    TestCase.assertTrue(ka.isBitSet(key, 6));
    TestCase.assertFalse(ka.isBitSet(key, 7));
  }
  
  @Test
  public void keys() {
    PatriciaTrie<byte[], BigInteger> trie
      = new PatriciaTrie<byte[], BigInteger>(
          ByteArrayKeyAnalyzer.CONSTANT);
    
    Map<byte[], BigInteger> map 
      = new TreeMap<byte[], BigInteger>(
          ByteArrayKeyAnalyzer.CONSTANT);
    
    for (int i = 0; i < SIZE; i++) {
      BigInteger value = BigInteger.valueOf(i);
      byte[] key = toByteArray(value);
      
      BigInteger existing = trie.put(key, value);
      TestCase.assertNull(existing);
      
      map.put(key, value);
    }
    
    TestCase.assertEquals(map.size(), trie.size());
    
    for (byte[] key : map.keySet()) {
      BigInteger expected = new BigInteger(1, key);
      BigInteger value = trie.get(key);
      
      TestCase.assertEquals(expected, value);
    }
  }
  
  private static byte[] toByteArray(String value, int radix) {
    return toByteArray(Long.parseLong(value, radix));
  }
  
  private static byte[] toByteArray(long value) {
    return toByteArray(BigInteger.valueOf(value));
  }
  
  private static byte[] toByteArray(BigInteger value) {
    byte[] src = value.toByteArray();
    if (src.length <= 1) {
      return src;
    }
    
    if (src[0] != 0) {
      return src;
    }
    
    byte[] dst = new byte[src.length-1];
    System.arraycopy(src, 1, dst, 0, dst.length);
    return dst;
  }
  
  @Test
  public void variableLength() {
    Trie<String, String> trie1 
      = new PatriciaTrie<String, String>(
          StringKeyAnalyzer.CHAR);
    
    trie1.put("Hello", "Hello");
    trie1.put("World", "World");
    trie1.put("Alfred", "Alfred");
    trie1.put("Anna", "Anna");
    trie1.put("Anna-Marie", "Anna-Marie");
    
    Trie<byte[], String> trie2 
      = new PatriciaTrie<byte[], String>(
          ByteArrayKeyAnalyzer.VARIABLE);
    
    for (Entry<String, String> entry : trie1.entrySet()) {
      byte[] key = entry.getKey().getBytes();
      String value = entry.getValue();
      
      trie2.put(key, value);
    }
    
    TestCase.assertEquals("Anna", trie1.selectValue("An"));
    TestCase.assertEquals("Anna", trie2.selectValue("An".getBytes()));
    
    TestCase.assertEquals("Anna-Marie", trie1.selectValue("Anna-"));
    TestCase.assertEquals("Anna-Marie", trie2.selectValue("Anna-".getBytes()));
    
    TestCase.assertEquals("World", trie1.selectValue("x"));
    TestCase.assertEquals("World", trie2.selectValue("x".getBytes()));
  }
}
