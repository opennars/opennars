/*
 * Copyright 2005-2008 Roger Kapsi, Sam Berlin
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

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class PatriciaTrieTest {
  
  @Test
  public void testSimple() {
    PatriciaTrie<Integer, String> intTrie = new PatriciaTrie<Integer, String>(IntegerKeyAnalyzer.INSTANCE);
    TestCase.assertTrue(intTrie.isEmpty());
    TestCase.assertEquals(0, intTrie.size());
    
    intTrie.put(1, "One");
    TestCase.assertFalse(intTrie.isEmpty());
    TestCase.assertEquals(1, intTrie.size());
    
    TestCase.assertEquals("One", intTrie.remove(1));
    TestCase.assertNull(intTrie.remove(1));
    TestCase.assertTrue(intTrie.isEmpty());
    TestCase.assertEquals(0, intTrie.size());
    
    intTrie.put(1, "One");
    TestCase.assertEquals("One", intTrie.get(1));
    TestCase.assertEquals("One", intTrie.put(1, "NotOne"));
    TestCase.assertEquals(1, intTrie.size());
    TestCase.assertEquals("NotOne", intTrie.get(1));
    TestCase.assertEquals("NotOne", intTrie.remove(1));
    TestCase.assertNull(intTrie.put(1, "One"));
  }
  
  @Test
  public void testCeilingEntry() {
    PatriciaTrie<Character, String> charTrie 
      = new PatriciaTrie<Character, String>(CharacterKeyAnalyzer.CHAR);
    charTrie.put('c', "c");
    charTrie.put('p', "p");
    charTrie.put('l', "l");
    charTrie.put('t', "t");
    charTrie.put('k', "k");
    charTrie.put('a', "a");
    charTrie.put('y', "y");
    charTrie.put('r', "r");
    charTrie.put('u', "u");
    charTrie.put('o', "o");
    charTrie.put('w', "w");
    charTrie.put('i', "i");
    charTrie.put('e', "e");
    charTrie.put('x', "x");
    charTrie.put('q', "q");
    charTrie.put('b', "b");
    charTrie.put('j', "j");
    charTrie.put('s', "s");
    charTrie.put('n', "n");
    charTrie.put('v', "v");
    charTrie.put('g', "g");
    charTrie.put('h', "h");
    charTrie.put('m', "m");
    charTrie.put('z', "z");
    charTrie.put('f', "f");
    charTrie.put('d', "d");
    
    Object[] results = new Object[] {
      'a', "a", 'b', "b", 'c', "c", 'd', "d", 'e', "e",
      'f', "f", 'g', "g", 'h', "h", 'i', "i", 'j', "j",
      'k', "k", 'l', "l", 'm', "m", 'n', "n", 'o', "o",
      'p', "p", 'q', "q", 'r', "r", 's', "s", 't', "t",
      'u', "u", 'v', "v", 'w', "w", 'x', "x", 'y', "y", 
      'z', "z"
    };
    
    for(int i = 0; i < results.length; i++) {
      Map.Entry<Character, String> found = charTrie.ceilingEntry((Character)results[i]);
      TestCase.assertNotNull(found);
      TestCase.assertEquals(results[i], found.getKey());
      TestCase.assertEquals(results[++i], found.getValue());
    }
    
    // Remove some & try again...
    charTrie.remove('a');
    charTrie.remove('z');
    charTrie.remove('q');
    charTrie.remove('l');
    charTrie.remove('p');
    charTrie.remove('m');
    charTrie.remove('u');
    
    Map.Entry<Character, String> found = charTrie.ceilingEntry('u');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'v', found.getKey());
    
    found = charTrie.ceilingEntry('a');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'b', found.getKey());
    
    found = charTrie.ceilingEntry('z');
    TestCase.assertNull(found);
    
    found = charTrie.ceilingEntry('q');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'r', found.getKey());
    
    found = charTrie.ceilingEntry('l');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'n', found.getKey());
    
    found = charTrie.ceilingEntry('p');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'r', found.getKey());
    
    found = charTrie.ceilingEntry('m');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'n', found.getKey());
    
    found = charTrie.ceilingEntry('\0');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'b', found.getKey());
    
    charTrie.put('\0', "");
    found = charTrie.ceilingEntry('\0');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'\0', found.getKey());    
  }
  
  @Test
  public void testLowerEntry() {
    PatriciaTrie<Character, String> charTrie = new PatriciaTrie<Character, String>(CharacterKeyAnalyzer.CHAR);
    charTrie.put('c', "c");
    charTrie.put('p', "p");
    charTrie.put('l', "l");
    charTrie.put('t', "t");
    charTrie.put('k', "k");
    charTrie.put('a', "a");
    charTrie.put('y', "y");
    charTrie.put('r', "r");
    charTrie.put('u', "u");
    charTrie.put('o', "o");
    charTrie.put('w', "w");
    charTrie.put('i', "i");
    charTrie.put('e', "e");
    charTrie.put('x', "x");
    charTrie.put('q', "q");
    charTrie.put('b', "b");
    charTrie.put('j', "j");
    charTrie.put('s', "s");
    charTrie.put('n', "n");
    charTrie.put('v', "v");
    charTrie.put('g', "g");
    charTrie.put('h', "h");
    charTrie.put('m', "m");
    charTrie.put('z', "z");
    charTrie.put('f', "f");
    charTrie.put('d', "d");
    
    Object[] results = new Object[] {
      'a', "a", 'b', "b", 'c', "c", 'd', "d", 'e', "e",
      'f', "f", 'g', "g", 'h', "h", 'i', "i", 'j', "j",
      'k', "k", 'l', "l", 'm', "m", 'n', "n", 'o', "o",
      'p', "p", 'q', "q", 'r', "r", 's', "s", 't', "t",
      'u', "u", 'v', "v", 'w', "w", 'x', "x", 'y', "y", 
      'z', "z"
    };
    
    for(int i = 0; i < results.length; i+=2) {
      //System.out.println("Looking for: " + results[i]);
      Map.Entry<Character, String> found = charTrie.lowerEntry((Character)results[i]);
      if(i == 0) {
        TestCase.assertNull(found);
      } else {
        TestCase.assertNotNull(found);
        TestCase.assertEquals(results[i-2], found.getKey());
        TestCase.assertEquals(results[i-1], found.getValue());
      }
    }

    Map.Entry<Character, String> found = charTrie.lowerEntry((char)('z' + 1));
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'z', found.getKey());
    
    
    // Remove some & try again...
    charTrie.remove('a');
    charTrie.remove('z');
    charTrie.remove('q');
    charTrie.remove('l');
    charTrie.remove('p');
    charTrie.remove('m');
    charTrie.remove('u');
    
    found = charTrie.lowerEntry('u');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'t', found.getKey());
    
    found = charTrie.lowerEntry('v');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'t', found.getKey());
    
    found = charTrie.lowerEntry('a');
    TestCase.assertNull(found);
    
    found = charTrie.lowerEntry('z');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'y', found.getKey());
    
    found = charTrie.lowerEntry((char)('z'+1));
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'y', found.getKey());
    
    found = charTrie.lowerEntry('q');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'o', found.getKey());
    
    found = charTrie.lowerEntry('r');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'o', found.getKey());
    
    found = charTrie.lowerEntry('p');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'o', found.getKey());
    
    found = charTrie.lowerEntry('l');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'k', found.getKey());
    
    found = charTrie.lowerEntry('m');
    TestCase.assertNotNull(found);
    TestCase.assertEquals((Character)'k', found.getKey());
    
    found = charTrie.lowerEntry('\0');
    TestCase.assertNull(found);
    
    charTrie.put('\0', "");
    found = charTrie.lowerEntry('\0');
    TestCase.assertNull(found);    
  }
  
  @Test
  public void testIteration() {
    PatriciaTrie<Integer, String> intTrie = new PatriciaTrie<Integer, String>(IntegerKeyAnalyzer.INSTANCE);
    intTrie.put(1, "One");
    intTrie.put(5, "Five");
    intTrie.put(4, "Four");
    intTrie.put(2, "Two");
    intTrie.put(3, "Three");
    intTrie.put(15, "Fifteen");
    intTrie.put(13, "Thirteen");
    intTrie.put(14, "Fourteen");
    intTrie.put(16, "Sixteen");
    
    TestCursor cursor = new TestCursor(
        1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 13, "Thirteen",
        14, "Fourteen", 15, "Fifteen", 16, "Sixteen");

    cursor.starting();
    intTrie.traverse(cursor);
    cursor.finished();
    
    cursor.starting();
    for (Map.Entry<Integer, String> entry : intTrie.entrySet())
      cursor.select(entry);
    cursor.finished();
    
    cursor.starting();
    for (Integer integer : intTrie.keySet())
      cursor.checkKey(integer);
    cursor.finished();
    
    cursor.starting();
    for (String string : intTrie.values())
      cursor.checkValue(string);
    cursor.finished();

    PatriciaTrie<Character, String> charTrie = new PatriciaTrie<Character, String>(CharacterKeyAnalyzer.CHAR);
    charTrie.put('c', "c");
    charTrie.put('p', "p");
    charTrie.put('l', "l");
    charTrie.put('t', "t");
    charTrie.put('k', "k");
    charTrie.put('a', "a");
    charTrie.put('y', "y");
    charTrie.put('r', "r");
    charTrie.put('u', "u");
    charTrie.put('o', "o");
    charTrie.put('w', "w");
    charTrie.put('i', "i");
    charTrie.put('e', "e");
    charTrie.put('x', "x");
    charTrie.put('q', "q");
    charTrie.put('b', "b");
    charTrie.put('j', "j");
    charTrie.put('s', "s");
    charTrie.put('n', "n");
    charTrie.put('v', "v");
    charTrie.put('g', "g");
    charTrie.put('h', "h");
    charTrie.put('m', "m");
    charTrie.put('z', "z");
    charTrie.put('f', "f");
    charTrie.put('d', "d");
    cursor = new TestCursor('a', "a", 'b', "b", 'c', "c", 'd', "d", 'e', "e",
        'f', "f", 'g', "g", 'h', "h", 'i', "i", 'j', "j",
        'k', "k", 'l', "l", 'm', "m", 'n', "n", 'o', "o",
        'p', "p", 'q', "q", 'r', "r", 's', "s", 't', "t",
        'u', "u", 'v', "v", 'w', "w", 'x', "x", 'y', "y", 
        'z', "z");
    
    cursor.starting();
    charTrie.traverse(cursor);
    cursor.finished();

    cursor.starting();
    for (Map.Entry<Character, String> entry : charTrie.entrySet())
      cursor.select(entry);
    cursor.finished();
    
    cursor.starting();
    for (Character character : charTrie.keySet())
      cursor.checkKey(character);
    cursor.finished();
    
    cursor.starting();
    for (String string : charTrie.values())
      cursor.checkValue(string);
    cursor.finished();
  }
  
  @Test
  public void testSelect() {
    PatriciaTrie<Character, String> charTrie = new PatriciaTrie<Character, String>(CharacterKeyAnalyzer.CHAR);
    charTrie.put('c', "c");
    charTrie.put('p', "p");
    charTrie.put('l', "l");
    charTrie.put('t', "t");
    charTrie.put('k', "k");
    charTrie.put('a', "a");
    charTrie.put('y', "y");
    charTrie.put('r', "r");
    charTrie.put('u', "u");
    charTrie.put('o', "o");
    charTrie.put('w', "w");
    charTrie.put('i', "i");
    charTrie.put('e', "e");
    charTrie.put('x', "x");
    charTrie.put('q', "q");
    charTrie.put('b', "b");
    charTrie.put('j', "j");
    charTrie.put('s', "s");
    charTrie.put('n', "n");
    charTrie.put('v', "v");
    charTrie.put('g', "g");
    charTrie.put('h', "h");
    charTrie.put('m', "m");
    charTrie.put('z', "z");
    charTrie.put('f', "f");
    charTrie.put('d', "d");
    TestCursor cursor = new TestCursor(
        'd', "d", 'e', "e", 'f', "f", 'g', "g",
        'a', "a", 'b', "b", 'c', "c",  
        'l', "l", 'm', "m", 'n', "n", 'o', "o",
        'h', "h", 'i', "i", 'j', "j", 'k', "k", 
        't', "t", 'u', "u", 'v', "v", 'w', "w",
        'p', "p", 'q', "q", 'r', "r", 's', "s", 
        'x', "x", 'y', "y", 'z', "z");
        
    TestCase.assertEquals(26, charTrie.size());
    
    cursor.starting();
    charTrie.select('d', cursor);
    cursor.finished();
  }
  
  @Test
  public void testTraverseCursorRemove() {
    PatriciaTrie<Character, String> charTrie = new PatriciaTrie<Character, String>(CharacterKeyAnalyzer.CHAR);
    charTrie.put('c', "c");
    charTrie.put('p', "p");
    charTrie.put('l', "l");
    charTrie.put('t', "t");
    charTrie.put('k', "k");
    charTrie.put('a', "a");
    charTrie.put('y', "y");
    charTrie.put('r', "r");
    charTrie.put('u', "u");
    charTrie.put('o', "o");
    charTrie.put('w', "w");
    charTrie.put('i', "i");
    charTrie.put('e', "e");
    charTrie.put('x', "x");
    charTrie.put('q', "q");
    charTrie.put('b', "b");
    charTrie.put('j', "j");
    charTrie.put('s', "s");
    charTrie.put('n', "n");
    charTrie.put('v', "v");
    charTrie.put('g', "g");
    charTrie.put('h', "h");
    charTrie.put('m', "m");
    charTrie.put('z', "z");
    charTrie.put('f', "f");
    charTrie.put('d', "d");
    TestCursor cursor = new TestCursor('a', "a", 'b', "b", 'c', "c", 'd', "d", 'e', "e",
        'f', "f", 'g', "g", 'h', "h", 'i', "i", 'j', "j",
        'k', "k", 'l', "l", 'm', "m", 'n', "n", 'o', "o",
        'p', "p", 'q', "q", 'r', "r", 's', "s", 't', "t",
        'u', "u", 'v', "v", 'w', "w", 'x', "x", 'y', "y", 
        'z', "z");
    
    cursor.starting();
    charTrie.traverse(cursor);
    cursor.finished();
    
    // Test removing both an internal & external node.
    // 'm' is an example External node in this Trie, and 'p' is an internal.
    
    TestCase.assertEquals(26, charTrie.size());
    
    Object[] toRemove = new Object[] { 'g', 'd', 'e', 'm', 'p', 'q', 'r', 's' };
    cursor.addToRemove(toRemove);
    
    cursor.starting();
    charTrie.traverse(cursor);
    cursor.finished();
      
    TestCase.assertEquals(26 - toRemove.length, charTrie.size());

    cursor.starting();
    charTrie.traverse(cursor);
    cursor.finished();
    
    cursor.starting();
    for (Entry<Character, String> entry : charTrie.entrySet()) {
      cursor.select(entry);
      if (Arrays.asList(toRemove).contains(entry.getKey())) {
        TestCase.fail("got an: " + entry);
      }
    }
    cursor.finished();
  }
  
  @Test
  public void testIteratorRemove() {
    PatriciaTrie<Character, String> charTrie = new PatriciaTrie<Character, String>(CharacterKeyAnalyzer.CHAR);
    charTrie.put('c', "c");
    charTrie.put('p', "p");
    charTrie.put('l', "l");
    charTrie.put('t', "t");
    charTrie.put('k', "k");
    charTrie.put('a', "a");
    charTrie.put('y', "y");
    charTrie.put('r', "r");
    charTrie.put('u', "u");
    charTrie.put('o', "o");
    charTrie.put('w', "w");
    charTrie.put('i', "i");
    charTrie.put('e', "e");
    charTrie.put('x', "x");
    charTrie.put('q', "q");
    charTrie.put('b', "b");
    charTrie.put('j', "j");
    charTrie.put('s', "s");
    charTrie.put('n', "n");
    charTrie.put('v', "v");
    charTrie.put('g', "g");
    charTrie.put('h', "h");
    charTrie.put('m', "m");
    charTrie.put('z', "z");
    charTrie.put('f', "f");
    charTrie.put('d', "d");
    TestCursor cursor = new TestCursor('a', "a", 'b', "b", 'c', "c", 'd', "d", 'e', "e",
        'f', "f", 'g', "g", 'h', "h", 'i', "i", 'j', "j",
        'k', "k", 'l', "l", 'm', "m", 'n', "n", 'o', "o",
        'p', "p", 'q', "q", 'r', "r", 's', "s", 't', "t",
        'u', "u", 'v', "v", 'w', "w", 'x', "x", 'y', "y", 
        'z', "z");
    
    // Test removing both an internal & external node.
    // 'm' is an example External node in this Trie, and 'p' is an internal.
    
    TestCase.assertEquals(26, charTrie.size());
    
    Object[] toRemove = new Object[] { 'e', 'm', 'p', 'q', 'r', 's' };
    
    cursor.starting();
    for(Iterator<Map.Entry<Character, String>> i = charTrie.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry<Character,String> entry = i.next();
      cursor.select(entry);
      if(Arrays.asList(toRemove).contains(entry.getKey()))
        i.remove();      
    }
    cursor.finished();
      
    TestCase.assertEquals(26 - toRemove.length, charTrie.size());
    
    cursor.remove(toRemove);

    cursor.starting();
    for (Entry<Character, String> entry : charTrie.entrySet()) {
      cursor.select(entry);
      if (Arrays.asList(toRemove).contains(entry.getKey())) {
        TestCase.fail("got an: " + entry);
      }
    }
    cursor.finished();
  }
  
  @Test @Ignore
  public void testHamlet() throws Exception {
    // Make sure that Hamlet is read & stored in the same order as a SortedSet.
    List<String> original = new ArrayList<String>();
    List<String> control = new ArrayList<String>();
    SortedMap<String, String> sortedControl = new TreeMap<String, String>();
    PatriciaTrie<String, String> trie = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);
    
    InputStream in = getClass().getResourceAsStream("hamlet.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    
    String read = null;
    while( (read = reader.readLine()) != null) {
      StringTokenizer st = new StringTokenizer(read);
      while(st.hasMoreTokens()) {
        String token = st.nextToken();
        original.add(token);
        sortedControl.put(token, token);
        trie.put(token, token);
      }
    }
    control.addAll(sortedControl.values());

    TestCase.assertEquals(control.size(), sortedControl.size());
    TestCase.assertEquals(sortedControl.size(), trie.size());
    Iterator<String> iter = trie.values().iterator();
    for (String aControl : control) {
      TestCase.assertEquals(aControl, iter.next());
    }
    
    Random rnd = new Random();
    int item = 0;
    iter = trie.values().iterator();
    int removed = 0;
    for(; item < control.size(); item++) {
      TestCase.assertEquals(control.get(item), iter.next());
      if(rnd.nextBoolean()) {
        iter.remove();
        removed++;
      }
    }
    
    TestCase.assertEquals(control.size(), item);
    TestCase.assertTrue(removed > 0);
    TestCase.assertEquals(control.size(), trie.size() + removed);
    
    // reset hamlet
    trie.clear();
    for (String anOriginal : original) {
      trie.put(anOriginal, anOriginal);
    }
    
    assertEqualArrays(sortedControl.values().toArray(), trie.values().toArray());
    assertEqualArrays(sortedControl.keySet().toArray(), trie.keySet().toArray());
    assertEqualArrays(sortedControl.entrySet().toArray(), trie.entrySet().toArray());
    
    TestCase.assertEquals(sortedControl.firstKey(), trie.firstKey());
    TestCase.assertEquals(sortedControl.lastKey(), trie.lastKey());
    
    SortedMap<String, String> sub = trie.headMap(control.get(523));
    TestCase.assertEquals(523, sub.size());
    for(int i = 0; i < control.size(); i++) {
      if(i < 523)
        TestCase.assertTrue(sub.containsKey(control.get(i)));
      else
        TestCase.assertFalse(sub.containsKey(control.get(i)));
    }
    // Too slow to check values on all, so just do a few.
    TestCase.assertTrue(sub.containsValue(control.get(522)));
    TestCase.assertFalse(sub.containsValue(control.get(523)));
    TestCase.assertFalse(sub.containsValue(control.get(524)));
    
    try {
      sub.headMap(control.get(524));
      TestCase.fail("should have thrown IAE");
    } catch(IllegalArgumentException expected) {}
    
    TestCase.assertEquals(sub.lastKey(), control.get(522));
    TestCase.assertEquals(sub.firstKey(), control.get(0));
    
    sub = sub.tailMap(control.get(234));
    TestCase.assertEquals(289, sub.size());
    TestCase.assertEquals(control.get(234), sub.firstKey());
    TestCase.assertEquals(control.get(522), sub.lastKey());
    for(int i = 0; i < control.size(); i++) {
      if(i < 523 && i > 233)
        TestCase.assertTrue(sub.containsKey(control.get(i)));
      else
        TestCase.assertFalse(sub.containsKey(control.get(i)));
    }

    try {
      sub.tailMap(control.get(232));
      TestCase.fail("should have thrown IAE");
    } catch(IllegalArgumentException expected) {}
    
    sub = sub.subMap(control.get(300), control.get(400));
    TestCase.assertEquals(100, sub.size());
    TestCase.assertEquals(control.get(300), sub.firstKey());
    TestCase.assertEquals(control.get(399), sub.lastKey());
    
    for(int i = 0; i < control.size(); i++) {
      if(i < 400 && i > 299)
        TestCase.assertTrue(sub.containsKey(control.get(i)));
      else
        TestCase.assertFalse(sub.containsKey(control.get(i)));
    }
  }
  
  @Test
  public void testPrefixedBy() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);
    
    final String[] keys = new String[]{
        "", 
        "Albert", "Xavier", "XyZ", "Anna", "Alien", "Alberto",
        "Alberts", "Allie", "Alliese", "Alabama", "Banane",
        "Blabla", "Amber", "Ammun", "Akka", "Akko", "Albertoo",
        "Amma"
    };

    for (String key : keys) {
      trie.put(key, key);
    }
    
    SortedMap<String, String> map;
    Iterator<String> iterator;
    Iterator<Map.Entry<String, String>> entryIterator;
    Map.Entry<String, String> entry;
    
    map = trie.prefixMap("Al");
    TestCase.assertEquals(8, map.size());
    TestCase.assertEquals("Alabama", map.firstKey());
    TestCase.assertEquals("Alliese", map.lastKey());
    TestCase.assertEquals("Albertoo", map.get("Albertoo"));
    TestCase.assertNotNull(trie.get("Xavier"));
    TestCase.assertNull(map.get("Xavier"));
    TestCase.assertNull(trie.get("Alice"));
    TestCase.assertNull(map.get("Alice"));
    iterator = map.values().iterator();
    TestCase.assertEquals("Alabama", iterator.next());
    TestCase.assertEquals("Albert", iterator.next());
    TestCase.assertEquals("Alberto", iterator.next());
    TestCase.assertEquals("Albertoo", iterator.next());
    TestCase.assertEquals("Alberts", iterator.next());
    TestCase.assertEquals("Alien", iterator.next());
    TestCase.assertEquals("Allie", iterator.next());
    TestCase.assertEquals("Alliese", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    
    map = trie.prefixMap("Albert");
    iterator = map.keySet().iterator();
    TestCase.assertEquals("Albert", iterator.next());
    TestCase.assertEquals("Alberto", iterator.next());
    TestCase.assertEquals("Albertoo", iterator.next());
    TestCase.assertEquals("Alberts", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    TestCase.assertEquals(4, map.size());
    TestCase.assertEquals("Albert", map.firstKey());
    TestCase.assertEquals("Alberts", map.lastKey());
    TestCase.assertNull(trie.get("Albertz"));
    map.put("Albertz", "Albertz");
    TestCase.assertEquals("Albertz", trie.get("Albertz"));
    TestCase.assertEquals(5, map.size());
    TestCase.assertEquals("Albertz", map.lastKey());
    iterator = map.keySet().iterator();
    TestCase.assertEquals("Albert", iterator.next());
    TestCase.assertEquals("Alberto", iterator.next());
    TestCase.assertEquals("Albertoo", iterator.next());
    TestCase.assertEquals("Alberts", iterator.next());
    TestCase.assertEquals("Albertz", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    TestCase.assertEquals("Albertz", map.remove("Albertz"));
    
    map = trie.prefixMap("Alberto");
    TestCase.assertEquals(2, map.size());
    TestCase.assertEquals("Alberto", map.firstKey());
    TestCase.assertEquals("Albertoo", map.lastKey());
    entryIterator = map.entrySet().iterator();
    entry = entryIterator.next();
    TestCase.assertEquals("Alberto", entry.getKey());
    TestCase.assertEquals("Alberto", entry.getValue());
    entry = entryIterator.next();
    TestCase.assertEquals("Albertoo", entry.getKey());
    TestCase.assertEquals("Albertoo", entry.getValue());
    TestCase.assertFalse(entryIterator.hasNext());
    trie.put("Albertoad", "Albertoad");
    TestCase.assertEquals(3, map.size());
    TestCase.assertEquals("Alberto", map.firstKey());
    TestCase.assertEquals("Albertoo", map.lastKey());
    entryIterator = map.entrySet().iterator();
    entry = entryIterator.next();
    TestCase.assertEquals("Alberto", entry.getKey());
    TestCase.assertEquals("Alberto", entry.getValue());
    entry = entryIterator.next();
    TestCase.assertEquals("Albertoad", entry.getKey());
    TestCase.assertEquals("Albertoad", entry.getValue());
    entry = entryIterator.next();
    TestCase.assertEquals("Albertoo", entry.getKey());
    TestCase.assertEquals("Albertoo", entry.getValue());
    TestCase.assertFalse(entryIterator.hasNext());
    TestCase.assertEquals("Albertoo", trie.remove("Albertoo"));
    TestCase.assertEquals("Alberto", map.firstKey());
    TestCase.assertEquals("Albertoad", map.lastKey());
    TestCase.assertEquals(2, map.size());
    entryIterator = map.entrySet().iterator();
    entry = entryIterator.next();
    TestCase.assertEquals("Alberto", entry.getKey());
    TestCase.assertEquals("Alberto", entry.getValue());
    entry = entryIterator.next();
    TestCase.assertEquals("Albertoad", entry.getKey());
    TestCase.assertEquals("Albertoad", entry.getValue());
    TestCase.assertFalse(entryIterator.hasNext());
    TestCase.assertEquals("Albertoad", trie.remove("Albertoad"));
    trie.put("Albertoo", "Albertoo");
    
    map = trie.prefixMap("X");
    TestCase.assertEquals(2, map.size());
    TestCase.assertFalse(map.containsKey("Albert"));
    TestCase.assertTrue(map.containsKey("Xavier"));
    TestCase.assertFalse(map.containsKey("Xalan"));
    iterator = map.values().iterator();
    TestCase.assertEquals("Xavier", iterator.next());
    TestCase.assertEquals("XyZ", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    
    map = trie.prefixMap("An");
    TestCase.assertEquals(1, map.size());
    TestCase.assertEquals("Anna", map.firstKey());
    TestCase.assertEquals("Anna", map.lastKey());
    iterator = map.keySet().iterator();
    TestCase.assertEquals("Anna", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    
    map = trie.prefixMap("Ban");
    TestCase.assertEquals(1, map.size());
    TestCase.assertEquals("Banane", map.firstKey());
    TestCase.assertEquals("Banane", map.lastKey());
    iterator = map.keySet().iterator();
    TestCase.assertEquals("Banane", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    
    map = trie.prefixMap("Am");
    TestCase.assertFalse(map.isEmpty());
    TestCase.assertEquals(3, map.size());
    TestCase.assertEquals("Amber", trie.remove("Amber"));
    iterator = map.keySet().iterator();
    TestCase.assertEquals("Amma", iterator.next());
    TestCase.assertEquals("Ammun", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    iterator = map.keySet().iterator();
    map.put("Amber", "Amber");
    TestCase.assertEquals(3, map.size());
    try {
      iterator.next();
      TestCase.fail("CME expected");
    } catch(ConcurrentModificationException expected) {}
    TestCase.assertEquals("Amber", map.firstKey());
    TestCase.assertEquals("Ammun", map.lastKey());
    
    map = trie.prefixMap("Ak\0");
    TestCase.assertTrue(map.isEmpty());
    
    map = trie.prefixMap("Ak");
    TestCase.assertEquals(2, map.size());
    TestCase.assertEquals("Akka", map.firstKey());
    TestCase.assertEquals("Akko", map.lastKey());
    map.put("Ak", "Ak");
    TestCase.assertEquals("Ak", map.firstKey());
    TestCase.assertEquals("Akko", map.lastKey());
    TestCase.assertEquals(3, map.size());
    trie.put("Al", "Al");
    TestCase.assertEquals(3, map.size());
    TestCase.assertEquals("Ak", map.remove("Ak"));
    TestCase.assertEquals("Akka", map.firstKey());
    TestCase.assertEquals("Akko", map.lastKey());
    TestCase.assertEquals(2, map.size());
    iterator = map.keySet().iterator();
    TestCase.assertEquals("Akka", iterator.next());
    TestCase.assertEquals("Akko", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    TestCase.assertEquals("Al", trie.remove("Al"));
    
    map = trie.prefixMap("Akka");
    TestCase.assertEquals(1, map.size());
    TestCase.assertEquals("Akka", map.firstKey());
    TestCase.assertEquals("Akka", map.lastKey());
    iterator = map.keySet().iterator();
    TestCase.assertEquals("Akka", iterator.next());
    TestCase.assertFalse(iterator.hasNext());
    
    map = trie.prefixMap("Ab");
    TestCase.assertTrue(map.isEmpty());
    TestCase.assertEquals(0, map.size());
    try {
      Object o = map.firstKey();
      TestCase.fail("got a first key: " + o);
    } catch(NoSuchElementException nsee) {}
    try {
      Object o = map.lastKey();
      TestCase.fail("got a last key: " + o);
    } catch(NoSuchElementException nsee) {}
    iterator = map.values().iterator();
    TestCase.assertFalse(iterator.hasNext());
    
    map = trie.prefixMap("Albertooo");
    TestCase.assertTrue(map.isEmpty());
    TestCase.assertEquals(0, map.size());
    try {
      Object o = map.firstKey();
      TestCase.fail("got a first key: " + o);
    } catch(NoSuchElementException nsee) {}
    try {
      Object o = map.lastKey();
      TestCase.fail("got a last key: " + o);
    } catch(NoSuchElementException nsee) {}
    iterator = map.values().iterator();
    TestCase.assertFalse(iterator.hasNext());
    
    map = trie.prefixMap("");
    TestCase.assertSame(trie, map); // stricter than necessary, but a good check
    
    map = trie.prefixMap("\0");
    TestCase.assertTrue(map.isEmpty());
    TestCase.assertEquals(0, map.size());
    try {
      Object o = map.firstKey();
      TestCase.fail("got a first key: " + o);
    } catch(NoSuchElementException nsee) {}
    try {
      Object o = map.lastKey();
      TestCase.fail("got a last key: " + o);
    } catch(NoSuchElementException nsee) {}
    iterator = map.values().iterator();
    TestCase.assertFalse(iterator.hasNext());
  }
  
  @Test
  public void testPrefixedByRemoval() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);
    
    final String[] keys = new String[]{
        "Albert", "Xavier", "XyZ", "Anna", "Alien", "Alberto",
        "Alberts", "Allie", "Alliese", "Alabama", "Banane",
        "Blabla", "Amber", "Ammun", "Akka", "Akko", "Albertoo",
        "Amma"
    };

    for (String key : keys) {
      trie.put(key, key);
    }
    
    SortedMap<String, String> map = trie.prefixMap("Al");
    TestCase.assertEquals(8, map.size());
    Iterator<String> iter = map.keySet().iterator();
    TestCase.assertEquals("Alabama", iter.next());
    TestCase.assertEquals("Albert", iter.next());
    TestCase.assertEquals("Alberto", iter.next());
    TestCase.assertEquals("Albertoo", iter.next());
    TestCase.assertEquals("Alberts", iter.next());
    TestCase.assertEquals("Alien", iter.next());
    iter.remove();
    TestCase.assertEquals(7, map.size());
    TestCase.assertEquals("Allie", iter.next());
    TestCase.assertEquals("Alliese", iter.next());
    TestCase.assertFalse(iter.hasNext());
    
    map = trie.prefixMap("Ak");
    TestCase.assertEquals(2, map.size());
    iter = map.keySet().iterator();
    TestCase.assertEquals("Akka", iter.next());
    iter.remove();
    TestCase.assertEquals(1, map.size());
    TestCase.assertEquals("Akko", iter.next());
    if(iter.hasNext())
      TestCase.fail("shouldn't have next (but was: " + iter.next() + ")");
    TestCase.assertFalse(iter.hasNext());
  }

  @Test
  public void testTraverseWithAllNullBitKey() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);
    
    //
    // One entry in the Trie
    // Entry is stored at the root
    //
    
    // trie.put("", "All Bits Are Zero");
    trie.put("\0", "All Bits Are Zero");
    
    //
    //  / ("")   <-- root
    //  \_/  \
    //     null
    //
    
    final List<String> strings = new ArrayList<String>();
    trie.traverse(new Cursor<String, String>() {
      public Decision select(Entry<? extends String, ? extends String> entry) {
        strings.add(entry.getValue());
        return Decision.CONTINUE;
      }
    });
    
    TestCase.assertEquals(1, strings.size());
    
    strings.clear();
    for (String s : trie.values()) {
      strings.add(s);
    }
    TestCase.assertEquals(1, strings.size());
  }
  
  @Test
  public void testSelectWithAllNullBitKey() {
    PatriciaTrie<String, String> trie 
      = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);
    
    // trie.put("", "All Bits Are Zero");
    trie.put("\0", "All Bits Are Zero");
    
    final List<String> strings = new ArrayList<String>();
    trie.select("Hello", new Cursor<String, String>() {
      public Decision select(Entry<? extends String, ? extends String> entry) {
        strings.add(entry.getValue());
        return Decision.CONTINUE;
      }
    });
    TestCase.assertEquals(1, strings.size());
  }
  
  private static class TestCursor implements Cursor<Object, Object> {
    private List<Object> keys;
    private List<Object> values;
    private Object selectFor;
    private List<Object> toRemove;
    private int index = 0;
    
    TestCursor(Object... objects) {
      if(objects.length % 2 != 0)
        throw new IllegalArgumentException("must be * 2");
      
      keys = new ArrayList<Object>(objects.length / 2);
      values = new ArrayList<Object>(keys.size());
      toRemove = Collections.emptyList();
      for(int i = 0; i < objects.length; i++) {
        keys.add(objects[i]);
        values.add(objects[++i]);
      }
    }
    
    void addToRemove(Object... objects) {
      toRemove = new ArrayList<Object>(Arrays.asList(objects));
    }
    
    void remove(Object... objects) {
      for (Object object : objects) {
        int idx = keys.indexOf(object);
        keys.remove(idx);
        values.remove(idx);
      }
    }
    
    void starting() {
      index = 0;
    }
    
    public void checkKey(Object k) {
      TestCase.assertEquals(keys.get(index++), k);
    }
    
    public void checkValue(Object o) {
      TestCase.assertEquals(values.get(index++), o);
    }

    public Decision select(Entry<?, ?> entry) {
      //  System.out.println("Scanning: " + entry.getKey());
      TestCase.assertEquals(keys.get(index), entry.getKey());
      TestCase.assertEquals(values.get(index), entry.getValue());
      index++;
      
      if(toRemove.contains(entry.getKey())) {
        // System.out.println("Removing: " + entry.getKey());
        index--;
        keys.remove(index);
        values.remove(index);
        toRemove.remove(entry.getKey());
        return Decision.REMOVE;
      } 
      
      if(selectFor != null && selectFor.equals(entry.getKey()))
        return Decision.EXIT;
      else
        return Decision.CONTINUE;
    }
    
    void finished() {
      TestCase.assertEquals(keys.size(), index);
    }
  }
  
  private static void assertEqualArrays(Object[] a, Object[] b) {
    TestCase.assertTrue(Arrays.equals(a, b));
  }
}
