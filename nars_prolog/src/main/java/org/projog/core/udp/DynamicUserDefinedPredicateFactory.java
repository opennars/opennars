package org.projog.core.udp;

import static org.projog.core.KnowledgeBaseUtils.getProjogProperties;
import static org.projog.core.KnowledgeBaseUtils.getSpyPoints;

import java.util.Iterator;

import org.projog.core.KnowledgeBase;
import org.projog.core.Predicate;
import org.projog.core.PredicateKey;
import org.projog.core.SpyPoints;
import org.projog.core.term.PTerm;
import org.projog.core.udp.interpreter.ClauseAction;
import org.projog.core.udp.interpreter.ClauseActionFactory;
import org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate;

/**
 * Maintains a record of the clauses that represents a "dynamic" user defined predicate.
 * <p>
 * A "dynamic" user defined predicate is one that can have clauses added and removed <i>after</i> it has been first
 * defined. This is normally done using the {@code asserta/1}, {@code assertz/1} and {@code retract/1} predicates.
 * 
 * @see org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate
 */
public final class DynamicUserDefinedPredicateFactory implements UserDefinedPredicateFactory {
   // use array rather than two instances so that references not lost between
   // copies when heads or tails alter
   private static final int FIRST = 0;
   private static final int LAST = 1;

   private final Object LOCK = new Object();
   private final KnowledgeBase kb;
   private final PredicateKey predicateKey;
   private final SpyPoints.SpyPoint spyPoint;
   private final ClauseActionMetaData[] ends = new ClauseActionMetaData[2];

   public DynamicUserDefinedPredicateFactory(KnowledgeBase kb, PredicateKey predicateKey) {
      this.kb = kb;
      this.predicateKey = predicateKey;
      if (getProjogProperties(kb).isSpyPointsEnabled()) {
         this.spyPoint = getSpyPoints(kb).getSpyPoint(predicateKey);
      } else {
         this.spyPoint = null;
      }
   }

   @Override
   public void setKnowledgeBase(KnowledgeBase kb) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Predicate getPredicate(PTerm... args) {
      ClauseActionIterator itr = new ClauseActionIterator(ends[FIRST]);
      return new InterpretedUserDefinedPredicate(predicateKey, spyPoint, itr);
   }

   @Override
   public PredicateKey getPredicateKey() {
      return predicateKey;
   }

   @Override
   public boolean isDynamic() {
      return true;
   }

   /**
    * Returns an iterator over the clauses of this user defined predicate.
    * <p>
    * The iterator returned will have the following characteristics:
    * <ul>
    * <li>Calls to {@link java.util.Iterator#next()} return a <i>new copy</i> of the {@link ClauseModel} to avoid the
    * original being altered.</li>
    * <li>Calls to {@link java.util.Iterator#remove()} <i>do</i> alter the underlying structure of this user defined
    * predicate.</li>
    * <li></li>
    * </ul>
    */
   @Override
   public Iterator<ClauseModel> getImplications() {
      return new ImplicationsIterator();
   }

   @Override
   public void addFirst(ClauseModel clauseModel) {
      synchronized (LOCK) {
         // if first used in a implication antecedant before being used as a consequent,
         // it will originally been created with first and last both null
         ClauseActionMetaData newClause = createClauseActionMetaData(clauseModel);
         ClauseActionMetaData first = ends[FIRST];
         if (first == null) {
            ends[FIRST] = newClause;
            ends[LAST] = newClause;
            return;
         }
         newClause.next = first;
         first.previous = newClause;
         ends[FIRST] = newClause;
      }
   }

   @Override
   public void addLast(ClauseModel clauseModel) {
      synchronized (LOCK) {
         // if first used in a implication antecedant before being used as a consequent,
         // it will originally been created with first and last both null
         ClauseActionMetaData newClause = createClauseActionMetaData(clauseModel);
         ClauseActionMetaData last = ends[LAST];
         if (last == null) {
            ends[FIRST] = newClause;
            ends[LAST] = newClause;
            return;
         }
         last.next = newClause;
         newClause.previous = last;
         ends[LAST] = newClause;
      }
   }

   @Override
   public ClauseModel getClauseModel(int index) {
      ClauseActionMetaData next = ends[FIRST];
      for (int i = 0; i < index; i++) {
         if (next == null) {
            return null;
         }
         next = next.next;
      }
      if (next == null) {
         return null;
      }
      return next.clauseModel.copy();
   }

   private ClauseActionMetaData createClauseActionMetaData(ClauseModel clauseModel) {
      return new ClauseActionMetaData(kb, clauseModel);
   }

   private static class ClauseActionIterator implements Iterator<ClauseAction> {
      private ClauseActionMetaData next;

      ClauseActionIterator(ClauseActionMetaData first) {
         next = first;
      }

      @Override
      public boolean hasNext() {
         return next != null;
      }

      /** need to call getFree on result */
      @Override
      public ClauseAction next() {
         ClauseAction c = next.getClauseAction();
         next = next.next;
         return c;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private class ImplicationsIterator implements Iterator<ClauseModel> {
      private ClauseActionMetaData previous;

      private ClauseActionMetaData getNext() {
         return previous == null ? ends[FIRST] : previous.next;
      }

      @Override
      public boolean hasNext() {
         return getNext() != null;
      }

      /**
       * Returns a <i>new copy</i> to avoid the original being altered.
       */
      @Override
      public ClauseModel next() {
         ClauseActionMetaData next = getNext();
         ClauseModel clauseModel = next.clauseModel;
         previous = next;
         return clauseModel.copy();
      }

      @Override
      public void remove() {
         synchronized (LOCK) {
            if (previous.previous != null) {
               previous.previous.next = previous.next;
            } else {
               ClauseActionMetaData newHead = previous.next;
               if (newHead != null) {
                  newHead.previous = null;
               }
               ends[FIRST] = newHead;
            }
            if (previous.next != null) {
               previous.next.previous = previous.previous;
            } else {
               ClauseActionMetaData newTail = previous.previous;
               if (newTail != null) {
                  newTail.next = null;
               }
               ends[LAST] = newTail;
            }
         }
      }
   }

   private static class ClauseActionMetaData {
      final KnowledgeBase kb;
      final ClauseModel clauseModel;
      ClauseActionMetaData previous;
      ClauseActionMetaData next;

      ClauseActionMetaData(KnowledgeBase kb, ClauseModel clauseModel) {
         this.kb = kb;
         this.clauseModel = clauseModel;
      }

      private ClauseAction getClauseAction() {
         return ClauseActionFactory.getClauseAction(kb, clauseModel);
      }
   }
}