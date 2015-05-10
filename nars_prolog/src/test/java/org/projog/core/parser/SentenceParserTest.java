package org.projog.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.parseSentence;
import static org.projog.TestUtils.write;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.Operands;
import org.projog.core.term.PTerm;

public class SentenceParserTest {
   @Test
   public void testIncompleteSentences() {
      error(":-");
      error("a :-");
      error("a :- .");
      error(":- X is");
      error(":- X is 1"); // no '.' character at end of sentence
      error(":- X is p(a, b, c)"); // no '.' character at end of sentence
      error(":- X is [a, b, c | d]"); // no '.' character at end of sentence
      error(":- X = 'hello."); // no closing quote on atom
      error(":- X = /*hello."); // no closing */ on comment
   }

   @Test
   public void testIncompletePredicateSyntax() {
      error(":- X is p("); // no )
      error(":- X is p(a, b"); // no )
      error(":- X is p(a b"); // no ,
   }

   @Test
   public void testInvalidListSyntax() {
      error(":- X is ["); // no ]
      error(":- X is [a b"); // no , or |
      error(":- X is [a, b"); // no ]
      error(":- X is [a, b |"); // no tail
      error(":- X is [a, b | ]"); // no tail
      error(":- X is [a, b | c, d]"); // 2 args after |
   }

   @Test
   public void testInvalidOperators() {
      error("a xyz b.");
      error("a $ b.");
      error("a b.");
      error("$ b.");
   }

   @Test
   public void testInvalidOperatorOrder() {
      error("1 :- 2 :- 3.");
      error(":- X = 1 + 2 + 3 + 4 = 5.");
      error("a ?- b.");
      error("?- a ?- b.");
      error("?- :- X.");
      error("?- ?- true.");
   }

   @Test
   public void testEquationPrecedence() {
      checkEquation("(((1+2)-3)*4)/5", "/(*(-(+(1, 2), 3), 4), 5)");

      checkEquation("1+2-3*4/5", "-(+(1, 2), /(*(3, 4), 5))");
      checkEquation("1+2-3/4*5", "-(+(1, 2), *(/(3, 4), 5))");
      checkEquation("1+2/3-4*5", "-(+(1, /(2, 3)), *(4, 5))");
      checkEquation("1+2/3*4-5", "-(+(1, *(/(2, 3), 4)), 5)");
      checkEquation("1/2+3*4-5", "-(+(/(1, 2), *(3, 4)), 5)");
      checkEquation("1/2*3+4-5", "-(+(*(/(1, 2), 3), 4), 5)");

      checkEquation("1+2+3+4+5+6+7+8+9+0", "+(+(+(+(+(+(+(+(+(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1*2+3+4+5+6+7+8+9+0", "+(+(+(+(+(+(+(+(*(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1+2+3+4+5*6+7+8+9+0", "+(+(+(+(+(+(+(+(1, 2), 3), 4), *(5, 6)), 7), 8), 9), 0)");
      checkEquation("1+2+3+4+5+6+7+8+9*0", "+(+(+(+(+(+(+(+(1, 2), 3), 4), 5), 6), 7), 8), *(9, 0))");
      checkEquation("1*2+3+4+5*6+7+8+9+0", "+(+(+(+(+(+(+(*(1, 2), 3), 4), *(5, 6)), 7), 8), 9), 0)");
      checkEquation("1*2+3+4+5+6+7+8+9*0", "+(+(+(+(+(+(+(*(1, 2), 3), 4), 5), 6), 7), 8), *(9, 0))");
      checkEquation("1+2+3+4+5*6+7+8+9*0", "+(+(+(+(+(+(+(1, 2), 3), 4), *(5, 6)), 7), 8), *(9, 0))");
      checkEquation("1*2+3+4+5*6+7+8+9*0", "+(+(+(+(+(+(*(1, 2), 3), 4), *(5, 6)), 7), 8), *(9, 0))");

      checkEquation("1*2*3*4*5*6*7*8*9*0", "*(*(*(*(*(*(*(*(*(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1+2*3*4*5*6*7*8*9*0", "+(1, *(*(*(*(*(*(*(*(2, 3), 4), 5), 6), 7), 8), 9), 0))");
      checkEquation("1*2*3*4*5+6*7*8*9*0", "+(*(*(*(*(1, 2), 3), 4), 5), *(*(*(*(6, 7), 8), 9), 0))");
      checkEquation("1*2*3*4*5*6*7*8*9+0", "+(*(*(*(*(*(*(*(*(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1+2*3*4*5+6*7*8*9*0", "+(+(1, *(*(*(2, 3), 4), 5)), *(*(*(*(6, 7), 8), 9), 0))");
      checkEquation("1+2*3*4*5*6*7*8*9+0", "+(+(1, *(*(*(*(*(*(*(2, 3), 4), 5), 6), 7), 8), 9)), 0)");
      checkEquation("1*2*3*4*5+6*7*8*9+0", "+(+(*(*(*(*(1, 2), 3), 4), 5), *(*(*(6, 7), 8), 9)), 0)");
      checkEquation("1+2*3*4*5+6*7*8*9+0", "+(+(+(1, *(*(*(2, 3), 4), 5)), *(*(*(6, 7), 8), 9)), 0)");
   }

   @Test
   public void testMultiTerm() {
      String[] sentences = {"p(A, B, C) :- A = 1 , B = 2 , C = 3", "p(X, Y, Z) :- X = 1 , Y = 2 , Z = 3", "p(Q, W, E) :- Q = 1 ; W = 2 ; E = 3"};
      String source = sentences[0] + ".\n" + sentences[1] + ". " + sentences[2] + ".";
      SentenceParser sp = getSentenceParser(source);
      for (String sentence : sentences) {
         PTerm t = sp.parseSentence();
         assertNotNull(t);
         assertEquals(sentence, write(t));
      }
   }

   @Test
   public void testConjunction() {
      assertParse("a, b, c.", "a , b , c", ",(,(a, b), c)");
   }

   @Test
   public void testBrackets1() {
      assertParse("a(b,(c)).", "a(b, c)", "a(b, c)");
   }

   @Test
   public void testBrackets2() {
      assertParse("?- fail, fail ; true.", "?- fail , fail ; true", "?-(;(,(fail, fail), true))");
      assertParse("?- fail, (fail;true).", "?- fail , (fail ; true)", "?-(,(fail, ;(fail, true)))");
   }

   @Test
   public void testBrackets3() {
      assertParse("?- X is 4*(2+3).", "?- X is 4 * (2 + 3)", "?-(is(X, *(4, +(2, 3))))");
   }

   @Test
   public void testBrackets4() {
      assertParse("?- Y = ( ^^ = @@ ).", "?- Y = (^^ = @@)", "?-(=(Y, =(^^, @@)))");
   }

   @Test
   public void testBrackets5() {
      assertParse("?- X = a(b,(c)).", "?- X = a(b, c)", "?-(=(X, a(b, c)))");
   }

   @Test
   public void testBrackets6() {
      assertParse("X = ( A = 1 , B = 2 , C = 3).", "X = (A = 1 , B = 2 , C = 3)", "=(X, ,(,(=(A, 1), =(B, 2)), =(C, 3)))");
   }

   @Test
   public void testParsingBrackets7() {
      assertParse("X = (!).", "X = !", "=(X, !)");
   }

   @Test
   public void testParsingBrackets8() {
      assertParse("X = (a, !).", "X = (a , !)", "=(X, ,(a, !))");
   }

   @Test
   public void testParsingBrackets9() {
      assertParse("X = (a, !; b).", "X = (a , ! ; b)", "=(X, ;(,(a, !), b))");
   }

   @Test
   public void testParsingBrackets10() {
      assertParse("X = [a,'('|Y].", "X = [a,(|Y]", "=(X, .(a, .((, Y)))");
   }

   @Test
   public void testParsingBrackets11() {
      assertParse("a :- (b, c ; e), f.", "a :- b , c ; e , f", ":-(a, ,(;(,(b, c), e), f))");
   }

   @Test
   public void testParsingBrackets12() {
      assertParse("a :- z, (b, c ; e), f.", "a :- z , (b , c ; e) , f", ":-(a, ,(,(z, ;(,(b, c), e)), f))");
   }

   @Test
   public void testExtraTextAfterFullStop() {
      SentenceParser sp = getSentenceParser("?- consult(\'bench.pl\'). jkhkj");
      PTerm t = sp.parseSentence();
      assertEquals("?-(consult(bench.pl))", t.toString());
      try {
         sp.parseSentence();
         fail();
      } catch (ParserException pe) {
         // expected
      }
   }

   @Test
   public void testMixtureOfPrefixInfixAndPostfixOperands() {
      assertParse("a --> { 1 + -2 }.", "a --> { 1 + -2 }", "-->(a, {(}(+(1, -2))))");
   }

   /**
    * Test "xf" (postfix) associativity.
    * <p>
    * A "x" means that the argument can contain operators of <i>only</i> a lower level of priority than the operator
    * represented by "f".
    */
   @Test
   public void testParseOperandXF() {
      Operands o = new Operands();
      o.addOperand("~", "xf", 900);
      SentenceParser sp = SentenceParser.getInstance("a ~.", o);
      PTerm t = sp.parseSentence();
      assertEquals("~(a)", t.toString());
      try {
         sp = SentenceParser.getInstance("a ~ ~.", o);
         sp.parseSentence();
         fail();
      } catch (ParserException e) {
         // expected
      }
   }

   /**
    * Test "yf" (postfix) associativity.
    * <p>
    * A "y" means that the argument can contain operators of <i>the same</i> or lower level of priority than the
    * operator represented by "f".
    */
   @Test
   public void testParseOperandYF() {
      Operands o = new Operands();
      o.addOperand(":", "yf", 900);
      SentenceParser sp = SentenceParser.getInstance("a : :.", o);
      PTerm t = sp.parseSentence();
      assertEquals(":(:(a))", t.toString());
   }

   @Test
   public void testBuiltInPredicateNamesAsAtomArguments() {
      check("[=]", ".(=, [])");
      check("[=, = | =]", ".(=, .(=, =))");

      check("[:-]", ".(:-, [])");
      check("[:-, :- | :-]", ".(:-, .(:-, :-))");

      check("p(?-)", "p(?-)");
      check("p(:-)", "p(:-)");
      check("p(<)", "p(<)");

      check("p(1<1,is)", "p(<(1, 1), is)");
      check("p(;, ',', :-, ?-)", "p(;, ,, :-, ?-)");

      check("?- write(p(1, :-, 1))", "?-(write(p(1, :-, 1)))");
      check("?- write(p(1, ',', 1))", "?-(write(p(1, ,, 1)))");
      check("?- write(p(<,>,=))", "?-(write(p(<, >, =)))");

      // following fails as '\\+' prefix operand has higher precedence than '/' infix operand
      error("?- test('\\+'/1, 'abc').");
      // following works as explicitly specifying '/' as the functor of a structure
      check("?- test('/'('\\+', 1), 'abc')", "?-(test(/(\\+, 1), abc))");

      error("p(a :- b).");
      check("p(:-(a, b))", "p(:-(a, b))");
      check("p(':-'(a, b))", "p(:-(a, b))");
   }

   @Test
   public void testListAfterPrefixOperator() {
      assertParse("?- [a,b,c].", "?- [a,b,c]", "?-(.(a, .(b, .(c, []))))");
   }

   @Test
   public void testSentenceTerminatorAsAtomName() {
      assertParse("p(C) :- C=='.'.", "p(C) :- C == .", ":-(p(C), ==(C, .))");
   }

   @Test
   public void testAlphaNumericPredicateName() {
      String expectedOutput = "is(X, ~(1, 1))";
      check("X is '~'(1,1)", expectedOutput);
      check("X is ~(1,1)", expectedOutput);
   }

   @Test
   public void testInfixOperatorAsPredicateName() {
      String expectedOutput = "is(X, +(1, 1))";
      check("X is '+'(1,1)", expectedOutput);
      check("X is 1+1", expectedOutput);
      check("X is +(1,1)", expectedOutput);
      check("X = >(+(1,1),-2)", "=(X, >(+(1, 1), -2))");
   }

   private void checkEquation(String input, String expected) {
      check(input, expected);

      // apply same extra tests just because is easy to do...
      check("X is " + input, "is(X, " + expected + ")");
      String conjunction = "X is " + input + ", Y is " + input + ", Z is " + input;
      String expectedConjunctionResult = ",(,(is(X, " + expected + "), is(Y, " + expected + ")), is(Z, " + expected + "))";
      check(conjunction, expectedConjunctionResult);
      check("?- " + conjunction, "?-(" + expectedConjunctionResult + ")");
      check("test(X, Y, Z) :- " + conjunction, ":-(test(X, Y, Z), " + expectedConjunctionResult + ")");

      for (int n = 0; n < 10; n++) {
         input = input.replace("" + n, "p(" + n + ")");
         expected = expected.replace("" + n, "p(" + n + ")");
      }
   }

   private void error(String input) {
      try {
         PTerm term = parseSentence(input);
         fail("parsing: " + input + " produced: " + term + " when expected an exception");
      } catch (ParserException pe) {
         // expected
      } catch (Exception e) {
         e.printStackTrace();
         fail("parsing: " + input + " produced: " + e + " when expected a ParserException");
      }
   }

   /**
    * @param input syntax (not including trailing .) to attempt to produce term for
    * @param expectedOutput what toString method of Term should look like
    */
   private PTerm check(String input, String expectedOutput) {
      error(input);
      try {
         input += ".";
         PTerm t = parseSentence(input);
         if (!expectedOutput.equals(t.toString())) {
            throw new Exception("got: " + t + " instead of: " + expectedOutput);
         }
         return t;
      } catch (Exception e) {
         throw new RuntimeException("Exception parsing: " + input + " " + e.getClass() + " " + e.getMessage(), e);
      }
   }

   private void assertParse(String input, String expectedFormatterOutput, String expectedToString) {
      PTerm t = parseSentence(input);
      assertEquals(expectedFormatterOutput, write(t));
      assertEquals(expectedToString, t.toString());
   }

   private SentenceParser getSentenceParser(String source) {
      return TestUtils.createSentenceParser(source);
   }
}