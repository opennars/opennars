package org.projog.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Collection of operands.
 * <p>
 * Prolog allows functors (names of predicates) to be defined as "operators". The use of operators allows syntax to be
 * easier to read.
 * <p>
 * Infix operators are placed between their two arguments. Prefix operators are placed before their single argument.
 * Postfix operators are placed after their single argument.
 * <p>
 * A common use of operators is in the definition of arithmetic operations. By declaring {@code is} and {@code -} as
 * infix operators we can write valid prolog syntax like {@code X is 1 + 2.} instead of {@code is(X, +(1, 2)).}
 * <p>
 * Each {@link org.projog.core.KnowledgeBase} has a single unique {@code Operands} instance.
 * 
 * @see KnowledgeBaseUtils#getOperands(KnowledgeBase)
 */
public final class Operands {
   private final Object LOCK = new Object();

   private final Map<String, Operand> infixOperands = new HashMap<>();

   private final Map<String, Operand> prefixOperands = new HashMap<>();

   private final Map<String, Operand> postfixOperands = new HashMap<>();

   /**
    * Adds a new operator.
    * 
    * @param operandName the name of the new operator
    * @param associativityName the operators associativity (must be one of: xfx, xfy, yfx, fx, fy, xf or yf)
    * @param precedence used to specify the ordering of terms where it is not made explicit by the use of brackets
    */
   public void addOperand(String operandName, String associativityName, int precedence) {
      Associativity a = getAssociativity(associativityName);
      Map<String, Operand> operandsMap = getOperandsMap(a);
      synchronized (LOCK) {
         if (operandsMap.containsKey(operandName)) {
            Operand o = operandsMap.get(operandName);
            // if the operand is already registered throw an exception if the precedence is different else do nothing
            if (o.precedence != precedence || o.associativity != a) {
               throw new ProjogException("Operand: " + operandName + " with associativity: " + o.associativity + " and precedence: " + o.precedence + " already exists");
            }
         } else {
            operandsMap.put(operandName, new Operand(a, precedence));
         }
      }
   }

   private Associativity getAssociativity(String associativityName) {
      try {
         return Associativity.valueOf(associativityName);
      } catch (IllegalArgumentException e) {
         throw new ProjogException("Cannot add operand with associativity of: " + associativityName + " as the only values allowed are: " + Arrays.toString(Associativity.values()));
      }
   }

   private Map<String, Operand> getOperandsMap(Associativity a) {
      switch (a.location) {
         case INFIX:
            return infixOperands;
         case PREFIX:
            return prefixOperands;
         case POSTFIX:
            return postfixOperands;
      }
      // the Associativity enum currently only has 3 values, all of which are included in the above switch statement - so should never get here
      throw new ProjogException("Do not support associativity: " + a);
   }

   /** Returns the priority (precedence/level) of the infix operator represented by {@code op}. */
   public int getInfixPriority(String op) {
      return infixOperands.get(op).precedence;
   }

   /** Returns the priority (precedence/level) of the prefix operator represented by {@code op}. */
   public int getPrefixPriority(String op) {
      return prefixOperands.get(op).precedence;
   }

   /** Returns the priority (precedence/level) of the postfix operator represented by {@code op}. */
   public int getPostfixPriority(String op) {
      return postfixOperands.get(op).precedence;
   }

   /**
    * Returns {@code true} if {@code op} represents an infix operator, else {@code false}.
    */
   public boolean infix(String op) {
      return infixOperands.containsKey(op);
   }

   /**
    * Returns {@code true} if {@code op} represents an infix operator with associativity of {@code yfx}, else
    * {@code false}.
    */
   public boolean yfx(String op) {
      return infix(op) && infixOperands.get(op).associativity == Associativity.yfx;
   }

   /**
    * Returns {@code true} if {@code op} represents an infix operator with associativity of {@code xfy}, else
    * {@code false}.
    */
   public boolean xfy(String op) {
      return infix(op) && infixOperands.get(op).associativity == Associativity.xfy;
   }

   /**
    * Returns {@code true} if {@code op} represents an infix operator with associativity of {@code xfx}, else
    * {@code false}.
    */
   public boolean xfx(String op) {
      return infix(op) && infixOperands.get(op).associativity == Associativity.xfx;
   }

   /**
    * Returns {@code true} if {@code op} represents a prefix operator, else {@code false}.
    */
   public boolean prefix(String op) {
      return prefixOperands.containsKey(op);
   }

   /**
    * Returns {@code true} if {@code op} represents a prefix operator with associativity of {@code fx}, else
    * {@code false}.
    */
   public boolean fx(String op) {
      return prefix(op) && prefixOperands.get(op).associativity == Associativity.fx;
   }

   /**
    * Returns {@code true} if {@code op} represents a prefix operator with associativity of {@code fy}, else
    * {@code false}.
    */
   public boolean fy(String op) {
      return prefix(op) && prefixOperands.get(op).associativity == Associativity.fy;
   }

   /**
    * Returns {@code true} if {@code op} represents a postfix operator, else {@code false}.
    */
   public boolean postfix(String op) {
      return postfixOperands.containsKey(op);
   }

   /**
    * Returns {@code true} if {@code op} represents a postfix operator with associativity of {@code xf}, else
    * {@code false}.
    */
   public boolean xf(String op) {
      return postfix(op) && postfixOperands.get(op).associativity == Associativity.xf;
   }

   /**
    * Returns {@code true} if {@code op} represents a postfix operator with associativity of {@code yf}, else
    * {@code false}.
    */
   public boolean yf(String op) {
      return postfix(op) && postfixOperands.get(op).associativity == Associativity.yf;
   }

   /** Returns {@code true} if {@code commandName} represents any known operator, else {@code false}. */
   public boolean isDefined(String commandName) {
      return infix(commandName) || prefix(commandName) || postfix(commandName);
   }

   private static class Operand {
      final Associativity associativity;

      final int precedence;

      Operand(Associativity associativity, int precedence) {
         this.associativity = associativity;
         this.precedence = precedence;
      }
   }

   /**
    * Associativity is used to specify rules over operators in the same expression that have the same priority.
    * <p>
    * A "y" means that the argument can contain operators of <i>the same</i> or lower level of priority than the
    * operator represented by "f", while a "x" means that the argument can <i>only</i> contain operators of a lower
    * priority.
    */
   private static enum Associativity {
      xfx(Location.INFIX),
      xfy(Location.INFIX),
      yfx(Location.INFIX),
      fx(Location.PREFIX),
      fy(Location.PREFIX),
      xf(Location.POSTFIX),
      yf(Location.POSTFIX);

      final Location location;

      Associativity(Location location) {
         this.location = location;
      }
   }

   private static enum Location {
      /**
       * An operator that is positioned directly <i>before</i> it's single argument.
       * <p>
       * e.g. {@code - X} where {@code -} is the operator (negation) and {@code X} is it's argument.
       */
      PREFIX,
      /**
       * An operator that is positioned <i>between</i> it's two argument.
       * <p>
       * e.g. {@code X = 3} where {@code =} is the operator with the arguments {@code X} and {@code 3}.
       */
      INFIX,
      /** An operator that is positioned directly <i>after</i> it's single argument. */
      POSTFIX
   }
}