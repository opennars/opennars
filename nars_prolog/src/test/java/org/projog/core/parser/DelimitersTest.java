package org.projog.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DelimitersTest {
   @Test
   public void testArgumentSeperator() {
      assertTrue(Delimiters.isArgumentSeperator(symbol(",")));
      assertFalse(Delimiters.isArgumentSeperator(atom(",")));
      assertFalse(Delimiters.isArgumentSeperator(symbol(";")));
      assertFalse(Delimiters.isArgumentSeperator(symbol(" ")));
      assertFalse(Delimiters.isArgumentSeperator(null));
   }

   @Test
   public void testListOpenBracket() {
      assertTrue(Delimiters.isListOpenBracket(symbol("[")));
      assertFalse(Delimiters.isListOpenBracket(atom("[")));
      assertFalse(Delimiters.isListOpenBracket(symbol("]")));
      assertFalse(Delimiters.isListOpenBracket(symbol("(")));
      assertFalse(Delimiters.isArgumentSeperator(null));
   }

   @Test
   public void testListCloseBracket() {
      assertTrue(Delimiters.isListCloseBracket(symbol("]")));
      assertFalse(Delimiters.isListCloseBracket(atom("]")));
      assertFalse(Delimiters.isListCloseBracket(symbol("[")));
      assertFalse(Delimiters.isListCloseBracket(symbol(")")));
      assertFalse(Delimiters.isArgumentSeperator(null));
   }

   @Test
   public void testPredicateOpenBracket() {
      assertTrue(Delimiters.isPredicateOpenBracket(symbol("(")));
      assertFalse(Delimiters.isPredicateOpenBracket(atom("(")));
      assertFalse(Delimiters.isPredicateOpenBracket(symbol(")")));
      assertFalse(Delimiters.isPredicateOpenBracket(symbol("[")));
      assertFalse(Delimiters.isArgumentSeperator(null));
   }

   @Test
   public void testPredicateCloseBracket() {
      assertTrue(Delimiters.isPredicateCloseBracket(symbol(")")));
      assertFalse(Delimiters.isPredicateCloseBracket(atom(")")));
      assertFalse(Delimiters.isPredicateCloseBracket(symbol("(")));
      assertFalse(Delimiters.isPredicateCloseBracket(symbol("]")));
      assertFalse(Delimiters.isArgumentSeperator(null));
   }

   @Test
   public void testListTail() {
      assertTrue(Delimiters.isListTail(symbol("|")));
      assertFalse(Delimiters.isListTail(atom("|")));
      assertFalse(Delimiters.isListTail(symbol("[")));
      assertFalse(Delimiters.isListTail(symbol("]")));
      assertFalse(Delimiters.isArgumentSeperator(null));
   }

   @Test
   public void testSentenceTerminator() {
      assertTrue(Delimiters.isSentenceTerminator(symbol(".")));
      assertFalse(Delimiters.isSentenceTerminator(atom(".")));
      assertFalse(Delimiters.isSentenceTerminator(symbol("..=")));
      assertFalse(Delimiters.isSentenceTerminator(symbol(",")));
      assertFalse(Delimiters.isArgumentSeperator(symbol(null)));
   }

   @Test
   public void testDelimiter() {
      assertDelimiter(true, '[', ']', '(', ')', '|', ',', '.');
      assertDelimiter(false, '!', '?', '{', '}', ':', ';', '-', 'a', 'A', '1');
      assertFalse(Delimiters.isDelimiter("..="));
      assertFalse(Delimiters.isDelimiter(null));
   }

   private void assertDelimiter(boolean expectedResult, char... chars) {
      for (char c : chars) {
         assertEquals(expectedResult, Delimiters.isDelimiter(c));
         assertEquals(expectedResult, Delimiters.isDelimiter(Character.toString(c)));
      }
   }

   private Token symbol(String value) {
      return new Token(value, TokenType.SYMBOL);
   }

   private Token atom(String value) {
      return new Token(value, TokenType.ATOM);
   }
}
