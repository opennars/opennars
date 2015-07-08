package org.ardverk.collection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.junit.Test;

public class SerializationTest {

  @Test
  public void serialize() throws IOException, ClassNotFoundException {
    Trie<String, String> trie1 
      = new PatriciaTrie<String, String>(
        StringKeyAnalyzer.CHAR);
    trie1.put("Hello", "World");
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(trie1);
    oos.close();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);
    
    @SuppressWarnings("unchecked")
    Trie<String, String> trie2 = (Trie<String, String>)ois.readObject();
    ois.close();
    
    TestCase.assertEquals(trie1.size(), trie2.size());
    TestCase.assertEquals("World", trie2.get("Hello"));
  }
  
  @Test
  public void prefixMap() throws IOException, ClassNotFoundException {
    Trie<String, String> trie1 
      = new PatriciaTrie<String, String>(
        StringKeyAnalyzer.CHAR);
    trie1.put("Hello", "World");
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(trie1);
    oos.close();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);
    
    @SuppressWarnings("unchecked")
    Trie<String, String> trie2 = (Trie<String, String>)ois.readObject();
    ois.close();
    
    TestCase.assertEquals(1, trie1.prefixMap("Hello").size());
    TestCase.assertEquals(1, trie2.prefixMap("Hello").size());
  }
}
