package org.projog.core.function.db;

import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;

import java.util.Iterator;

import static org.projog.core.KnowledgeBaseServiceLocator.getServiceLocator;
import static org.projog.core.term.TermUtils.createAnonymousVariable;

/* TEST
 %FALSE recorded(X,Y,Z)
 
 % Note: recorded/2 is equivalent to calling recorded/3 with the third argument as an anonymous variable.
 %FALSE recorded(X,Y)
 */
/**
 * <code>recorded(X,Y,Z)</code> - checks if a term is associated with a key.
 * <p>
 * <code>recorded(X,Y,Z)</code> succeeds if there exists an association between the key represented by <code>X</code>
 * and the term represented by <code>Y</code>, with the reference represented by <code>Z</code>.
 */
public final class Recorded extends AbstractRetryablePredicate {
   private final Iterator<Record> itr;

   public Recorded() {
      this.itr = null;
   }

   private Recorded(Iterator<Record> itr) {
      this.itr = itr;
   }

   @Override
   public Recorded getPredicate(PTerm key, PTerm value) {
      return getPredicate(key, value, createAnonymousVariable());
   }

   @Override
   public Recorded getPredicate(PTerm key, PTerm value, PTerm reference) {
      RecordedDatabase database = getServiceLocator(getKB()).getInstance(RecordedDatabase.class);
      Iterator<Record> itr = getIterator(key, database);
      return new Recorded(itr);
   }

   private Iterator<Record> getIterator(PTerm key, RecordedDatabase database) {
      if (key.type().isVariable()) {
         return database.getAll();
      } else {
         PredicateKey k = PredicateKey.createForTerm(key);
         return database.getChain(k);
      }
   }

   @Override
   public boolean evaluate(PTerm key, PTerm value) {
      return evaluate(key, value, createAnonymousVariable());
   }

   @Override
   public boolean evaluate(PTerm key, PTerm value, PTerm reference) {
      while (couldReEvaluationSucceed()) {
         Record next = itr.next();
         key.backtrack();
         value.backtrack();
         reference.backtrack();
         if (unify(next, key, value, reference)) {
            return true;
         }
      }
      return false;
   }

   private boolean unify(Record record, PTerm key, PTerm value, PTerm reference) {
      return key.unify(record.getKey()) && value.unify(record.getValue()) && reference.unify(record.getReference());
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return itr.hasNext();
   }
}