package org.projog.core.udp.compiler;

import org.projog.core.PredicateFactory;
import org.projog.core.udp.compiler.NumericComparisonPredicateInvocationGenerator.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a collection of {@link PredicateInvocationGenerator} instances.
 * <p>
 * Provides a mapping between {@code PredicateFactory} classes (which provided functionality in a generic interpreted
 * manner) to corresponding {@code PredicateInvocationGenerator} classes (which output Java source code to implement
 * functionality in a specific compiled manner).
 */
final class PredicateInvocationGeneratorFactory {
   /**
    * Private constructor as all methods are static.
    */
   private PredicateInvocationGeneratorFactory() {
      // do nothing
   }

   private static final DefaultPredicateInvocationGenerator DEFAULT_PREDICATE_INVOCATION_GENERATOR = new DefaultPredicateInvocationGenerator();
   private static final CompiledPredicateInvocationGenerator COMPILED_PREDICATE_INVOCATION_GENERATOR = new CompiledPredicateInvocationGenerator();
   private static final RecursivePredicateInvocationGenerator RECURSIVE_PREDICATE_INVOCATION_GENERATOR = new RecursivePredicateInvocationGenerator();

   // TODO make this mapping configurable via properties file so it can be easily extended
   private static final Map<Class<? extends PredicateFactory>, PredicateInvocationGenerator> mappings = new HashMap<>();
   static {
      mappings.put(org.projog.core.udp.SingleRuleWithSingleImmutableArgumentPredicate.class, new SingleRuleWithSingleImmutableArgumentPredicateInvocationGenerator());
      mappings.put(org.projog.core.udp.SingleRuleWithMultipleImmutableArgumentsPredicate.class, new SingleRuleWithMultipleImmutableArgumentPredicateInvocationGenerator());
      mappings.put(org.projog.core.udp.MultipleRulesWithSingleImmutableArgumentPredicate.class, new MultipleRulesWithSingleImmutableArgumentPredicateInvocationGenerator());
      mappings.put(org.projog.core.udp.MultipleRulesWithMultipleImmutableArgumentsPredicate.class, new MultipleRulesWithMultipleImmutableArgumentPredicateInvocationGenerator());

      mappings.put(org.projog.core.function.bool.True.class, new TruePredicateInvocationGenerator());
      mappings.put(org.projog.core.function.bool.Fail.class, new FailPredicateInvocationGenerator());
      mappings.put(org.projog.core.UnknownPredicate.class, new FailPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.flow.Cut.class, new CutPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.math.Is.class, new IsPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.compare.Equal.class, new EqualPredicateInvocationGenerator());

      mappings.put(org.projog.core.function.compare.NumericEquality.class, new NumericEqualPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.compare.NumericInequality.class, new NumericNotEqualPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.compare.NumericGreaterThan.class, new NumericGreaterThanPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.compare.NumericGreaterThanOrEqual.class, new NumericGreaterThanOrEqualPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.compare.NumericLessThan.class, new NumericLessThanPredicateInvocationGenerator());
      mappings.put(org.projog.core.function.compare.NumericLessThanOrEqual.class, new NumericLessThanOrEqualPredicateInvocationGenerator());
   }

   /**
    * Returns the appropriate {@link PredicateInvocationGenerator} for the specified {@link PredicateFactory}.
    * <p>
    * The returned {@link PredicateInvocationGenerator} will be able to generate Java code that is a more efficient
    * version of the functionality provided by the specified {@link PredicateFactory}.
    */
   static PredicateInvocationGenerator getPredicateInvocationGenerator(PredicateFactory ef) {
      PredicateInvocationGenerator pig;
      if (ef == null) {
         // TODO assuming if null then call to self - add isSelf method to currentClause
         pig = RECURSIVE_PREDICATE_INVOCATION_GENERATOR;
      } else if (ef instanceof CompiledPredicate) {
         pig = COMPILED_PREDICATE_INVOCATION_GENERATOR;
      } else if (mappings.containsKey(ef.getClass())) {
         pig = mappings.get(ef.getClass());
      } else {
         pig = DEFAULT_PREDICATE_INVOCATION_GENERATOR;
      }
      return pig;
   }
}