package org.projog.core.udp.compiler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.projog.core.PredicateFactory;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;

final class DefaultPredicateInvocationGenerator implements PredicateInvocationGenerator {
   // TODO consider ways to improve this class through refactoring
   // In common with some other classes in org.projog.core.udp.compiler,
   // this class is large and it's intentions not always immediately obvious.
   // CompiledPredicateSourceGeneratorTest (which checks actual content of generated source files)
   // and the system tests (which check actual behaviour) should give confidence when refactoring. 

   @Override
   public void generate(CompiledPredicateWriter g) {
      final PTerm function = g.currentClause().getCurrentFunction();
      final PredicateFactory ef = g.currentClause().getCurrentPredicateFactory();
      final boolean isRetryable = g.currentClause().isCurrentFunctionMulipleResult();
      final boolean inRetryMethod = g.currentClause().isInRetryMethod();
      final boolean firstInMethod = g.currentClause().isFirstMutlipleResultFunctionInConjunction();
      final int numberOfArguments = function.length();

      if (isRetryable) {
         if (inRetryMethod == false) {
            throw new RuntimeException("Should never have a retryable Predicate factory without out being in a retry method");
         }

         Set<PVar> variablesInCurrentFunction = g.currentClause().getVariablesInCurrentFunction();

         // only has to be unique per clause as can be reused
         String PredicateVariableName = g.classVariables().getNewMemberPredicateName(g.currentClause(), getPredicateReturnType(ef, numberOfArguments));

         String functionVariableName = g.classVariables().getPredicateFactoryVariableName(function, g.knowledgeBase());
         g.beginIf(PredicateVariableName + "==null");
         StringBuilder methodArgs = new StringBuilder();
         for (int i = 0; i < numberOfArguments; i++) {
            if (i != 0) {
               methodArgs.append(", ");
            }
            PTerm arg = function.term(i);
            String argValue = g.outputCreateTermStatement(arg, true);
            if (arg.constant()) {
               methodArgs.append(argValue);
            } else {
               String argVariable = g.classVariables().getNewTermVariable(g.currentClause());
               g.assign(argVariable, argValue + ".getTerm()");
               methodArgs.append(argVariable);
            }
         }
         g.assign(PredicateVariableName, functionVariableName + ".getPredicate(" + methodArgs + ")");
         g.elseStatement();
         g.outputIfTrueThenBreak(PredicateVariableName + ".isRetryable()==false");
         Map<String, String> variablesToKeepTempVersionOf = g.assignTempVariablesBackToTerm();
         g.endBlock();

         g.beginIf("!" + PredicateVariableName + ".evaluate(" + methodArgs + ")");
         if (firstInMethod == false) {
            g.currentClause().addVariablesToBackTrack(variablesInCurrentFunction);
            g.outputBacktrack();
         }
         g.currentClause().clearVariablesToBackTrack();
         g.assign(PredicateVariableName, null);
         g.exitClauseEvaluation();
         g.endBlock();

         g.assignTermToTempVariable(variablesToKeepTempVersionOf);
      } else {
         Set<PVar> variables = g.currentClause().getVariablesInCurrentFunction();
         g.currentClause().addVariablesToBackTrack(variables);
         StringBuilder methodArgs = new StringBuilder();
         for (int i = 0; i < numberOfArguments; i++) {
            if (i != 0) {
               methodArgs.append(", ");
            }
            methodArgs.append(g.outputCreateTermStatement(function.term(i), true) + ".getTerm()");
         }
         String functionVariableName = g.classVariables().getPredicateFactoryVariableName(function, g.knowledgeBase());
         final String eval;
         if (ef instanceof AbstractSingletonPredicate) {
            // note: no need to getPredicate as know it will "return this;"
            eval = "!" + functionVariableName + ".evaluate(" + methodArgs + ")";
         } else {
            eval = "!" + functionVariableName + ".getPredicate(" + methodArgs + ").evaluate(" + methodArgs + ")";
         }
         g.outputIfTrueThenBreak(eval);
      }
   }

   private String getPredicateReturnType(PredicateFactory ef, int numberOfArguments) {
      Class<? extends PredicateFactory> predicateFactoryClass = ef.getClass();
      Method m;
      try {
         // if an overloaded version of the getPredicate method exists, with the exact number of required arguments, then use that
         m = predicateFactoryClass.getDeclaredMethod("getPredicate", getMethodParameters(numberOfArguments));
      } catch (NoSuchMethodException e) {
         try {
            // default to using the overridden varargs version of the getPredicate method (as defined by PredicateFactory) 
            m = predicateFactoryClass.getDeclaredMethod("getPredicate", PTerm[].class);
         } catch (NoSuchMethodException e2) {
            throw new RuntimeException("No getPredicate(Term[]) method declared for: " + predicateFactoryClass, e2);
         }
      }
      return m.getReturnType().getName();
   }

   @SuppressWarnings("rawtypes")
   private Class<?>[] getMethodParameters(int numberOfArguments) {
      Class<?>[] args = new Class[numberOfArguments];
      for (int i = 0; i < numberOfArguments; i++) {
         args[i] = PTerm.class;
      }
      return args;
   }
}