package org.projog.core;

import org.projog.core.term.PTerm;

/**
 * Represents a goal.
 * <p>
 * <b>Note:</b> Rather than directly implementing {@code Predicate} it is recommended to extend either
 * {@link org.projog.core.function.AbstractSingletonPredicate} or
 * {@link org.projog.core.function.AbstractRetryablePredicate}.
 * 
 * @see PredicateFactory
 * @see KB#addFactory(PredicateKey, String)
 */
public interface Predicate {
   /**
    * Attempts to satisfy the goal this instance represents.
    * <p>
    * Calling this method multiple times on a single instance allows all possible answers to be identified. An attempt
    * to find a solution carries on from where the last successful call finished.
    * <p>
    * If {@link #isRetryable()} returns {@code false} then this method should only be called once per individual query
    * (no attempt should be made to find alternative solutions).
    * <p>
    * If {@link #isRetryable()} returns {@code true} then, in order to find all possible solutions for an individual
    * query, this method should be recalled on backtracking until it returns {@code false}.
    * <p>
    * <b>Note:</b> It is recommended that implementations of {@code Predicate} also implement an overloaded version of
    * {@code evaluate} that, instead of having a single varargs parameter, accepts a number of individual {@code Term}
    * parameters. The exact number of parameters accepted should be the same as the number of arguments expected when
    * evaluating the goal this object represents. For example, a {@code Predicate} that does not expect any arguments
    * should implement {@code public boolean evaluate()} while a {@code Predicate} that expects three arguments should
    * implement {@code public boolean evaluate(Term, Term, Term)}. The reason why this is recommended is so that java
    * code generated at runtime for user defined predicates will be able to use the overloaded method rather than the
    * varargs version and thus avoid the unnecessary overhead of creating a new {@code Term} array for each method
    * invocation.
    * 
    * @param args the arguments to use in the evaluation of this goal
    * @return {@code true} if it was possible to satisfy the clause, {@code false} otherwise
    * @see PredicateFactory#getPredicate(PTerm[])
    */
   boolean evaluate(PTerm... args);

   /**
    * Should instances of this implementation be re-evaluated when backtracking?
    * <p>
    * Some goals (e.g. {@code X is 1}) are only meant to be evaluated once (the statement is either true or false) while
    * others (e.g. {@code repeat(3)}) are meant to be evaluated multiple times. For instances of {@code Predicate} that
    * are designed to possibly have {@link #evaluate(PTerm[])} called on them multiple times for the same individual
    * query this method should return {@code true}. For instances of {@code Predicate} that are designed to only be
    * evaluated once per individual query this method should return {@code false}.
    * 
    * @return {@code true} if an attempt should be made to re-evaluate instances of implementing classes when
    * backtracking, {@code false} otherwise
    */
   boolean isRetryable();

   /**
    * Could the next re-evaluation of this instance succeed?
    * <p>
    * Specifies whether a specific instance of a specific implementation of {@code Predicate}, that has already had
    * {@link #evaluate(PTerm[])} called on it at least once, could possibly return {@code true} the next time
    * {@link #evaluate(PTerm[])} is called on it. i.e. is it worth trying to continue to find solutions for the specific
    * query this particular instance represents and has been evaluating?
    * <p>
    * (Note: the difference between this method and {@link #isRetryable()} is that {@link #isRetryable()} deals with
    * whether, in general, a specific <i>implementation</i> (rather than <i>instance</i>) of {@code Predicate} could
    * <i>ever</i> produce multiple answers for an individual query.)
    * 
    * @return {@code true} if an attempt to re-evaluate this instance could possible succeed, {@code false} otherwise
    */
   boolean couldReEvaluationSucceed();
}