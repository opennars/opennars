package org.projog.core.udp.compiler;

/**
 * Generates Java source code for a particular predicate call of a user defined predicate.
 * <p>
 * A modular solution to provide the functionality to transform user defined predicates, specified using standard Prolog
 * syntax, into Java code at runtime.
 */
interface PredicateInvocationGenerator {
   /**
    * Generates the Java source code to call the current clause of the specified {@link CompiledPredicateWriter}.
    */
   void generate(CompiledPredicateWriter w);
}