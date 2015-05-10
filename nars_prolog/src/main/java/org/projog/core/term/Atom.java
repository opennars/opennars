package org.projog.core.term;

import java.util.Map;

/**
 * Represents a specific object or relationship.
 * <p>
 * Atoms are constant; their values cannot be changed after they are created. Atoms have no arguments.
 */
public final class Atom implements PTerm {
   private final String value;

   /**
    * @param value the value this {@code Atom} represents
    */
   public Atom(String value) {
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
    * Returns {@link TermType#ATOM}.
    * 
    * @return {@link TermType#ATOM}
    */
   @Override
   public TermType type() {
      return TermType.ATOM;
   }

   @Override
   public boolean constant() {
      return true;
   }

   @Override
   public Atom copy(Map<Variable, Variable> sharedVariables) {
      return this;
   }

   @Override
   public Atom get() {
      return this;
   }

   @Override
   public boolean unify(PTerm t) {
      TermType tType = t.type();
      if (tType == TermType.ATOM) {
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
    * @return {@code true} if the given term represents a {@link TermType#ATOM} with a value equal to the value of this
    * atom
    */
   @Override
   public boolean strictEquals(PTerm t) {
      return t.type() == TermType.ATOM && value.equals(t.getName());
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