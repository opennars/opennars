package org.projog.api;

import org.projog.core.CutException;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.ProjogException;
import org.projog.core.term.PTerm;
import org.projog.core.term.Variable;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Represents an executing query.
 */
public final class QueryResult  {
   private final PredicateFactory predicateFactory;
   public final PTerm query;
   public final Map<String, Variable> variables;
   public final PTerm[] args;
   private Predicate predicate;


   /*
   implements Iterable<Map.Entry<String,Variable>>
   @Override
   public Iterator<Map.Entry<String, Variable>> iterator() {
      return variables.entrySet().iterator();
   }*/

   /**
    * Evaluates a query.
    * 
    * @param PredicateFactory the {@link PredicateFactory} that will be used to evaluate the query
    * @param query represents the query statement being evaluated
    * @param variables collection of variables contained in the query (keyed by variable id)
    * @see QueryStatement#get()
    */
   QueryResult(PredicateFactory predicateFactory, PTerm query, Map<String, Variable> variables) {
      this.predicateFactory = predicateFactory;
      this.query = query;
      this.variables = variables;
      this.args = new PTerm[query.args()];
   }

   /**
    * Attempts to evaluate the query this object represents.
    * <p>
    * Subsequent calls of the {@code next()} method attempt to reevaluate the query, and because it returns
    * {@code false} when the are no more results, it can be used in a {@code while} loop to iterate through all the
    * results.
    * 
    * @return {@code true} if the query was (re)evaluated successfully or {@code false} if there are no more results.
    * Once {@code false} has been returned by {@code next()} the {@code next()} method should no longer be called on
    * that object.
    * @throws PrologException if an error occurs while evaluating the query
    */
   public boolean next() {
      if (predicate == null) {
         return doFirstEvaluationOfQuery();
      } else if (predicate.isRetryable()) {
         return doRetryEvaluationOfQuery();
      } else {
         return false;
      }
   }

   public void all(double timeLimitSec) {
      all(timeLimitSec, null);
   }

   public void all(double timeLimitSec, Consumer<QueryResult> receiver) {
      long start = System.currentTimeMillis(); //allow nanosec resolution via option

      while (!isExhausted()) {

         next();

         long now = System.currentTimeMillis();
         if (((now = start) * 1.0e-3) > timeLimitSec) break;
      }

      if (receiver!=null)
         receiver.accept(this);
   }

   private boolean doFirstEvaluationOfQuery() {
      for (int i = 0; i < args.length; i++) {
         args[i] = query.arg(i).get();
      }
      predicate = predicateFactory.getPredicate(args);
      return predicate.evaluate(args);
   }

   private boolean doRetryEvaluationOfQuery() {
      try {
         return predicate.evaluate(args);
      } catch (CutException e) {
         // e.g. for a query like: ?- true, !.
         return false;
      }
   }

   /**
    * Returns {@code true} if it is known that all possible solutions have been found, else {@code false}.
    * 
    * @return {@code true} if it is known that all possible solutions have been found, else {@code false}.
    * @see org.projog.core.Predicate#couldReEvaluationSucceed()
    */
   public boolean isExhausted() {
      if (predicate == null) {
         return false;
      }
      return predicate.couldReEvaluationSucceed() == false;
   }

   /**
    * Attempts to unify the specified term to the variable with the specified id.
    * <p>
    * If the variable is already unified to a term then an attempt will be made to unify the specified term with the
    * term the variable is currently unified with.
    * 
    * @param variableId the id of the variable
    * @param term the term to unify
    * @return {@code true} if the attempt to unify the specified term to the variable with the specified id was
    * successful
    * @throws ProjogException if {@link #next()} has already been called on this object or if no variable with the
    * specified id exists in the query this object represents
    */
   public boolean setTerm(String variableId, PTerm term) {
      if (predicate != null) {
         throw new ProjogException("Calling setTerm(" + variableId + ", " + term + ") after next() has already been called for: " + query);
      }
      PTerm v = getTerm(variableId);
      return v.unify(term);
   }

   /**
    * Returns the term instantiated to the variable with the specified id.
    * 
    * @param variableId the id of the variable from which to return the instantiated term
    * @return the term instantiated to the variable with the specified id (or the {@link org.projog.core.term.Variable}
    * of representing the variable if it is uninstantiated)
    * @throws ProjogException if no variable with the specified id exists in the query this object represents
    */
   public PTerm getTerm(String variableId) {
      Variable v = variables.get(variableId);
      if (v == null) {
         throw new ProjogException("Do not know about variable named: " + variableId + " in query: " + query);
      }
      return v.get();
   }

   /**
    * Returns id's of all variables defined in the query this object represents.
    * 
    * @return id's of all variables defined in the query this object represents
    */
   public Set<String> getVariableIds() {
      return new TreeSet<>(variables.keySet());
   }
}