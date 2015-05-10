package org.projog.core.term;

import java.util.Map;

/**
 * Represents a data structure with two {@link PTerm}s - a head and a tail.
 * <p>
 * The head and tail can be any {@link PTerm}s - including other {@code List}s. By having a {@code List} with a
 * {@code List} as it's tail it is possible to represent an ordered sequence of {@link PTerm}s of any length. The end of
 * an ordered sequence of {@link PTerm}s is normally represented as a tail having the value of an {@link EmptyList}.
 * 
 * @see EmptyList
 * @see ListFactory
 * @see ListUtils
 */
public final class PList implements PTerm {
   private final PTerm head;
   private PTerm tail;
   private final boolean immutable;

   /**
    * Creates a new list with the specified head and tail.
    * <p>
    * Use {@link ListFactory} rather than calling directly.
    * 
    * @param head the head of the new list
    * @param tail the tail of the new list
    * @param immutable is this list immutable (i.e. are both the head and tail known to be immutable)?
    */
   PList(PTerm head, PTerm tail, boolean immutable) {
      this.head = head;
      this.tail = tail;
      this.immutable = immutable;
   }

   /**
    * Replaces the tail of the list with the specified term.
    * <p>
    * <b>Note:</b> This method has only been added to make it easier to optimise tail-recursive functions. It's use is
    * not recommend as altering the tail of a list after it has been created may cause unexpected behaviour.
    * <p>
    * TODO Find an alternative to this method for doing tail-recursive optimisation.
    * 
    * @param tail term to set as the tail of this object
    * @deprecated only used to make tail recursive functions more efficient
    */
   @Deprecated
   public void setTail(PTerm tail) {
      this.tail = tail;
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

   @Override
   public PTerm[] getArgs() {
      throw new UnsupportedOperationException();
   }

   @Override
   public int args() {
      return 2;
   }

   @Override
   public PTerm arg(int index) {
      return index == 0 ? head : tail;
   }

   /**
    * Returns {@link TermType#LIST}.
    * 
    * @return {@link TermType#LIST}
    */
   @Override
   public TermType type() {
      return TermType.LIST;
   }

   @Override
   public boolean constant() {
      return immutable;
   }

   @Override
   public PList get() {
      if (immutable) {
         return this;
      } else {
         PTerm newHead = head.get();
         PTerm newTail = tail.get();
         if (newHead == head && newTail == tail) {
            return this;
         } else {
            return new PList(newHead, newTail, newHead.constant() && newTail.constant());
         }
      }
   }

   @Override
   public PList copy(Map<Variable, Variable> sharedVariables) {
      if (immutable) {
         return this;
      } else {
         PTerm newHead = head.copy(sharedVariables);
         PTerm newTail = tail.copy(sharedVariables);
         if (newHead == head && newTail == tail) {
            return this;
         } else {
            return new PList(newHead, newTail, newHead.constant() && newTail.constant());
         }
      }
   }

   @Override
   public boolean unify(PTerm t1) {
      // used to be implemented using recursion but caused stack overflow problems with long lists
      PTerm t2 = this;
      do {
         TermType tType = t1.type();
         if (tType == TermType.LIST) {
            if (t2.arg(0).unify(t1.arg(0)) == false) {
               return false;
            }
            t1 = t1.arg(1);
            t2 = t2.arg(1);
         } else if (tType.isVariable()) {
            return t1.unify(t2);
         } else {
            return false;
         }
      } while (t2.type() == TermType.LIST);
      return t2.unify(t1);
   }

   /**
    * Performs a strict comparison of this list to the specified term.
    * 
    * @param t1 the term to compare this list against
    * @return {@code true} if the given term represents a {@link TermType#LIST} with a head and tail strictly equal to
    * the corresponding head and tail of this List object.
    */
   @Override
   public boolean strictEquals(PTerm t1) {
      // used to be implemented using recursion but caused stack overflow problems with long lists
      PTerm t2 = this;
      do {
         boolean equal = t1.type() == TermType.LIST && t1.arg(0).strictEquals(t2.arg(0));
         if (equal == false) {
            return false;
         }
         t1 = t1.arg(1);
         t2 = t2.arg(1);
      } while (t2.type() == TermType.LIST);
      return t1.strictEquals(t2);
   }

   @Override
   public void backtrack() {
      if (!immutable) {
         head.backtrack();
         tail.backtrack();
      }
   }

   @Override
   public String toString() {
      // used to be implemented using recursion but caused stack overflow problems with long listsSS
      StringBuilder sb = new StringBuilder();
      int listCtr = 0;
      PTerm t = this;
      do {
         sb.append(ListFactory.LIST_PREDICATE_NAME);
         sb.append("(");
         sb.append(t.arg(0));
         sb.append(", ");
         t = t.arg(1);
         listCtr++;
      } while (t.type() == TermType.LIST);
      sb.append(t);
      for (int i = 0; i < listCtr; i++) {
         sb.append(")");
      }
      return sb.toString();
   }
}