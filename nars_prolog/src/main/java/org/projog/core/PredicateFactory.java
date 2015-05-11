package org.projog.core;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/**
 * Returns specialised implementations of {@link Predicate}.
 * <p>
 * There are two general types of predicates:
 * <ul>
 * <li><i>User defined predicates</i> are defined by a mixture of rules and facts constructed from Prolog syntax
 * consulted at runtime.</li>
 * <li><i>Plugin predicates</i> are written in Java. Plugin predicates can provide facilities that would not be possible
 * using pure Prolog syntax. The two predicates that are always available in Projog are {@code pj_add_predicate/2} and
 * {@code pj_add_calculatable/2}. The {@code pj_add_predicate/2} predicate allows other predicates to be "plugged-in" to
 * Projog.</li>
 * </ul>
 * <p>
 * <b>Note:</b> Rather than directly implementing {@code PredicateFactory} it is recommended to extend either
 * {@link org.projog.core.function.AbstractSingletonPredicate} or
 * {@link org.projog.core.function.AbstractRetryablePredicate}.
 * <p>
 * <p>
 * <a href="doc-files/PredicateFactory.png">View Class Diagram</a>
 * </p>
 * 
 * @see KB#addFactory(PredicateKey, String)
 */
public interface PredicateFactory {
   /**
    * Provides a reference to a {@code KnowledgeBase}.
    * <p>
    * This method will be called by {@link KB#addFactory(PredicateKey, String)} when this class is
    * registered with a {@code KnowledgeBase} - meaning this object will always have access to a {@code KnowledgeBase}
    * by the time it's {@code getPredicate} method is invoked.
    */
   void setKB(KB kb);

   /**
    * Returns a {@link Predicate} to be used in the evaluation of a goal.
    * <p>
    * <b>Note:</b> It is recommended that implementations of {@code PredicateFactory} also implement an overloaded
    * version of {@code getPredicate} that, instead of having a single varargs parameter, accepts a number of individual
    * {@code Term} parameters. The exact number of parameters accepted should be the same as the number of arguments
    * expected when evaluating the goal this object represents. For example, a {@code PredicateFactory} that does not
    * expect any arguments should implement {@code getPredicate()} while a {@code PredicateFactory} that expects three
    * arguments should implement {@code getPredicate(Term, Term, Term)}. The reason why this is recommended is so that
    * java code generated at runtime for user defined predicates will be able to use the overloaded method rather than
    * the varargs version and thus avoid the unnecessary overhead of creating a new {@code Term} array for each method
    * invocation.
    * </p>
    * <p>
    * <b>Note:</b> The above recommendations are <i>not</i> required for subclasses of
    * {@link AbstractSingletonPredicate}. (As the compiler is aware that
    * {@link AbstractSingletonPredicate#getPredicate(PTerm...)} always returns {@code this}.
    * </p>
    * 
    * @param args the arguments to use in the evaluation of the goal
    * @return Predicate to be used in the evaluation of the goal
    * @see Predicate#evaluate(PTerm[])
    */
   Predicate getPredicate(PTerm... args);
}