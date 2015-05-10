package org.projog.core.term;

import java.util.Map;

/**
 * Represents a data structure with no {@link PTerm}s.
 * 
 * @see PList
 * @see ListFactory
 */
public final class EmptyList implements PTerm {
   /**
    * Singleton instance
    */
   public static final EmptyList EMPTY_LIST = new EmptyList();

   /**
    * Private constructor to force use of {@link #EMPTY_LIST}
    */
   private EmptyList() {
      // do nothing
   }

   @Override
   public void backtrack() {
      // do nothing
   }

   @Override
   public PTerm copy(Map<Variable, Variable> sharedVariables) {
      return EMPTY_LIST;
   }

   @Override
   public EmptyList get() {
      return EMPTY_LIST;
   }

   @Override
   public boolean constant() {
      return true;
   }

   /**
    * @throws UnsupportedOperationException as this implementation of {@link PTerm} has no arguments
    */
   @Override
   public PTerm[] getArgs() {
      throw new UnsupportedOperationException();
   }

   @Override
   public int args() {
      return 0;
   }

   @Override
   public PTerm arg(int index) {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns {@link ListFactory#LIST_PREDICATE_NAME}.
    * 
    * @return {@link ListFactory#LIST_PREDICATE_NAME}
    */
   @Override
   public String getName() {
      return ListFactory.LIST_PREDICATE_NAME;
   }

   /**
    * Returns {@link TermType#EMPTY_LIST}.
    * 
    * @return {@link TermType#EMPTY_LIST}
    */
   @Override
   public TermType type() {
      return TermType.EMPTY_LIST;
   }

   /**
    * Performs a strict comparison of this term to the specified term.
    * 
    * @param t the term to compare this term against
    * @return {@code true} if the given term represents a {@link TermType#EMPTY_LIST}
    */
   @Override
   public boolean strictEquals(PTerm t) {
      return t.type() == TermType.EMPTY_LIST;
   }

   @Override
   public boolean unify(PTerm t) {
      TermType tType = t.type();
      if (tType == TermType.EMPTY_LIST) {
         return true;
      } else if (tType.isVariable()) {
         return t.unify(this);
      } else {
         return false;
      }
   }

   /**
    * @return {@code []}
    */
   @Override
   public String toString() {
      return "[]";
   }
}