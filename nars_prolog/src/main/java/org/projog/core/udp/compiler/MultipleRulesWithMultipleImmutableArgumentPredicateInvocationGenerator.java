package org.projog.core.udp.compiler;

import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getUnifyStatement;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.Variable;
import org.projog.core.udp.MultipleRulesWithMultipleImmutableArgumentsPredicate;

final class MultipleRulesWithMultipleImmutableArgumentPredicateInvocationGenerator implements PredicateInvocationGenerator {
   // TODO consider ways to improve this class through refactoring
   // In common with some other classes in org.projog.core.udp.compiler,
   // this class is large and it's intentions not always immediately obvious.
   // CompiledPredicateSourceGeneratorTest (which checks actual content of generated source files)
   // and the system tests (which check actual behaviour) should give confidence when refactoring. 

   @Override
   public void generate(CompiledPredicateWriter g) {
      PTerm function = g.currentClause().getCurrentFunction();
      PredicateFactory ef = g.currentClause().getCurrentPredicateFactory();

      MultipleRulesWithMultipleImmutableArgumentsPredicate mrwmia = (MultipleRulesWithMultipleImmutableArgumentsPredicate) ef;
      String functionVariableName = g.classVariables().getPredicateFactoryVariableName(function, g.knowledgeBase());
      String ctrVarName = g.classVariables().getNewInlinedCtrVariableName();
      Runnable r = g.createOnBreakCallback(functionVariableName, function, ctrVarName);
      Set<Variable> termsThatAreNotYetAssignedButReusedLater = g.getTermArgumentsThatAreCurrentlyUnassignedAndNotReusedWithinTheTerm(function);
      if (termsThatAreNotYetAssignedButReusedLater.size() == function.args()) {
         g.logMultipleRulesWithImmutableArgumentsPredicateCall(functionVariableName, ctrVarName, function.getArgs());
         g.outputIfTrueThenBreak(ctrVarName + ">" + (mrwmia.data.length - 1), r);
         g.assign("final Term[] data" + ctrVarName, functionVariableName + ".data[" + ctrVarName + "++]");
         for (int i = 0; i < function.args(); i++) {
            PTerm arg = function.arg(i);
            String variableId = g.getVariableId(arg);
            g.classVariables().addAssignedVariable(variableId);
            g.assign(variableId, "data" + ctrVarName + "[" + i + "]");
         }
         g.logInlinedPredicatePredicate("Exit", functionVariableName, function);
      } else {
         Map<PTerm, String> tmpVars = g.getTermsThatRequireBacktrack(function);
         Map<String, String> variablesToKeepTempVersionOf = g.outputBacktrackTermArguments(tmpVars);
         g.logMultipleRulesWithImmutableArgumentsPredicateCall(functionVariableName, ctrVarName, function.getArgs());
         g.addLine("do {");
         g.outputIfTrueThenBreak(ctrVarName + ">" + (mrwmia.data.length - 1), r);
         g.assign("final Term[] data" + ctrVarName, functionVariableName + ".data[" + ctrVarName + "++]");
         // LinkedHashSet so predictable order (makes unit tests easier)
         Set<String> varsToBacktrack = new LinkedHashSet<>();
         for (int i = 0; i < function.args(); i++) {
            PTerm arg = function.arg(i);
            if (termsThatAreNotYetAssignedButReusedLater.contains(arg) == false) {
               String termId = tmpVars.get(arg);
               if (arg.constant() == false) {
                  varsToBacktrack.add(termId);
               }
               g.beginIf(getUnifyStatement(termId, "data" + ctrVarName + "[" + i + "]") + "==false");
               for (String id : varsToBacktrack) {
                  g.outputBacktrack(id);
               }
               g.endBlock();
               g.addLine("else");
            }
         }
         g.addLine("{");
         for (Variable v : termsThatAreNotYetAssignedButReusedLater) {
            String variableId = g.getVariableId(v);
            g.classVariables().addAssignedVariable(variableId);
            int i = 0;
            for (; i < function.args(); i++) {
               if (v == function.arg(i)) {
                  break;
               }
            }
            g.assign(variableId, "data" + ctrVarName + "[" + i + "]");
         }
         g.logInlinedPredicatePredicate("Exit", functionVariableName, function);
         g.writeStatement("break");
         g.endBlock();
         g.addLine("} while (true);");
         g.assignTermToTempVariable(variablesToKeepTempVersionOf);
      }

      g.currentClause().clearVariablesToBackTrack();
   }
}