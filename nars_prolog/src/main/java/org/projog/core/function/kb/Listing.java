package org.projog.core.function.kb;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.KnowledgeBaseUtils.getPredicateKeysByName;
import static org.projog.core.KnowledgeBaseUtils.getTermFormatter;
import static org.projog.core.term.TermUtils.getAtomName;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.projog.core.FileHandles;
import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.UserDefinedPredicateFactory;

/* TEST
 test(X) :- X < 3.
 test(X) :- X > 9.
 test(X) :- X = 5.
 %QUERY listing(test)
 %OUTPUT
 % test(X) :- X < 3
 % test(X) :- X > 9
 % test(X) :- X = 5
 %
 %OUTPUT
 %ANSWER/
 
 overloaded_predicate_name(X) :- X = this_rule_has_one_argument.
 overloaded_predicate_name(X, Y) :- X = this_rule_has_two_arguments, X = Y.
 %QUERY listing(overloaded_predicate_name)
 %OUTPUT
 % overloaded_predicate_name(X) :- X = this_rule_has_one_argument
 % overloaded_predicate_name(X, Y) :- X = this_rule_has_two_arguments , X = Y
 %
 %OUTPUT
 %ANSWER/

 %TRUE listing(predicate_name_that_doesnt_exist_in_knowledge_base)

 %QUERY listing(X)
 %ERROR Expected an atom but got: NAMED_VARIABLE with value: X
 */
/**
 * <code>listing(X)</code> - outputs current clauses.
 * <p>
 * <code>listing(X)</code> allows you to inspect the clauses you currently have loaded. Causes all clauses with
 * <code>X</code> as the predicate name to be written to the current output stream.
 * </p>
 */
public final class Listing extends AbstractSingletonPredicate {
   private TermFormatter termFormatter;
   private FileHandles fileHandles;

   @Override
   protected void init() {
      termFormatter = getTermFormatter(getKnowledgeBase());
      fileHandles = getFileHandles(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm arg) {
      String predicateName = getAtomName(arg);
      List<PredicateKey> keys = getPredicateKeysByName(getKnowledgeBase(), predicateName);
      for (PredicateKey key : keys) {
         listClauses(key);
      }
      return true;
   }

   private void listClauses(PredicateKey key) {
      Iterator<ClauseModel> implications = getClauses(key);
      while (implications.hasNext()) {
         listClause(implications.next());
      }
   }

   private Iterator<ClauseModel> getClauses(PredicateKey key) {
      Map<PredicateKey, UserDefinedPredicateFactory> userDefinedPredicates = getKnowledgeBase().getUserDefinedPredicates();
      UserDefinedPredicateFactory userDefinedPredicate = userDefinedPredicates.get(key);
      return userDefinedPredicate.getImplications();
   }

   private void listClause(ClauseModel clauseModel) {
      String s = termFormatter.toString(clauseModel.getOriginal());
      fileHandles.getCurrentOutputStream().println(s);
   }
}