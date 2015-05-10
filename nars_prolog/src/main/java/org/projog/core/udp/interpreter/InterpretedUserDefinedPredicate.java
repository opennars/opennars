package org.projog.core.udp.interpreter;

import java.util.Iterator;

import org.projog.core.CutException;
import org.projog.core.Predicate;
import org.projog.core.PredicateKey;
import org.projog.core.ProjogException;
import org.projog.core.SpyPoints;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/**
 * Represents a user defined predicate.
 * 
 * @see #evaluate(PTerm...)
 */
public final class InterpretedUserDefinedPredicate implements Predicate {
   private final PredicateKey key;
   private final Iterator<ClauseAction> clauseActions;
   private final SpyPoints.SpyPoint spyPoint;
   private final boolean debugEnabled;

   private int currentClauseIdx;
   private ClauseAction currentClauseAction;
   private boolean retryCurrentClauseAction;

   public InterpretedUserDefinedPredicate(PredicateKey key, SpyPoints.SpyPoint spyPoint, Iterator<ClauseAction> clauseActions) {
      this.key = key;
      this.clauseActions = clauseActions;
      this.spyPoint = spyPoint;
      this.debugEnabled = spyPoint != null && spyPoint.isEnabled();
   }

   /**
    * Evaluates a user defined predicate.
    * <p>
    * The process for evaluating a user defined predicate is as follows:
    * <ul>
    * <li>Iterates through every clause of the user defined predicate.</li>
    * <li>For each clause it attempts to unify the arguments in it's head (consequent) with the arguments in the query (
    * {@code queryArgs}).</li>
    * <li>If the head of the clause can be unified with the query then an attempt is made to evaluate the body
    * (antecedant) of the clause.</li>
    * <li>If the body of the clause is successfully evaluated then {@code true} is returned.</li>
    * <li>If the body of the clause is not successfully evaluated then the arguments in the query are backtracked.</li>
    * <li>When there are no more clauses left to check then {@code false} is returned.</li>
    * </ul>
    * Once {@code evaluate(Term...)} has returned {@code true} subsequent invocations of {@code evaluate(Term...)} will
    * attempt to re-evaluate the antecedant of the previously successfully evaluated clause. If the body of the clause
    * is successfully re-evaluated then {@code true} is returned. If the body of the clause is not successfully
    * re-evaluated then the arguments in the query are backtracked and the method continues to iterate through the
    * clauses starting with the next clause in the sequence.
    */
   @Override
   public boolean evaluate(PTerm... queryArgs) {
      try {
         if (retryCurrentClauseAction) {
            if (debugEnabled) {
               spyPoint.logRedo(this, queryArgs);
            }
            if (currentClauseAction.evaluate(queryArgs)) {
               retryCurrentClauseAction = currentClauseAction.couldReEvaluationSucceed();
               if (debugEnabled) {
                  spyPoint.logExit(this, queryArgs);
               }
               return true;
            }
            // attempt at retrying has failed so discard it
            retryCurrentClauseAction = false;
            TermUtils.backtrack(queryArgs);
         } else if (currentClauseIdx == 0) {
            if (debugEnabled) {
               spyPoint.logCall(this, queryArgs);
            }
         } else {
            if (debugEnabled) {
               spyPoint.logRedo(this, queryArgs);
            }
            TermUtils.backtrack(queryArgs);
         }
         // cycle though all rules until none left
         while (clauseActions.hasNext()) {
            currentClauseIdx++;
            currentClauseAction = clauseActions.next().getFree();
            if (currentClauseAction.evaluate(queryArgs)) {
               retryCurrentClauseAction = currentClauseAction.couldReEvaluationSucceed();
               if (debugEnabled) {
                  spyPoint.logExit(this, queryArgs);
               }
               return true;
            } else {
               retryCurrentClauseAction = false;
               TermUtils.backtrack(queryArgs);
            }
         }
         if (debugEnabled) {
            spyPoint.logFail(this, queryArgs);
         }
         return false;
      } catch (CutException e) {
         return false;
      } catch (ProjogException pe) {
         pe.addUserDefinedPredicate(this);
         throw pe;
      } catch (Throwable t) {
         ProjogException pe = new ProjogException("Exception processing: " + key, t);
         pe.addUserDefinedPredicate(this);
         throw pe;
      }
   }

   @Override
   public boolean isRetryable() {
      return true;
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return retryCurrentClauseAction || clauseActions.hasNext();
   }

   public PredicateKey getPredicateKey() {
      return key;
   }

   public int getCurrentClauseIdx() {
      return currentClauseIdx - 1;
   }
}