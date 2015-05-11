package org.projog.core.term;

import java.util.Map;

/**
 * Represents a value of the primitive type {@code long} as a {@link PTerm}.
 * <p>
 * IntegerNumbers are constant; their values cannot be changed after they are created. IntegerNumbers have no arguments.
 */
public final class IntegerNumber implements Numeric {
   private final long value;

   /**
    * @param value the value this term represents
    */
   public IntegerNumber(long value) {
      this.value = value;
   }

   /**
    * Returns a {@code String} representation of the {@code long} this term represents.
    * 
    * @return a {@code String} representation of the {@code long} this term represents
    */
   @Override
   public String getName() {
      return toString();
   }

   @Override
   public PTerm[] terms() {
      return TermUtils.EMPTY_ARRAY;
   }

   @Override
   public int length() {
      return 0;
   }

   /**
    * @throws UnsupportedOperationException as this implementation of {@link PTerm} has no arguments
    */
   @Override
   public PTerm term(int index) {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns {@link PrologOperator#INTEGER}.
    * 
    * @return {@link PrologOperator#INTEGER}
    */
   @Override
   public PrologOperator type() {
      return PrologOperator.INTEGER;
   }

   @Override
   public boolean constant() {
      return true;
   }

   @Override
   public IntegerNumber get() {
      return this;
   }

   @Override
   public IntegerNumber copy(Map<PVar, PVar> sharedVariables) {
      return this;
   }

   @Override
   public boolean unify(PTerm t) {
      PrologOperator tType = t.type();
      if (tType == PrologOperator.INTEGER) {
         return value == ((IntegerNumber) t.get()).value;
      } else if (tType.isVariable()) {
         return t.unify(this);
      } else {
         return false;
      }
   }

   /**
    * Performs a strict comparison of this term to the specified term.
    * 
    * @param t the term to compare this term against
    * @return {@code true} if the given term represents a {@link PrologOperator#INTEGER} with a value equal to the value of
    * this {@code IntegerNumber}
    */
   @Override
   public boolean strictEquals(PTerm t) {
      return t.type() == PrologOperator.INTEGER && value == ((IntegerNumber) t.get()).value;
   }

   @Override
   public void backtrack() {
      // do nothing
   }

   /**
    * @return the {@code long} value of this term
    */
   @Override
   public long getLong() {
      return value;
   }

   /**
    * @return the {@code long} value of this term cast to a {@code double}
    */
   @Override
   public double getDouble() {
      return value;
   }

   /**
    * @return a {@code String} representation of the {@code long} this term represents
    */
   @Override
   public String toString() {
      return Long.toString(value);
   }
}