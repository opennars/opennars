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
   public PTerm[] getArgs() {
      return TermUtils.EMPTY_ARRAY;
   }

   @Override
   public int args() {
      return 0;
   }

   /**
    * @throws UnsupportedOperationException as this implementation of {@link PTerm} has no arguments
    */
   @Override
   public PTerm arg(int index) {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns {@link TermType#INTEGER}.
    * 
    * @return {@link TermType#INTEGER}
    */
   @Override
   public TermType type() {
      return TermType.INTEGER;
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
   public IntegerNumber copy(Map<Variable, Variable> sharedVariables) {
      return this;
   }

   @Override
   public boolean unify(PTerm t) {
      TermType tType = t.type();
      if (tType == TermType.INTEGER) {
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
    * @return {@code true} if the given term represents a {@link TermType#INTEGER} with a value equal to the value of
    * this {@code IntegerNumber}
    */
   @Override
   public boolean strictEquals(PTerm t) {
      return t.type() == TermType.INTEGER && value == ((IntegerNumber) t.get()).value;
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