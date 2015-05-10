package org.projog.core.udp.compiler;

import org.projog.core.udp.TailRecursivePredicate;
import org.projog.core.udp.TailRecursivePredicateMetaData;

/**
 * A super-class of all compiled "tail recursion optimised" user defined predicates.
 * <p>
 * For a user defined predicate to be implemented using {@code CompiledTailRecursivePredicate} it must be judged as
 * eligible for <i>tail recursion optimisation</i> using the criteria used by {@link TailRecursivePredicateMetaData}.
 * </p>
 * 
 * @see TailRecursivePredicateMetaData
 */
public abstract class CompiledTailRecursivePredicate extends TailRecursivePredicate implements CompiledPredicate {
   public abstract boolean[] isSingleResultIfArgumentImmutable();
}