package org.projog.core.term;

import static org.projog.core.term.TermComparator.TERM_COMPARATOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper methods for performing common tasks with Prolog list data structures.
 * 
 * @see List
 * @see ListFactory
 * @see TermUtils
 */
public class ListUtils {
   /**
    * Private constructor as all methods are static.
    */
   private ListUtils() {
      // do nothing
   }

   /**
    * Returns a new {@code java.util.List} containing the contents of the specified {@code org.projog.core.term.List}.
    * <p>
    * Will return {@code null} if {@code list} is neither of type {@link PrologOperator#LIST} or {@link PrologOperator#EMPTY_LIST},
    * or if {@code list} represents a partial list (i.e. a list that does not have an empty list as its tail).
    * </p>
    * 
    * @see #toSortedJavaUtilList(PTerm)
    */
   public static List<PTerm> toJavaUtilList(PTerm list) {
      if (list.type() == PrologOperator.LIST) {
         final List<PTerm> result = new ArrayList<PTerm>();
         do {
            result.add(list.term(0));
            list = list.term(1);
         } while (list.type() == PrologOperator.LIST);

         if (list.type() == PrologOperator.EMPTY_LIST) {
            return result;
         } else {
            // partial list
            return null;
         }
      } else if (list.type() == PrologOperator.EMPTY_LIST) {
         return Collections.emptyList();
      } else {
         // not a list
         return null;
      }
   }

   /**
    * Returns a new {@code java.util.List} containing the sorted contents of the specified
    * {@code org.projog.core.term.List}.
    * <p>
    * The elements in the returned list will be ordered using the standard ordering of terms, as implemented by
    * {@link TermComparator}.
    * </p>
    * <p>
    * Will return {@code null} if {@code list} is neither of type {@link PrologOperator#LIST} or {@link PrologOperator#EMPTY_LIST},
    * or if {@code list} represents a partial list (i.e. a list that does not have an empty list as its tail).
    * </p>
    * 
    * @see #toJavaUtilList(PTerm)
    */
   public static List<PTerm> toSortedJavaUtilList(PTerm unsorted) {
      List<PTerm> elements = toJavaUtilList(unsorted);
      if (elements != null) {
         Collections.sort(elements, TERM_COMPARATOR);
      }
      return elements;
   }

   /**
    * Checks is a term can be unified with at least one element of a list.
    * <p>
    * Iterates through each element of {@code list} attempting to unify with {@code element}. Returns {@code true}
    * immediately after the first unifiable element is found. If {@code list} contains no elements that can be unified
    * with {@code element} then {@code false} is returned.
    * </p>
    * 
    * @throws IllegalArgumentException if {@code list} is not of type {@code TermType#LIST} or {@code TermType#EMPTY_LIST}
    */
   public static boolean isMember(PTerm element, PTerm list) {
      if (list.type() != PrologOperator.LIST && list.type() != PrologOperator.EMPTY_LIST) {
         throw new IllegalArgumentException("Expected list but got: " + list);
      }
      while (list.type() == PrologOperator.LIST) {
         if (element.unify(list.term(0))) {
            return true;
         }
         element.backtrack();
         list.backtrack();
         list = list.term(1);
      }
      return false;
   }
}
