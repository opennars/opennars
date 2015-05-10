package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class OperandsTest {
   private static final String[] ASSOCIATIVITES = {"fx", "fy", "xfx", "xfy", "yfx", "xf", "yf"};

   @Test
   public void testInvalidAssociativity() {
      try {
         new Operands().addOperand("test", "yfy", 100);
         fail();
      } catch (ProjogException e) {
         assertEquals("Cannot add operand with associativity of: yfy as the only values allowed are: [xfx, xfy, yfx, fx, fy, xf, yf]", e.getMessage());
      }
   }

   @Test
   public void testDuplicate() {
      Operands operands = new Operands();
      operands.addOperand("test", "xfx", 100);
      // test can re-add if the same precedence and associativity
      operands.addOperand("test", "xfx", 100);
      try {
         // test can not re-add if different precedence
         operands.addOperand("test", "xfx", 101);
         fail();
      } catch (ProjogException e) {
         // expected
      }
      try {
         // test can not re-add if different associativity
         operands.addOperand("test", "xfy", 100);
         fail();
      } catch (ProjogException e) {
         // expected
      }
      operands.addOperand("test", "fx", 100);
   }

   @Test
   public void testOperands() {
      Operands operands = new Operands();

      List<TestOperand> testCases = new ArrayList<>();

      // add 3 operands for each type of associativity
      int ctr = 1;
      for (String associativity : ASSOCIATIVITES) {
         for (int i = 0; i < 3; i++) {
            TestOperand t = new TestOperand("name" + ctr, associativity, ctr);
            testCases.add(t);
            assertFalse(operands.isDefined(t.name));
            operands.addOperand(t.name, t.associativity, t.priority);
            assertTrue(operands.isDefined(t.name));
            ctr++;
         }
      }

      for (TestOperand t : testCases) {
         assertOperand(operands, t);
      }
   }

   private void assertOperand(Operands o, TestOperand t) {
      assertTrue(o.isDefined(t.name));
      assertEquals(t.prefix(), o.prefix(t.name));
      assertEquals(t.infix(), o.infix(t.name));
      assertEquals(t.postfix(), o.postfix(t.name));
      assertEquals(t.fx(), o.fx(t.name));
      assertEquals(t.fy(), o.fy(t.name));
      assertEquals(t.xfx(), o.xfx(t.name));
      assertEquals(t.xfy(), o.xfy(t.name));
      assertEquals(t.yfx(), o.yfx(t.name));
      assertEquals(t.xf(), o.xf(t.name));
      assertEquals(t.yf(), o.yf(t.name));

      try {
         assertEquals(t.priority, o.getPrefixPriority(t.name));
         assertTrue(t.prefix());
      } catch (NullPointerException e) {
         assertFalse(t.prefix());
      }
      try {
         assertEquals(t.priority, o.getInfixPriority(t.name));
         assertTrue(t.infix());
      } catch (NullPointerException e) {
         assertFalse(t.infix());
      }
      try {
         assertEquals(t.priority, o.getPostfixPriority(t.name));
         assertTrue(t.postfix());
      } catch (NullPointerException e) {
         assertFalse(t.postfix());
      }
   }

   private static class TestOperand {
      final String name;
      final String associativity;
      final int priority;

      TestOperand(String name, String associativity, int priority) {
         this.name = name;
         this.associativity = associativity;
         this.priority = priority;
      }

      boolean prefix() {
         return fx() || fy();
      }

      boolean infix() {
         return xfx() || xfy() || yfx();
      }

      boolean postfix() {
         return xf() || yf();
      }

      boolean fx() {
         return "fx".equals(associativity);
      }

      boolean fy() {
         return "fy".equals(associativity);
      }

      boolean xfx() {
         return "xfx".equals(associativity);
      }

      boolean xfy() {
         return "xfy".equals(associativity);
      }

      boolean yfx() {
         return "yfx".equals(associativity);
      }

      boolean xf() {
         return "xf".equals(associativity);
      }

      boolean yf() {
         return "yf".equals(associativity);
      }
   }
}