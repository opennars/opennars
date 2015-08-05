package org.projog.core.udp.compiler;

import org.projog.core.KB;
import org.projog.core.term.*;
import org.projog.core.udp.ClauseModel;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.projog.core.KnowledgeBaseUtils.getProjogProperties;
import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.*;

/**
 * Constructs Java source code of new {@link CompiledPredicate} classes.
 */
final class CompiledPredicateWriter extends JavaSourceWriter {
   // TODO consider ways to improve this class through refactoring
   // In common with some other classes in org.projog.core.udp.compiler,
   // this class is large and it's intentions not always immediately obvious.
   // CompiledPredicateSourceGeneratorTest (which checks actual content of generated source files)
   // and the system tests (which check actual behaviour) should give confidence when refactoring. 

   static final String EMPTY_LIST_SYNTAX = "EmptyList.EMPTY_LIST";
   static final String MAIN_LOOP_LABEL = "mainloop";

   private static final AtomicInteger ctr = new AtomicInteger();

   private final String className;
   private final KB kb;
   private final PredicateMetaData factMetaData;
   private final CompiledPredicateVariables classVariables = new CompiledPredicateVariables();
   private ClauseMetaData currentClause;
   private boolean inStaticRecursiveMethodBlock;
   private boolean needsKnowledgeBaseStaticVariable;
   private boolean needsCalculatablesStaticVariable;

   CompiledPredicateWriter(KB kb, List<ClauseModel> copyImplications) {
      this.kb = kb;
      this.factMetaData = new PredicateMetaData(kb, copyImplications);
      this.className = generateClassName();
   }

   ClauseMetaData currentClause() {
      return currentClause;
   }

   void setCurrentClause(ClauseMetaData currentClause) {
      this.currentClause = currentClause;
   }

   boolean isInStaticRecursiveMethodBlock() {
      return inStaticRecursiveMethodBlock;
   }

   void setInStaticRecursiveMethodBlock(boolean inStaticRecursiveMethodBlock) {
      this.inStaticRecursiveMethodBlock = inStaticRecursiveMethodBlock;
   }

   boolean isNeedsKnowledgeBaseStaticVariable() {
      return needsKnowledgeBaseStaticVariable;
   }

   void setNeedsKnowledgeBaseStaticVariable(boolean needsKnowledgeBaseStaticVariable) {
      this.needsKnowledgeBaseStaticVariable = needsKnowledgeBaseStaticVariable;
   }

   public boolean isNeedsCalculatablesStaticVariable() {
      return needsCalculatablesStaticVariable;
   }

   void setNeedsCalculatablesStaticVariable(boolean needsCalculatablesStaticVariable) {
      this.needsCalculatablesStaticVariable = needsCalculatablesStaticVariable;
   }

   String className() {
      return className;
   }

   KB knowledgeBase() {
      return kb;
   }

   PredicateMetaData factMetaData() {
      return factMetaData;
   }

   CompiledPredicateVariables classVariables() {
      return classVariables;
   }

   private static String generateClassName() {
      int nextId = ctr.incrementAndGet();
      return "CompiledPredicate" + nextId;
   }

   String outputCreateTermStatement(PTerm t, boolean reuseImmutableTerms) {
      if (t == EmptyList.EMPTY_LIST) {
         return EMPTY_LIST_SYNTAX;
      } else if (reuseImmutableTerms && t.constant()) {
         String immutableTermVariableName = classVariables.getTermVariableName(t);
         return immutableTermVariableName;
      } else if (t.type() == PrologOperator.NAMED_VARIABLE) {
         declareVariableIfNotAlready(t, reuseImmutableTerms);
         return getVariableId(t);
      } else if (t.type() == PrologOperator.STRUCTURE) {
         StringBuilder sb = new StringBuilder("Structure.createStructure(");
         sb.append(encodeName(t));
         sb.append(", new Term[]{");
         boolean first = true;
         for (PTerm arg : t.terms()) {
            if (first) {
               first = false;
            } else {
               sb.append(", ");
            }
            sb.append(outputCreateTermStatement(arg, reuseImmutableTerms));
            if (arg.constant() == false) {
               sb.append(".getTerm()");
            }
         }
         sb.append("})");
         return sb.toString();
      } else if (t.type() == PrologOperator.LIST) {
         PTerm head = t.term(0);
         String headSyntax = outputCreateTermStatement(head, reuseImmutableTerms);
         if (head.constant() == false) {
            headSyntax += ".getTerm()";
         }
         PTerm tail = t.term(1);
         String tailSyntax = outputCreateTermStatement(tail, reuseImmutableTerms);
         if (tail.constant() == false) {
            tailSyntax += ".getTerm()";
         }
         return getNewListSyntax(headSyntax, tailSyntax);
      } else if (t.type() == PrologOperator.ATOM) {
         return "new Atom(" + encodeName(t) + ")";
      } else if (t.type() == PrologOperator.INTEGER) {
         return "new IntegerNumber(" + t.getName() + "L)";
      } else if (t.type() == PrologOperator.FRACTION) {
         return "new DecimalFraction(" + t.getName() + ")";
      } else {
         throw new RuntimeException("unknown " + t.type() + " " + t);
      }
   }

   final boolean declareVariableIfNotAlready(PTerm variable, boolean assign) {
      String variableId = getVariableId(variable);
      if (classVariables.addDeclaredVariable(variableId)) {
         if (classVariables.isMemberVariable(variableId)) {
            if (assign) {
               if (classVariables.addAssignedVariable(variableId)) {
                  beginIf(variableId + "==null");
                  assign(variableId, getNewVariableSyntax(variable));
                  endBlock();
               }
            }
         } else {
            if (assign) {
               if (classVariables.addAssignedVariable(variableId)) {
                  assign("final Term " + variableId, getNewVariableSyntax(variable));
               }
            } else {
               writeStatement("final Term " + variableId);
            }
         }
         return true;
      } else {
         if (assign && classVariables.addAssignedVariable(variableId)) {
            assign(variableId, getNewVariableSyntax(variable));
         }
         return false;
      }
   }

   final String getVariableId(PTerm variable) {
      return classVariables.getVariableId(currentClause, (PVar) variable);
   }

   final void outputIfFailThenBreak(String eval) {
      outputIfTrueThenBreak("!" + eval);
   }

   final void outputIfFailThenBreak(String eval, Runnable onBreakCallback) {
      outputIfTrueThenBreak("!" + eval, onBreakCallback);
   }

   final void outputIfTrueThenBreak(String eval) {
      outputIfTrueThenBreak(eval, null);
   }

   final void outputIfTrueThenBreak(String eval, Runnable onBreakCallback) {
      beginIf(eval);
      if (onBreakCallback != null) {
         onBreakCallback.run();
      }
      outputBacktrackAndExitClauseEvaluation();
      endBlock();
   }

   final void outputBacktrackAndExitClauseEvaluation() {
      outputBacktrack();
      exitClauseEvaluation();
   }

   @SuppressWarnings("unchecked")
   final void outputBacktrack() {
      outputBacktrack(Collections.EMPTY_SET);
   }

   final void outputBacktrack(Set<PVar> variablesToIgnore) {
      if (currentClause.isInRetryMethod() == false) {
         return;
      }

      for (PVar v : currentClause.getVariablesToBackTrack()) {
         String variableId = getVariableId(v);
         if (classVariables.isAssignedVariable(variableId) && variablesToIgnore.contains(v) == false) {
            outputBacktrack(variableId);
         } else {
            // not backtracking as not yet declared: "variableId"
         }
      }
   }

   final void exitClauseEvaluation() {
      if (currentClause.isLastCutAfterLastBacktrackPoint()) {
         if (factMetaData().isSingleResultPredicate() == false) {
            assignTrue("isCut");
         }
         exitCodeBlock();
      } else if (currentClause.isInRetryMethod() && currentClause.isFirstMutlipleResultFunctionInConjunction() == false) {
         if (currentClause.isConjunctionMulipleResult(currentClause.getConjunctionIndex())) {
            // only set conjunctionCtr if required
            assign("conjunctionCtr", currentClause.getLastBacktrackPoint());
         }
         writeStatement("break " + MAIN_LOOP_LABEL);
      } else {
         exitCodeBlock();
      }
   }

   final void exitCodeBlock() {
      if (inStaticRecursiveMethodBlock) {
         writeStatement("break");
      } else {
         returnFalse();
      }
   }

   void callUserDefinedPredicate(String compiledPredicateName, boolean isRetryable) {
      PTerm function = currentClause.getCurrentFunction();
      Set<PVar> variablesInCurrentFunction = currentClause.getVariablesInCurrentFunction();
      boolean firstInMethod = currentClause.isFirstMutlipleResultFunctionInConjunction();

      StringBuilder constructorArgs = new StringBuilder();
      for (int i = 0; i < function.length(); i++) {
         if (i != 0) {
            constructorArgs.append(", ");
         }
         PTerm a = function.term(i);
         constructorArgs.append(outputCreateTermStatement(a, true));
      }
      currentClause.addVariablesToBackTrack(variablesInCurrentFunction);
      if (isRetryable) {
         String compiledPredicateVariableName = classVariables.getNewCompiledPredicateVariableName(currentClause(), compiledPredicateName);
         StringBuilder sb = new StringBuilder();
         if (currentClause.getCurrentPredicateFactory() instanceof CompiledTailRecursivePredicate) {
            CompiledTailRecursivePredicate marfe = (CompiledTailRecursivePredicate) currentClause.getCurrentPredicateFactory();
            boolean[] isSingleResultIfArgumentImmutable = marfe.isSingleResultIfArgumentImmutable();
            for (int i = 0; i < function.length(); i++) {
               PTerm arg = function.term(i);
               if (arg.type() == PrologOperator.NAMED_VARIABLE) {
                  if (isSingleResultIfArgumentImmutable[i] && classVariables.isAssignedVariable(getVariableId(arg))) {
                     if (sb.length() != 0) {
                        sb.append(" || ");
                     }
                     sb.append(getVariableId(arg));
                     sb.append(".isImmutable()");
                  }
               }
            }
            if (sb.length() > 0) {
               String needToBacktrackVariableName = classVariables.getNewBooleanVariableName();
               beginIf(needToBacktrackVariableName);
               if (!firstInMethod) {
                  // note: no need to set variable to false if first in method
                  // as will now exit method and it will never be re-called
                  assign(needToBacktrackVariableName, "false");
               }
               outputBacktrackAndExitClauseEvaluation();
               elseIf(compiledPredicateVariableName + "==null && (" + sb + ")");
               beginIf("!" + compiledPredicateName + ".staticEvaluate(" + constructorArgs + ")");
               outputBacktrackAndExitClauseEvaluation();
               endBlock();
               assign(needToBacktrackVariableName, "true");
               addLine("} else {");
            }
         }

         beginIf(compiledPredicateVariableName + "==null");

         for (PVar v : variablesInCurrentFunction) {
            String variableId = getVariableId(v);
            if (classVariables.addDeclaredVariable(variableId)) {
               classVariables.addAssignedVariable(variableId);
               assign(variableId, getNewVariableSyntax(v));
            }
         }
         String declaration = "new " + compiledPredicateName + "(" + constructorArgs + ")";
         assign(compiledPredicateVariableName, declaration);
         elseStatement();
         Map<String, String> variablesToKeepTempVersionOf = assignTempVariablesBackToTerm();
         endBlock();
         beginIf("!" + compiledPredicateVariableName + ".evaluate((Term[])null)");
         if (firstInMethod == false) {
            assign(compiledPredicateVariableName, null);
            outputBacktrack();
            currentClause.clearVariablesToBackTrack();
         }
         exitClauseEvaluation();
         endBlock();
         assignTermToTempVariable(variablesToKeepTempVersionOf);

         if (sb.length() > 0) {
            endBlock();
         }
      } else {
         outputStaticEvaluateCall(compiledPredicateName, constructorArgs);
      }
   }

   private void outputStaticEvaluateCall(String compiledPredicateName, StringBuilder constructorArgs) {
      outputIfFailThenBreak(compiledPredicateName + ".staticEvaluate(" + constructorArgs + ")");
   }

   void outputEqualsEvaluation() {
      PTerm equalsFunction = currentClause.getCurrentFunction();
      PTerm t1 = equalsFunction.term(0);
      PTerm t2 = equalsFunction.term(1);

      outputEqualsEvaluation(t1, t2, DUMMY);
   }

   private static final Runnable DUMMY = new Runnable() {
      @Override
      public void run() {
      }
   };

   void outputEqualsEvaluation(PTerm t1, PTerm t2, Runnable onBreakCallback) {
      if (t2.type() == PrologOperator.NAMED_VARIABLE) {
         PTerm tmp = t1;
         t1 = t2;
         t2 = tmp;
      }

      // compare "t1" to "t2"
      if (isNoMoreThanTwoElementList(t1) && isNoMoreThanTwoElementList(t2)) {
         outputEqualsEvaluation(t1.term(0), t2.term(0), onBreakCallback);
         outputEqualsEvaluation(t1.term(1), t2.term(1), onBreakCallback);
      } else if (t1.type() == PrologOperator.NAMED_VARIABLE && isListOfTwoVariables(t2)) {
         Set<PVar> variables = TermUtils.getAllVariablesInTerm(t1);
         variables.addAll(TermUtils.getAllVariablesInTerm(t2));
         Set<PVar> newlyDeclaredVariables = new HashSet<>();
         for (PVar v : variables) {
            if (declareVariableIfNotAlready(v, false)) {
               newlyDeclaredVariables.add(v);
            }
         }

         String arg1 = outputCreateTermStatement(t1, true);
         beginIf(arg1 + ".getType()==TermType.LIST");
         outputAssignOfUnifyListElement(t2, arg1, 0, onBreakCallback);
         outputAssignOfUnifyListElement(t2, arg1, 1, onBreakCallback);

         elseIf(arg1 + ".getType()==TermType.NAMED_VARIABLE");
         String arg2 = outputCreateTermStatement(t2, true);
         beginIf("!" + getUnifyStatement(arg1, arg2));
         onBreakCallback.run();
         outputBacktrackAndExitClauseEvaluation();
         endBlock();
         elseStatement();
         onBreakCallback.run();
         outputBacktrackAndExitClauseEvaluation();
         endBlock();
      } else if (t1.type() == PrologOperator.NAMED_VARIABLE) {
         boolean firstUse = declareVariableIfNotAlready(t1, false);
         String variableId = getVariableId(t1);
         String arg2 = outputCreateTermStatement(t2, true);
         if (firstUse) {
            classVariables.addAssignedVariable(variableId);
            assign(variableId, arg2);
         } else {
            outputIfFailThenBreak(getUnifyStatement(variableId, arg2), onBreakCallback);
         }
      } else {
         String arg1 = outputCreateTermStatement(t1, true);
         String arg2 = outputCreateTermStatement(t2, true);
         outputIfFailThenBreak(getUnifyStatement(arg1, arg2), onBreakCallback);
      }
   }

   private void outputAssignOfUnifyListElement(PTerm list, String listId, int elementId, Runnable onBreakCallback) {
      String variableId = getVariableId(list.term(elementId));
      String element = listId + ".getArgument(" + elementId + ").getTerm()";
      if (isAssigned(variableId)) {
         beginIf("!" + getUnifyStatement(variableId, element));
         onBreakCallback.run();
         outputBacktrackAndExitClauseEvaluation();
         endBlock();
      } else {
         assign(variableId, element);
      }
   }

   private boolean isListOfTwoVariables(PTerm t) {
      return t.type() == PrologOperator.LIST && t.term(0).type().isVariable() && t.term(1).type().isVariable();
   }

   final Map<String, String> assignTempVariablesBackToTerm() {
      // use LinkedHashMap so order predictable (makes unit tests easier)
      Map<String, String> variablesToKeepTempVersionOf = new LinkedHashMap<>();
      for (String variableId : getVariablesToKeepTempVersionOf()) {
         String tmpVariableName = classVariables.getNewTempVariableName();
         assign(variableId, tmpVariableName);
         variablesToKeepTempVersionOf.put(tmpVariableName, variableId);
      }
      return variablesToKeepTempVersionOf;
   }

   final void assignTermToTempVariable(Map<String, String> variablesToKeepTempVersionOf) {
      for (Map.Entry<String, String> e : variablesToKeepTempVersionOf.entrySet()) {
         String tmpVariableName = e.getKey();
         String variableId = e.getValue();
         assign(tmpVariableName, variableId);
         assign(variableId, variableId + ".getTerm()");
      }
   }

   /**
    * Return IDs of variables that need to be kept track of.
    * <p>
    * Returns variables already defined (including in current clause) that are reused in future clauses of this same
    * rule.
    */
   @SuppressWarnings("unchecked")
   private final Set<String> getVariablesToKeepTempVersionOf() {
      if (currentClause.getConjunctionIndex() == currentClause.getConjunctionCount()) {
         return Collections.EMPTY_SET;
      }

      // LinkedHashSet to make order predictable (makes unit tests easier)
      Set<String> alreadyDeclaredVariables = new LinkedHashSet<>();
      Set<PVar> variables1 = TermUtils.getAllVariablesInTerm(currentClause.getConsequent());
      for (PVar v : variables1) {
         String variableId = getVariableId(v);
         alreadyDeclaredVariables.add(variableId);
      }

      for (int i = 0; i <= currentClause.getConjunctionIndex(); i++) {
         Set<PVar> variables = currentClause.getVariablesInConjunction(i);
         for (PVar v : variables) {
            String variableId = getVariableId(v);
            alreadyDeclaredVariables.add(variableId);
         }
      }

      int start = currentClause.getConjunctionIndex();
      if (start < 0) {
         start = 0;
      }
      Set<String> usedLaterVariables = new HashSet<>();
      for (int i = start; i < currentClause.getConjunctionCount(); i++) {
         Set<PVar> variables = currentClause.getVariablesInConjunction(i);
         for (PVar v : variables) {
            String variableId = getVariableId(v);
            usedLaterVariables.add(variableId);
         }
      }

      // LinkedHashSet to make order predictable (makes unit tests easier)
      Set<String> result = new LinkedHashSet<>();
      for (String variableId : alreadyDeclaredVariables) {
         if (usedLaterVariables.contains(variableId) && classVariables.isMemberVariable(variableId)) {
            result.add(variableId);
         }
      }
      return result;
   }

   final Map<PTerm, String> getTermsThatRequireBacktrack(PTerm function) {
      Set<PVar> x = getTermArgumentsThatAreCurrentlyUnassignedAndNotReusedWithinTheTerm(function);
      // use LinkedHashMap so order is predictable - purely so unit tests are easier 
      Map<PTerm, String> tempVars = new LinkedHashMap<>();

      for (int i = 0; i < function.length(); i++) {
         PTerm arg = function.term(i);
         if (x.contains(arg)) {
            // ignore
         } else {
            String id = outputCreateTermStatement(arg, true);
            if (arg.constant() == false) {
               id = classVariables.getNewTempVariableName();
            }
            tempVars.put(arg, id);
         }
      }
      return tempVars;
   }

   /**
    * Identifies cases where a variable is reused multiple times in a clause before it is assigned.
    * <p>
    * e.g. <code>p(X,X,X)</code>
    */
   final Set<PVar> getTermArgumentsThatAreCurrentlyUnassignedAndNotReusedWithinTheTerm(PTerm function) {
      // LinkedHashSet so predictable order (makes unit tests easier)
      final Set<PVar> result = new LinkedHashSet<>();
      final Set<PVar> duplicates = new HashSet<>();
      for (int i = 0; i < function.length(); i++) {
         PTerm t = function.term(i);
         if (t.type() == PrologOperator.NAMED_VARIABLE) {
            PVar v = (PVar) t;
            if (classVariables.isAssignedVariable(getVariableId(v)) == false && duplicates.contains(v) == false) {
               boolean newEntry = result.add(v);
               if (newEntry == false) {
                  duplicates.add(v);
                  result.remove(v);
               }
            }
         }
      }
      return result;
   }

   Map<String, String> outputBacktrackTermArguments(Map<PTerm, String> termsThatRequireBacktrack) {
      beginIf(classVariables.getCurrentInlinedCtrVariableName() + "!=0");
      Map<String, String> variablesToKeepTempVersionOf = assignTempVariablesBackToTerm();

      for (Map.Entry<PTerm, String> e : termsThatRequireBacktrack.entrySet()) {
         if (e.getKey().constant() == false) {
            outputBacktrack(e.getValue());
         }
      }

      addLine("} else {");

      for (Map.Entry<PTerm, String> e : termsThatRequireBacktrack.entrySet()) {
         if (e.getKey().constant() == false) {
            String createTermStatement = outputCreateTermStatement(e.getKey(), true);
            assign(e.getValue(), createTermStatement + ".getTerm()");
         }
      }

      endBlock();

      return variablesToKeepTempVersionOf;
   }

   final Runnable createOnBreakCallback(final String functionVariableName, final PTerm function, final String ctrVarName) {
      return new Runnable() {
         @Override
         public void run() {
            if (ctrVarName != null) {
               assign(ctrVarName, 0);
            }
            logInlinedPredicatePredicate("Fail", functionVariableName, function);
         }
      };
   }

   final void logMultipleRulesWithImmutableArgumentsPredicateCall(String functionVariableName, String ctrVarName, PTerm... arguments) {
      if (isSpyPointsEnabled() == false) {
         return;
      }

      beginIf(ctrVarName + "==0");
      log("Call", functionVariableName, arguments);
      addLine("} else {");
      log("Redo", functionVariableName, arguments);
      endBlock();
   }

   final void logInlinedPredicatePredicate(String type, String functionVariableName, PTerm function) {
      log(type, functionVariableName, function.terms());
   }

   private void log(String type, String functionVariableName, PTerm... arguments) {
      if (isSpyPointsEnabled() == false) {
         return;
      }

      StringBuilder sb = new StringBuilder();
      if (arguments.length == 0) {
         sb.append(", TermUtils.EMPTY_ARRAY");
      } else {
         sb.append(", new Term[]{");
         for (int i = 0; i < arguments.length; i++) {
            if (i != 0) {
               sb.append(", ");
            }
            PTerm arg = arguments[i];
            sb.append(getLogArgument(arg));
         }
         sb.append("}");
      }
      String spyPointVariableName = functionVariableName + ".spyPoint";
      beginIf(spyPointVariableName + ".isEnabled()");
      writeStatement(functionVariableName + ".spyPoint.log" + type + "(" + functionVariableName + sb + ")");
      endBlock();
   }

   private String getLogArgument(PTerm arg) {
      if (arg.type() == PrologOperator.NAMED_VARIABLE) {
         String variableId = getVariableId(arg);
         if (classVariables.isMemberVariable(variableId) == false && classVariables.isAssignedVariable(variableId) == false) {
            return "new Variable(\"_\")";
         } else if (classVariables.isAssignedVariable(variableId) == false) {
            return variableId;
         } else {
            return variableId;
         }
      } else {
         return outputCreateTermStatement(arg, true);
      }
   }

   void outputBacktrack(String variableId) {
      writeStatement(variableId + ".backtrack()");
   }

   boolean isSpyPointsEnabled() {
      return getProjogProperties(kb).isSpyPointsEnabled();
   }

   boolean isAssigned(String id) {
      return classVariables.isAssignedVariable(id);
   }
}