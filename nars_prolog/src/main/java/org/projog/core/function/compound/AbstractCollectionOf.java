package org.projog.core.function.compound;

import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Predicate;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.ListFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;
import org.projog.core.term.TermUtils;

import java.util.*;
import java.util.Map.Entry;

abstract class AbstractCollectionOf extends AbstractRetryablePredicate {
   private List<PVar> variablesNotInTemplate;
   private Iterator<Entry<Key, List<PTerm>>> itr;

   @Override
   public final boolean evaluate(PTerm template, PTerm goal, PTerm bag) {
      if (itr == null) {
         init(template, goal);
      }

      if (itr.hasNext()) {
         template.backtrack();
         Entry<Key, List<PTerm>> e = itr.next();
         bag.backtrack();
         bag.unify(ListFactory.createList(e.getValue()));
         for (int i = 0; i < variablesNotInTemplate.size(); i++) {
            PVar v = variablesNotInTemplate.get(i);
            v.backtrack();
            v.unify(e.getKey().terms.get(i));
         }
         return true;
      } else {
         return false;
      }
   }

   private void init(PTerm template, PTerm goal) {
      variablesNotInTemplate = getVariablesNotInTemplate(template, goal);

      Predicate predicate = KnowledgeBaseUtils.getPredicate(getKB(), goal);
      PTerm[] goalArguments = goal.terms();

      Map<Key, List<PTerm>> m = new LinkedHashMap<>();
      if (predicate.evaluate(goalArguments)) {
         do {
            Key key = new Key(variablesNotInTemplate);
            List<PTerm> l = m.get(key);
            if (l == null) {
               l = new ArrayList<PTerm>();
               m.put(key, l);
            }
            add(l, template.get());
         } while (hasFoundAnotherSolution(predicate, goalArguments));
      }

      goal.backtrack();

      itr = m.entrySet().iterator();
   }

   protected abstract void add(List<PTerm> l, PTerm t);

   private List<PVar> getVariablesNotInTemplate(PTerm template, PTerm goal) {
      Set<PVar> variablesInGoal = TermUtils.getAllVariablesInTerm(goal);
      Set<PVar> variablesInTemplate = TermUtils.getAllVariablesInTerm(template);
      variablesInGoal.removeAll(variablesInTemplate);
      return new ArrayList<PVar>(variablesInGoal);
   }

   private boolean hasFoundAnotherSolution(final Predicate predicate, final PTerm[] goalArguments) {
      return predicate.isRetryable() && predicate.couldReEvaluationSucceed() && predicate.evaluate(goalArguments);
   }

   @Override
   public final boolean couldReEvaluationSucceed() {
      return itr == null || itr.hasNext();
   }

   /** Represents a combination of possible values for the variables contained in the goal. */
   private static class Key {
      final List<PTerm> terms;

      Key(List<PVar> variables) {
         terms = new ArrayList<>(variables.size());
         for (PVar v : variables) {
            terms.add(v.get());
         }
      }

      @Override
      public boolean equals(Object o) {
         Key k = (Key) o;
         for (int i = 0; i < terms.size(); i++) {
            if (!terms.get(i).strictEquals(k.terms.get(i))) {
               return false;
            }
         }
         return true;
      }

      @Override
      public int hashCode() {
         // TODO is it possible to improve on returning the same hashCode for all instances?
         return 0;
      }
   }
}