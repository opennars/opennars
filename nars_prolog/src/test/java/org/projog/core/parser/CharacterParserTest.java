package org.projog.core.parser;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class CharacterParserTest {
   @Test
   public void testEmpty() {
      CharacterParser p = createParser("");
      assertEquals(-1, p.peek());
      assertEquals(-1, p.getNext());
      assertEquals(-1, p.peek());
      assertEquals(-1, p.getNext());
   }

   @Test
   public void testSingleCharacter() {
      CharacterParser p = createParser("a");
      assertEquals('a', p.peek());
      assertEquals('a', p.peek());
      assertEquals('a', p.getNext());
      assertEquals('\n', p.peek());
      assertEquals('\n', p.getNext());
      assertEquals(-1, p.peek());
      assertEquals(-1, p.getNext());
      p.rewind();
      assertEquals('\n', p.peek());
      p.rewind();
      assertEquals('a', p.peek());
      assertEquals('a', p.getNext());
   }

   @Test
   public void testSingleLine() {
      String s = "qwerty";
      CharacterParser p = createParser(s);
      for (char c : s.toCharArray()) {
         assertEquals(c, p.getNext());
      }
      assertEquals('\n', p.getNext());
      assertEquals(-1, p.getNext());
   }

   @Test
   public void testGetColumnNumber() {
      String s = "abc";
      CharacterParser p = createParser(s);
      assertEquals('a', p.getNext());
      assertEquals(1, p.getColumnNumber());
      assertEquals('b', p.getNext());
      assertEquals(2, p.getColumnNumber());
      assertEquals('c', p.peek());
      assertEquals(2, p.getColumnNumber());
      p.rewind();
      assertEquals(1, p.getColumnNumber());
   }

   @Test
   public void testMultipleLine() {
      String s = "qwerty\nasdf";
      CharacterParser p = createParser(s);

      assertEquals('q', p.getNext());
      assertEquals("qwerty", p.getLine());
      assertEquals(1, p.getLineNumber());

      p.skipLine();

      assertEquals('a', p.getNext());
      assertEquals("asdf", p.getLine());
      assertEquals(2, p.getLineNumber());
   }

   @Test
   public void testRewind() {
      String s = "qwerty";
      CharacterParser p = createParser(s);
      assertEquals('q', p.getNext());
      assertEquals(1, p.getColumnNumber());
      p.rewind();
      assertEquals('q', p.getNext());
      assertEquals('w', p.getNext());
      assertEquals('e', p.getNext());
      assertEquals('r', p.getNext());
      assertEquals('t', p.getNext());
      assertEquals('y', p.getNext());
      assertEquals(6, p.getColumnNumber());
      p.rewind();
      assertEquals(5, p.getColumnNumber());
      assertEquals('y', p.getNext());
      p.rewind(3);
      assertEquals(3, p.getColumnNumber());
      assertEquals('r', p.getNext());
      assertEquals(4, p.getColumnNumber());
      p.rewind(4);
      assertEquals('q', p.getNext());
      assertEquals(1, p.getColumnNumber());
   }

   @Test
   public void testEndOfStreamTrailingNoLine() {
      String s = "a\n";
      CharacterParser p = createParser(s);
      assertEquals('a', p.getNext());
      assertEquals('\n', p.getNext());
      assertEquals(-1, p.peek());
      p.rewind();
      assertEquals('\n', p.getNext());
      assertEquals(-1, p.getNext());
      assertEquals(-1, p.getNext());
      p.rewind();
      assertEquals('\n', p.getNext());
   }

   @Test
   public void testEndOfStreamNoTrailingNoLine() {
      String s = "a";
      CharacterParser p = createParser(s);
      assertEquals('a', p.getNext());
      assertEquals('\n', p.getNext());
      assertEquals(-1, p.peek());
      p.rewind();
      assertEquals('\n', p.getNext());
      assertEquals(-1, p.getNext());
      assertEquals(-1, p.getNext());
      p.rewind();
      assertEquals('\n', p.getNext());
   }

   @Test
   public void testBlankLines() {
      String s = "\n\n";
      CharacterParser p = createParser(s);
      assertEquals('\n', p.peek());
      p.skipLine();
      assertEquals('\n', p.getNext());
      assertEquals(-1, p.getNext());
      assertEquals(-1, p.peek());
   }

   @Test
   public void testRewindException() {
      String s = "a\nbcdef";
      CharacterParser p = createParser(s);
      assertEquals('a', p.getNext());
      assertEquals('\n', p.getNext());
      assertEquals('b', p.getNext());
      assertEquals('c', p.getNext());
      assertEquals('d', p.getNext());
      p.rewind(3);
      assertEquals('b', p.getNext());
      assertEquals('c', p.getNext());
      assertEquals('d', p.getNext());
      try {
         p.rewind(4);
      } catch (ParserException e) {
         assertEquals("Cannot rewind past start of current line Line: bcdef", e.getMessage());
         assertEquals(3, e.getColumnNumber());
         assertEquals(2, e.getLineNumber());
      }
   }

   private CharacterParser createParser(String s) {
      StringReader sr = new StringReader(s);
      BufferedReader br = new BufferedReader(sr);
      return new CharacterParser(br);
   }
}
