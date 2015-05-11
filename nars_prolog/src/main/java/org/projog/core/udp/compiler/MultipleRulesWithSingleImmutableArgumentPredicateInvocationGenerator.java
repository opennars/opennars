package org.projog.core.udp.compiler;

import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getUnifyStatement;

import java.util.Map;

import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;
import org.projog.core.udp.MultipleRulesWithSingleImmutableArgumentPredicate;

final class MultipleRulesWithSingleImmutableArgumentPredicateInvocationGenerator implements PredicateInvocationGenerator {
   // TODO consider ways to improve this class through refactoring
   // In common with some other classes in org.projog.core.udp.compiler,
   // this class is large and it's intentions not always immediately obvious.
   // CompiledPredicateSourceGeneratorTest (which checks actual content of generated source files)
   // and the system tests (which check actual behaviour) should give confidence when refactoring. 

   @Override
   public void generate(CompiledPredicateWriter g) {
      PTerm function = g.currentClause().getCurrentFunction();
      PredicateFactory ef = g.currentClause().getCurrentPredicateFactory();

      MultipleRulesWithSingleImmutableArgumentPredicate mrwsia = (MultipleRulesWithSingleImmutableArgumentPredicate) ef;
      String functionVariableName = g.classVariables().getPredicateFactoryVariableName(function, g.knowledgeBase());
      PTerm arg = function.term(0);
      String ctrVarName = g.classVariables().getNewInlinedCtrVariableName();
      boolean firstUse = arg.type() == PrologOperator.NAMED_VARIABLE && g.classVariables().isAssignedVariable(g.getVariableId(arg)) == false;
      Runnable r = g.createOnBreakCallback(functionVariableName, function, ctrVarName);
      if (firstUse) {
         g.logMultipleRulesWithImmutableArgumentsPredicateCall(functionVariableName, ctrVarName, arg);
         String variableId = g.getVariableId(arg);
         g.outputIfTrueThenBreak(ctrVarName + ">" + (mrwsia.data.length - 1), r);
         g.classVariables().addAssignedVariable(variableId);
         g.assign(variableId, functionVariableName + ".data[" + ctrVarName + "++]");
         g.logInlinedPredicatePredicate("Exit", functionVariableName, function);
      } else {
         Map<PTerm, String> tmpVars = g.getTermsThatRequireBacktrack(function);
         Map<String, String> variablesToKeepTempVersionOf = g.outputBacktrackTermArguments(tmpVars);
         String termId = tmpVars.get(arg);
         g.logMultipleRulesWithImmutableArgumentsPredicateCall(functionVariableName, ctrVarName, arg);
         g.addLine("do {");
         g.outputIfTrueThenBreak(ctrVarName + ">" + (mrwsia.data.length - 1), r);
         g.beginIf(getUnifyStatement(termId, functionVariableName + ".data[" + ctrVarName + "++]"));
         g.logInlinedPredicatePredicate("Exit", functionVariableName, function);
         g.writeStatement("break");
         if (arg.constant() == false) {
            g.elseStatement();
            g.outputBacktrack(termId);
         }
         g.endBlock();
         g.addLine("} while (true);");
         g.assignTermToTempVariable(variablesToKeepTempVersionOf);
      }

      g.currentClause().clearVariablesToBackTrack();
   }
}