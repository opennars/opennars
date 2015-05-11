package org.projog.core.term;

import java.util.Map;

/**
 * Represents a specific object or relationship.
 * <p>
 * Atoms are constant; their values cannot be changed after they are created. Atoms have no arguments.
 */
public final class PAtom implements PTerm {
   private final String value;

   /**
    * @param value the value this {@code Atom} represents
    */
   public PAtom(String value) {
      this.value = value;
   }

   /**
    * Returns the value this {@code Atom} represents.
    * 
    * @return the value this {@code Atom} represents
    */
   @Override
   public String getName() {
      return value;
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
    * Returns {@link PrologOperator#ATOM}.
    * 
    * @return {@link PrologOperator#ATOM}
    */
   @Override
   public PrologOperator type() {
      return PrologOperator.ATOM;
   }

   @Override
   public boolean constant() {
      return true;
   }

   @Override
   public PAtom copy(Map<PVar, PVar> sharedVariables) {
      return this;
   }

   @Override
   public PAtom get() {
      return this;
   }

   @Override
   public boolean unify(PTerm t) {
      PrologOperator tType = t.type();
      if (tType == PrologOperator.ATOM) {
         return value.equals(t.getName());
      } else if (tType.isVariable()) {
         return t.unify(this);
      } else {
         return false;
      }
   }

   /**
    * Performs a strict comparison of this atom to the specified term.
    * 
    * @param t the term to compare this atom against
    * @return {@code true} if the given term represents a {@link PrologOperator#ATOM} with a value equal to the value of this
    * atom
    */
   @Override
   public boolean strictEquals(PTerm t) {
      return t.type() == PrologOperator.ATOM && value.equals(t.getName());
   }

   @Override
   public void backtrack() {
      // do nothing
   }

   /**
    * @return {@link #getName()}
    */
   @Override
   public String toString() {
      return getName();
   }
}