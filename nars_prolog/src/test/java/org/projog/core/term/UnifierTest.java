package org.projog.core.term;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.decimalFraction;
import static org.projog.TestUtils.integerNumber;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;

import org.junit.Test;

public class UnifierTest {
   /** [a] unified with [a] */
   @Test
   public void testExactMatchSingleImmutableArguments() {
      Atom inputArg = new Atom("a");
      Atom consequentArg = new Atom("a");
      PTerm[] input = {inputArg};
      PTerm[] consequent = {consequentArg};
      assertPreMatch(input, consequent);
      assertSame(inputArg, input[0]);
      assertSame(consequentArg, consequent[0]);
   }

   /** [a] unified with [b] */
   @Test
   public void testNoMatchSingleImmutableArguments() {
      Atom a = new Atom("a");
      Atom b = new Atom("b");
      assertPreMatchFailed(new PTerm[] {a}, new PTerm[] {b});
   }

   @Test
   public void testExactMatchManyImmutableArguments() {
      PTerm[] inputArgs = createListOfImmutableArguments();
      PTerm[] copyInputArgs = copy(inputArgs);

      PTerm[] consequentArgs = createListOfImmutableArguments();
      PTerm[] copyConsequentArgs = copy(consequentArgs);

      assertPreMatch(inputArgs, consequentArgs);

      for (int i = 0; i < copyInputArgs.length; i++) {
         assertSame(copyInputArgs[i], inputArgs[i]);
         assertSame(copyConsequentArgs[i], consequentArgs[i]);
         assertNotSame(inputArgs[i], consequentArgs[i]);
      }
   }

   private PTerm[] createListOfImmutableArguments() {
      return new PTerm[] {atom("a"), integerNumber(1), decimalFraction(1.5), structure("p", atom("x"), atom("y"))};
   }

   /** [X] unified with [a] */
   @Test
   public void testSingleVariableInInput() {
      Atom a = atom("a");
      Variable v = variable("X");
      PTerm[] input = {v};
      PTerm[] consequent = {a};
      assertPreMatch(input, consequent);
      assertSame(v, input[0]);
      assertSame(a, v.get());
      assertSame(a, consequent[0]);
   }

   /** [a] unified with [X] */
   @Test
   public void testSingleVariableInConsequent() {
      Atom a = atom("a");
      Variable v = variable("X");
      PTerm[] input = {a};
      PTerm[] consequent = {v};
      assertPreMatch(input, consequent);
      assertSame(a, input[0]);
      assertSame(a, consequent[0]);
   }

   /** [X] unified with [Y] */
   @Test
   public void testVariableInInputAndConsequent() {
      Variable v1 = variable("X");
      Variable v2 = variable("Y");
      PTerm[] input = {v1};
      PTerm[] consequent = {v2};
      assertPreMatch(input, consequent);
      assertSame(v1, input[0]);
      assertSame(v2, consequent[0]);
   }

   /** [X] unified with [Y] when X already unified to a */
   @Test
   public void testSingleAssignedVariableInInput_1() {
      Variable x = variable("X");
      Variable y = variable("Y");
      Atom a = atom();

      x.unify(a);

      PTerm[] input = {x};
      PTerm[] consequent = {y};

      assertPreMatch(input, consequent);

      assertSame(x, input[0]);
      assertSame(a, x.get());
      assertSame(a, consequent[0]);
   }

   /** [X] unified with [a] when X already unified to a */
   @Test
   public void testSingleAssignedVariableInInput_2() {
      Variable x = variable("X");
      Atom a1 = atom("a");
      Atom a2 = atom("a");

      x.unify(a1);

      PTerm[] input = {x};
      PTerm[] consequent = {a2};

      assertPreMatch(input, consequent);

      assertSame(x, input[0]);
      assertSame(a1, x.get());
      assertSame(a2, consequent[0]);
   }

   /** [X] unified with [b] when X already unified to a */
   @Test
   public void testSingleAssignedVariableInInput_3() {
      Variable x = variable("X");
      Atom a = atom("a");
      Atom b = atom("b");

      x.unify(a);

      PTerm[] input = {x};
      PTerm[] consequent = {b};

      assertFalse(Unifier.preMatch(input, consequent));

      assertSame(x, input[0]);
      assertSame(a, x.get());
      assertSame(b, consequent[0]);
   }

   /** [a, X] unified with [Y, b] */
   @Test
   public void testPrematch_1() {
      Atom a = atom("a");
      Variable x = variable("X");
      PTerm[] input = {a, x};

      Variable y = variable("Y");
      Atom b = atom("b");
      PTerm[] consequent = {y, b};

      assertPreMatch(input, consequent);

      assertSame(a, input[0]);
      assertSame(x, input[1]);
      assertSame(a, consequent[0]);
      assertSame(b, consequent[1]);

      assertSame(b, input[1].get());
   }

   /** [a, X, 1] unified with [Y, b, 2] */
   @Test
   public void testPrematch_2() {
      Atom a = atom("a");
      Variable x = variable("X");
      IntegerNumber i1 = integerNumber(1);
      PTerm[] input = {a, x, i1};

      Variable y = variable("Y");
      Atom b = atom("b");
      IntegerNumber i2 = integerNumber(2);
      PTerm[] consequent = {y, b, i2};

      assertPreMatchFailed(input, consequent);

      assertSame(x, x.get());
      assertSame(y, y.get());
   }

   /** [p(X), a] unified with [p(Y), Y] */
   @Test
   public void testPrematch_3() {
      Variable x = variable("X");
      PTerm inputArg1 = structure("p", x);
      PTerm inputArg2 = atom("a");
      PTerm[] input = {inputArg1, inputArg2};

      Variable y = variable("Y");
      PTerm consequentArg1 = structure("p", y);
      PTerm consequentArg2 = y;
      PTerm[] consequent = {consequentArg1, consequentArg2};

      assertPreMatch(input, consequent);

      // input args should still refer to the same term instances
      assertSame(inputArg1, input[0]);
      assertSame(inputArg2, input[1]);

      // consequent second argument should of been replaced with input second argument
      assertSame(inputArg2, consequent[1]);

      // predicate that is first argument of consquent should now have an atom as 
      // it's single argument rather than a variable
      assertSame(inputArg2, consequent[0].arg(0));

      // predicate that is first argument of input should still be a variable 
      // but now it should be unified with an atom
      assertSame(x, input[0].arg(0));
      assertSame(inputArg2, x.get());

      // unification of input argument should be undone on backtrack
      inputArg1.backtrack();
      assertSame(x, x.get());
   }

   /** [p(Y), Y] unified with [p(X), a] */
   @Test
   public void testPrematch_4() {
      Variable y = variable("Y");
      PTerm inputArg1 = structure("p", y);
      PTerm inputArg2 = y;
      PTerm[] input = {inputArg1, inputArg2};

      Variable x = variable("X");
      PTerm consequentArg1 = structure("p", x);
      PTerm consequentArg2 = atom("a");
      PTerm[] consequent = {consequentArg1, consequentArg2};

      assertPreMatch(input, consequent);

      assertSame(inputArg1, input[0]);
      assertSame(inputArg2, input[1]);
      assertSame(y, input[0].arg(0));
      assertSame(y, input[1]);
      assertSame(consequentArg2, y.get());

      assertSame(consequentArg2, consequent[0].arg(0));
      assertSame(consequentArg2, consequent[1]);

      // unification of input arguments should be undone on backtrack
      TermUtils.backtrack(input);
      assertSame(y, y.get());
      assertSame(y, input[0].arg(0));
      assertSame(y, input[1]);
   }

   /** [p(Y), Y] unified with [p(a), b] */
   @Test
   public void testPrematch_5() {
      Variable y = variable("Y");
      Structure inputArg1 = structure("p", y);
      PTerm[] input = {inputArg1, y};

      Structure consequentArg1 = structure("p", atom("a"));
      Atom consequentArg2 = atom("b");
      PTerm[] consequent = {consequentArg1, consequentArg2};

      assertPreMatchFailed(input, consequent);

      assertSame(inputArg1, input[0]);
      assertSame(y, input[1]);
      assertSame(y, input[0].arg(0));
      assertSame(y, y.get());
   }

   /** [p(a), b] unified with [p(Y), Y] */
   @Test
   public void testPrematch_6() {
      Variable y = variable("Y");

      assertPreMatchFailed(new PTerm[] {structure("p", atom("a")), atom("b")}, new PTerm[] {structure("p", y), y});
   }

   private void assertPreMatch(PTerm[] input, PTerm[] consequent) {
      assertTrue(Unifier.preMatch(input, consequent));
   }

   private void assertPreMatchFailed(PTerm[] input, PTerm[] consequent) {
      PTerm[] copyInput = copy(input);
      PTerm[] copyConsequent = copy(consequent);

      assertFalse(Unifier.preMatch(input, consequent));

      TermUtils.backtrack(input);
      for (int i = 0; i < input.length; i++) {
         assertSame(copyInput[i], input[i]);
         assertSame(copyInput[i], input[i].get());
      }

      TermUtils.backtrack(consequent);
      for (int i = 0; i < consequent.length; i++) {
         assertSame(copyConsequent[i], consequent[i]);
         assertSame(copyConsequent[i], consequent[i].get());
      }
   }

   private PTerm[] copy(PTerm[] in) {
      PTerm[] out = new PTerm[in.length];
      System.arraycopy(in, 0, out, 0, in.length);
      return out;
   }
}