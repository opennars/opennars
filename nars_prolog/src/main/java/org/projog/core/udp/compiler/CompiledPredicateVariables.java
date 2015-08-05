package org.projog.core.udp.compiler;

import org.projog.core.KB;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;
import org.projog.core.term.PrologOperator;
import org.projog.core.udp.StaticUserDefinedPredicateFactory;

import java.util.*;

import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getClassNameMinusPackage;

/**
 * Maintains details of member variables of a {@link CompiledPredicate}.
 */
final class CompiledPredicateVariables {
   static final String ARGUMENT_PREFIX = "a";
   static final String PLACEHOLDER_PREFIX = "p";
   static final String INLINED_CTR_PREFIX = "inlinedCtr";
   static final String TEMP_NUMERIC_PREFIX = "y";

   private static final String TEMP_PREFIX = "_";
   private static final String VARIABLE_PREFIX = "v";
   private static final String TERM_PREFIX = "t";
   private static final String COMPILED_PREDICATE_PREFIX = "c";
   private static final String BOOLEAN_PREFIX = "b";

   // use LinkedHashMap and LinkedHashSet implementations so order is predictable - purely so unit tests are easier 
   private final Map<String, String> memberPredicates = new LinkedHashMap<>();
   private final Map<String, PredicateFactoryStaticVariable> requiredPredicateFactories = new LinkedHashMap<>();
   // key = variable name, value = class name
   private final Map<String, String> memberCompiledPredicates = new LinkedHashMap<>();
   private final Map<String, PTerm> staticMemberVariables = new LinkedHashMap<>();
   private final Set<String> memberVariables = new LinkedHashSet<>();
   private final Set<String> memberTerms = new LinkedHashSet<>();
   private final Set<String> declaredVariables = new HashSet<>();
   private final Set<String> assignedVariables = new HashSet<>();
   private final Map<PVar, String> anonymousVariableIds = new HashMap<PVar, String>();

   private int tempTermCtr;
   private int tempNumericCtr;
   private int inlinedDataFunctionCtr;
   private int booleanCtr;

   void clearDeclaredVariables() {
      declaredVariables.clear();
   }

   void clearAssignedVariables() {
      assignedVariables.clear();
   }

   Set<String> getAssignedVariables() {
      return new HashSet<>(assignedVariables);
   }

   void setAssignedVariables(Set<String> assignedVariables) {
      clearAssignedVariables();
      this.assignedVariables.addAll(assignedVariables);
   }

   boolean addDeclaredVariable(String variableId) {
      return declaredVariables.add(variableId);
   }

   boolean isDeclaredVariable(String variableId) {
      return declaredVariables.contains(variableId);
   }

   boolean addAssignedVariable(String variableId) {
      addDeclaredVariable(variableId);
      return assignedVariables.add(variableId);
   }

   boolean isAssignedVariable(String variableId) {
      return assignedVariables.contains(variableId);
   }

   void addMemberVariables(ClauseMetaData clauseMetaData) {
      if (clauseMetaData.isSingleResult()) {
         return;
      }
      for (int i = clauseMetaData.getIndexOfFirstMulipleResultConjuction(); i < clauseMetaData.getConjunctionCount(); i++) {
         for (PVar v : clauseMetaData.getVariablesInConjunction(i)) {
            memberVariables.add(getVariableId(clauseMetaData, v));
         }
      }
   }

   boolean isMemberVariable(String variableId) {
      return memberVariables.contains(variableId);
   }

   String getTermVariableName(PTerm t) {
      String originalVariableName = t.type().toString();
      if (t.type().isNumeric()) {
         originalVariableName += "_" + t.getName().replace('.', '_').replace('-', 'N');
      }
      String variableName = originalVariableName;
      int ctr = 0;
      while (staticMemberVariables.containsKey(variableName) && staticMemberVariables.get(variableName).strictEquals(t) == false) {
         variableName = originalVariableName + "_" + (ctr++);
      }
      staticMemberVariables.put(variableName, t);
      return variableName;
   }

   Set<Map.Entry<String, PTerm>> getStaticMemberVariables() {
      return staticMemberVariables.entrySet();
   }

   String getPredicateFactoryVariableName(PTerm t, KB kb) {
      PredicateKey key = PredicateKey.createForTerm(t);
      PredicateFactory ef = kb.getPredicateFactory(key);
      String originalVariableName = getClassNameMinusPackage(ef);
      if (t.type() == PrologOperator.STRUCTURE) {
         originalVariableName += "_" + t.length();
      }
      String variableName = originalVariableName;
      int ctr = 0;
      while (requiredPredicateFactories.containsKey(variableName) && requiredPredicateFactories.get(variableName).key.equals(key) == false) {
         variableName = originalVariableName + "_" + (ctr++);
      }
      boolean isStaticUserDefinedPredicate = ef instanceof StaticUserDefinedPredicateFactory;
      if (isStaticUserDefinedPredicate) {
         ef = ((StaticUserDefinedPredicateFactory) ef).getActualPredicateFactory();
      }
      PredicateFactoryStaticVariable efsv = new PredicateFactoryStaticVariable(key, variableName, ef, isStaticUserDefinedPredicate);
      requiredPredicateFactories.put(variableName, efsv);
      return variableName;
   }

   Collection<PredicateFactoryStaticVariable> getRequiredPredicateFactories() {
      return requiredPredicateFactories.values();
   }

   String getNewInlinedCtrVariableName() {
      return INLINED_CTR_PREFIX + (inlinedDataFunctionCtr++);
   }

   String getCurrentInlinedCtrVariableName() {
      if (inlinedDataFunctionCtr == 0) {
         throw new IllegalStateException("calling getCurrentInlinedCtrVariableName before getNewInlinedCtrVariableName");
      }
      return INLINED_CTR_PREFIX + (inlinedDataFunctionCtr - 1);
   }

   String getNewBooleanVariableName() {
      return BOOLEAN_PREFIX + (booleanCtr++);
   }

   String getNewTempVariableName() {
      return TEMP_PREFIX + (tempTermCtr++);
   }

   int getTempNumericCtr() {
      return tempNumericCtr;
   }

   String getNewTempNumericName() {
      return TEMP_NUMERIC_PREFIX + (tempNumericCtr++);
   }

   String getNewCompiledPredicateVariableName(ClauseMetaData clauseMetaData, String compiledPredicateName) {
      String variableName = COMPILED_PREDICATE_PREFIX + clauseMetaData.getClauseIndex() + "_" + (clauseMetaData.getNextMemberCompiledPredicatesIndex());
      memberCompiledPredicates.put(variableName, compiledPredicateName);
      return variableName;
   }

   String getNewTermVariable(ClauseMetaData clauseMetaData) {
      String variableName = TERM_PREFIX + clauseMetaData.getNextTermVariableIndex();
      memberTerms.add(variableName);
      return variableName;
   }

   String getNewMemberPredicateName(ClauseMetaData clauseMetaData, String returnType) {
      String predicateVariableName = "e" + (clauseMetaData.getNextRetryablePredicateIndex());
      memberPredicates.put(predicateVariableName, returnType);
      return predicateVariableName;
   }

   String getVariableId(ClauseMetaData clauseMetaData, PVar variable) {
      String id;
      if (isAnonymousVariable(variable)) {
         id = getAnonymousVariableId(variable);
      } else {
         id = variable.getId();
      }
      return VARIABLE_PREFIX + clauseMetaData.getClauseIndex() + "_" + id;
   }

   private boolean isAnonymousVariable(PVar v) {
      return v.getId().startsWith("_");
   }

   /**
    * Returns a unique variable name for the specified anonymous variable instance.
    * <p>
    * Anonymous variables may have the same name (normally {@code _}) - but they need to be treated as separate
    * instances (so we prefix the result to return with a number to make it unique).
    */
   private String getAnonymousVariableId(PVar variable) {
      if (anonymousVariableIds.containsKey(variable)) {
         return anonymousVariableIds.get(variable);
      } else {
         String id = anonymousVariableIds.size() + variable.getId();
         anonymousVariableIds.put(variable, id);
         return id;
      }
   }

   List<MemberVariable> getVariablesToDeclare() {
      List<MemberVariable> result = new ArrayList<>();
      for (Map.Entry<String, String> e : memberCompiledPredicates.entrySet()) {
         result.add(new MemberVariable(e.getValue(), e.getKey()));
      }
      for (Map.Entry<String, String> e : memberPredicates.entrySet()) {
         result.add(new MemberVariable(e.getValue(), e.getKey()));
      }
      for (String v : memberVariables) {
         result.add(new MemberVariable("Term", v));
      }
      for (String v : memberTerms) {
         result.add(new MemberVariable("Term", v));
      }
      for (int i = 0; i < tempTermCtr; i++) {
         result.add(new MemberVariable("Term", TEMP_PREFIX + i));
      }
      for (int i = 0; i < booleanCtr; i++) {
         result.add(new MemberVariable("boolean", BOOLEAN_PREFIX + i));
      }
      for (int i = 0; i < inlinedDataFunctionCtr; i++) {
         result.add(new MemberVariable("int", INLINED_CTR_PREFIX + i));
      }
      return result;
   }

   static class MemberVariable {
      final String type;
      final String name;

      private MemberVariable(String type, String name) {
         this.type = type;
         this.name = name;
      }
   }

   static class PredicateFactoryStaticVariable {
      final PredicateKey key;
      final String variableName;
      final PredicateFactory PredicateFactory;
      final boolean isCompiledPredicate;

      PredicateFactoryStaticVariable(PredicateKey key, String variableName, PredicateFactory ef, boolean isCompiledPredicate) {
         this.key = key;
         this.variableName = variableName;
         this.PredicateFactory = ef;
         this.isCompiledPredicate = isCompiledPredicate;
      }
   }
}