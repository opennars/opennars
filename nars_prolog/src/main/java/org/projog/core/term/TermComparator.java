package org.projog.core.term;

import java.util.Comparator;

import org.projog.core.ProjogException;

/**
 * An implementation of {@code Comparator} for comparing instances of {@link PTerm}.
 * 
 * @see #compare(PTerm, PTerm)
 * @see NumericTermComparator
 * @see PTerm#strictEquals(PTerm)
 */
public final class TermComparator implements Comparator<PTerm> {
   /**
    * Singleton instance
    */
   public static final TermComparator TERM_COMPARATOR = new TermComparator();

   /**
    * Private constructor to force use of {@link #TERM_COMPARATOR}
    */
   private TermComparator() {
      // do nothing
   }

   /**
    * Compares the two arguments for order.
    * <p>
    * Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
    * than the second.
    * <p>
    * The criteria for deciding the order of terms is as follows:
    * <ul>
    * <li>All uninstantiated variables are less than all floating point numbers, which are less than all integers, which
    * are less than all atoms, which are less than all structures (including lists).</li>
    * <li>Comparison of two integer or two floating point numbers is done using {@link NumericTermComparator}.</li>
    * <li>Comparison of two atoms is done by comparing the {@code String} values they represent using
    * {@code String.compareTo(String)}.</li>
    * <li>One structure is less than another if it has a lower arity (number of arguments). If two structures have the
    * same arity then they are ordered by comparing their functors (names) (determined by
    * {@code String.compareTo(String)}). If two structures have the same arity and functor then they are ordered by
    * comparing their arguments in order. The first corresponding arguments that differ determines the order of the two
    * structures.</li>
    * </ul>
    * 
    * @param t1 the first term to be compared
    * @param t2 the second term to be compared
    * @return a negative integer, zero, or a positive integer as the first term is less than, equal to, or greater than
    * the second
    */
   @Override
   public int compare(PTerm t1, PTerm t2) {
      PTerm v1 = t1.get();
      PTerm v2 = t2.get();

      // if the both arguments refer to the same object then must be identical
      // this deals with the case where both arguments are empty lists
      // or both are an anonymous variable
      if (v1.get() == v2.get()) {
         return 0;
      }

      PrologOperator type1 = v1.type();
      PrologOperator type2 = v2.type();

      if (type1.isStructure() && type2.isStructure()) {
         return compareStructures(v1, v2);
      } else if (type1 != type2) {
         return type1.getPrecedence() > type2.getPrecedence() ? 1 : -1;
      } else {
         switch (type1) {
            case FRACTION:
            case INTEGER:
               return NumericTermComparator.NUMERIC_TERM_COMPARATOR.compare(v1, v2);
            case ATOM:
               return t1.getName().compareTo(t2.getName());
            case NAMED_VARIABLE:
               // NOTE: uses Object's hashCode which is not guaranteed
               // so may get different results in different jvms
               return v1.hashCode() > v2.hashCode() ? 1 : -1;
            default:
               throw new ProjogException("Unknown TermType: " + type1);
         }
      }
   }

   private int compareStructures(PTerm t1, PTerm t2) {
      // compare number of arguments
      int t1Length = t1.length();
      int t2Length = t2.length();
      if (t1Length != t2Length) {
         return t1Length > t2Length ? 1 : -1;
      }

      // compare predicate names
      int nameComparison = t1.getName().compareTo(t2.getName());
      if (nameComparison != 0) {
         return nameComparison;
      }

      // compare arguments one at a time
      for (int i = 0; i < t1Length; i++) {
         int argComparison = compare(t1.term(i), t2.term(i));
         if (argComparison != 0) {
            return argComparison;
         }
      }

      // if still cannot separate then consider them identical
      return 0;
   }
}