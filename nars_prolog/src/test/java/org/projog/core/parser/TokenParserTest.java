package org.projog.core.parser;

import org.junit.Test;
import org.projog.core.Operands;

import java.io.StringReader;

import static org.junit.Assert.*;
import static org.projog.TestUtils.createKnowledgeBase;
import static org.projog.core.KnowledgeBaseUtils.getOperands;
import static org.projog.core.parser.TokenType.*;

public class TokenParserTest {
   private final Operands operands = getOperands(createKnowledgeBase());

   @Test
   public void testAtom() {
      assertTokenType("a", ATOM);
      assertTokenType("ab", ATOM);
      assertTokenType("aB", ATOM);
      assertTokenType("a1", ATOM);
      assertTokenType("a_", ATOM);
      assertTokenType("a_2bY", ATOM);
   }

   @Test
   public void testQuotedAtom() {
      assertTokenType("'abcdefg'", "abcdefg", QUOTED_ATOM);
      assertTokenType("''", "", QUOTED_ATOM);
      assertTokenType("''''", "'", QUOTED_ATOM);
      assertTokenType("''''''''''", "''''", QUOTED_ATOM);
      assertTokenType("'q 1 \" 0.5 | '' @#~'", "q 1 \" 0.5 | ' @#~", QUOTED_ATOM);
   }

   @Test
   public void testVariable() {
      assertTokenType("X", VARIABLE);
      assertTokenType("XY", VARIABLE);
      assertTokenType("Xy", VARIABLE);
      assertTokenType("X1", VARIABLE);
      assertTokenType("X_", VARIABLE);
      assertTokenType("X_7hU", VARIABLE);
   }

   @Test
   public void testAnonymousVariable() {
      assertTokenType("_", ANONYMOUS_VARIABLE);
      assertTokenType("__", ANONYMOUS_VARIABLE);
      assertTokenType("_X", ANONYMOUS_VARIABLE);
      assertTokenType("_x", ANONYMOUS_VARIABLE);
      assertTokenType("_2", ANONYMOUS_VARIABLE);
      assertTokenType("_X_2a", ANONYMOUS_VARIABLE);
   }

   @Test
   public void testInteger() {
      assertTokenType("0", INTEGER);
      assertTokenType("1", INTEGER);
      assertTokenType("6465456456", INTEGER);
   }

   @Test
   public void testFloat() {
      assertTokenType("0.0", FLOAT);
      assertTokenType("0.1", FLOAT);
      assertTokenType("768.567567", FLOAT);
      assertTokenType("3.4028235E38", FLOAT);
      assertTokenType("3.4028235e38", FLOAT);
   }

   @Test
   public void testEmptyInput() {
      assertFalse(create("").hasNext());
      assertFalse(create("\t \r\n   ").hasNext());
      assertFalse(create("%abcde").hasNext()); // single line comment
      assertFalse(create("/* hgjh\nghj*/").hasNext()); // multi line comment
   }

   @Test
   public void testSequence() {
      assertParse("Abc12.5@>=-0_2_jgkj a-2hUY_ty\nu\n% kghjgkj\na/*b*/c", "Abc12", ".", "5", "@>=", "-", "0", "_2_jgkj", "a", "-", "2", "hUY_ty", "u", "a", "c");
   }

   @Test
   public void testSentence() {
      assertParse("X is ~( 'Y', 1 ,a).", "X", "is", "~", "(", "Y", ",", "1", ",", "a", ")", ".");
   }

   @Test
   public void testNonAlphanumericCharacterFollowedByPeriod() {
      assertParse("!.", "!", ".");
   }

   /** Test that "!" and ";" get parsed separately, rather than as single combined "!;" element. */
   @Test
   public void testCutFollowedByDisjunction() {
      assertParse("!;true", "!", ";", "true");
   }

   /** Test that "(", "!", ")" and "." get parsed separately, rather than as single combined "(!)." element. */
   @Test
   public void testCutInBrackets() {
      assertParse("(!).", "(", "!", ")", ".");
   }

   @Test
   public void testWhitespaceAndComments() {
      TokenParser p = create("/* comment */\t % comment\n % comment\r\n\n");
      assertFalse(p.hasNext());
   }

   @Test
   public void testMultiLineComments() {
      assertParse("/*\n\n*\n/\n*/a/*/b*c/d/*e*/f", "a", "f");
   }

   @Test
   public void testFollowedByTerm() {
      TokenParser tp = create("?- , [ abc )");
      tp.next();
      assertFalse(tp.isFollowedByTerm());
      tp.next();
      assertTrue(tp.isFollowedByTerm());
      tp.next();
      assertTrue(tp.isFollowedByTerm());
      tp.next();
      assertFalse(tp.isFollowedByTerm());
   }

   /** @see {@link TokenParser#rewind(String)} */
   @Test
   public void testRewindException() {
      TokenParser tp = create("a b c");
      assertEquals("a", tp.next().value);
      Token b = tp.next();
      assertEquals("b", b.value);
      tp.rewind(b);
      assertSame(b, tp.next());
      tp.rewind(b);

      // check that can only rewind one token
      assertRewindException(tp, "b");
      assertRewindException(tp, "a");

      assertEquals("b", tp.next().value);
      Token c = tp.next();
      assertEquals("c", c.value);

      // check that the value specified in call to rewind has to be the last value parsed
      assertRewindException(tp, "b");
      assertRewindException(tp, null);
      assertRewindException(tp, "z");

      tp.rewind(c);
      assertSame(c, tp.next());
      assertFalse(tp.hasNext());
      tp.rewind(c);
      assertTrue(tp.hasNext());

      // check that can only rewind one token
      assertRewindException(tp, "c");
   }

   private void assertRewindException(TokenParser tp, String value) {
      try {
         tp.rewind(new Token(value, TokenType.ATOM));
         fail();
      } catch (IllegalArgumentException e) {
         // expected
      }
   }

   private void assertTokenType(String syntax, TokenType type) {
      assertTokenType(syntax, syntax, type);
   }

   private void assertTokenType(String syntax, String value, TokenType type) {
      TokenParser p = create(syntax);
      assertTrue(p.hasNext());
      Token token = p.next();
      assertEquals(value, token.value);
      assertSame(type, token.type);
      assertFalse(p.hasNext());
   }

   private void assertParse(String sentence, String... tokens) {
      TokenParser tp = create(sentence);
      for (String w : tokens) {
         Token next = tp.next();
         assertEquals(w, next.value);
         tp.rewind(next);
         assertSame(next, tp.next());
      }
      assertFalse(tp.hasNext());
      try {
         tp.next();
         fail();
      } catch (ParserException e) {
         assertEquals("Unexpected end of stream Line: " + e.getLine(), e.getMessage());
      }
   }

   private TokenParser create(String syntax) {
      StringReader sr = new StringReader(syntax);
      return new TokenParser(sr, operands);
   }
}
