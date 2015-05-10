package org.projog.core.term;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Static factory methods for creating new instances of {@link PList}.
 * 
 * @see PList
 * @see ListUtils
 */
public final class ListFactory {
   /**
    * A "{@code .}" is the functor name for all lists in Prolog.
    */
   public static final String LIST_PREDICATE_NAME = ".";

   /**
    * Private constructor as all methods are static.
    */
   private ListFactory() {
      // do nothing
   }

   /**
    * Returns a new {@link PList} with specified head and tail.
    * 
    * @param head the first argument in the list
    * @param tail the second argument in the list
    * @return a new {@link PList} with specified head and tail
    */
   public static PList createList(PTerm head, PTerm tail) {
      return createList(head, tail, head.constant() && tail.constant());
   }

   /**
    * Returns a new {@link PList} with the specified terms and a empty list as the final tail element.
    * 
    * @param terms contents of the list
    * @return a new {@link PList} with the specified terms and a empty list as the final tail element
    */
   public static PTerm createList(final Collection<PTerm> terms) {
      return createList(terms.toArray(new PTerm[terms.size()]));
   }

   /**
    * Returns a new {@link PList} with the specified terms and a empty list as the final tail element.
    * <p>
    * By having a {@code List} with a {@code List} as it's tail it is possible to represent an ordered sequence of the
    * specified terms.
    * 
    * @param terms contents of the list
    * @return a new {@link PList} with the specified terms and a empty list as the final tail element
    */
   public static PTerm createList(PTerm[] terms) {
      return createList(terms, EmptyList.EMPTY_LIST);
   }

   /**
    * Returns a new {@link PList} with the specified terms and the second parameter as the tail element.
    * <p>
    * By having a {@code List} with a {@code List} as it's tail it is possible to represent an ordered sequence of the
    * specified terms.
    * 
    * @param terms contents of the list
    * @return a new {@link PList} with the specified terms and the second parameter as the tail element
    */
   public static PTerm createList(PTerm[] terms, PTerm tail) {
      int numberOfElements = terms.length;
      if (numberOfElements == 0) {
         return EmptyList.EMPTY_LIST;
      }
      // keep track of whether sublists are immutable
      boolean isImmutable = tail.constant();
      PTerm list = tail;
      for (int i = numberOfElements - 1; i > -1; i--) {
         PTerm element = terms[i];
         isImmutable = isImmutable && element.constant();
         list = createList(element, list, isImmutable);
      }
      return list;
   }

   private static PList createList(PTerm head, PTerm tail, boolean isImmutable) {
      return new PList(head, tail, isImmutable);
   }

   /** Returns a new list of the specified length where is each element is a variable. */
   public static PTerm createListOfLength(final int length) {
      final java.util.List<PTerm> javaList = new ArrayList<PTerm>();
      for (int i = 0; i < length; i++) {
         javaList.add(new Variable("E" + i));
      }
      return createList(javaList);
   }
}