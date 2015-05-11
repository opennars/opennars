package org.projog.core.function.db;

import static org.projog.core.KnowledgeBaseServiceLocator.getServiceLocator;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/* TEST
 % Add three records to the recorded database.
 %TRUE recordz(k,a,_), recordz(k,b,_), recordz(k,c,_)
 
 % Confirm the records have been added.
 %QUERY recorded(k,X)
 %ANSWER X=a
 %ANSWER X=b
 %ANSWER X=c
 
 % Erase (i.e. remove) a record. 
 %QUERY recorded(k,b,X), erase(X)
 %ANSWER X=1
 %NO
 
 % Confirm the record has been removed.
 %QUERY recorded(k,X)
 %ANSWER X=a
 %ANSWER X=c
 */
/**
 * <code>erase(X)</code> - removes a record from the recorded database.
 * <p>
 * Removes from the recorded database the term associated with the reference specified by <code>X</code>. The goal
 * succeeds even if there is no term associated with the specified reference.
 */
public final class Erase extends AbstractSingletonPredicate {
   private RecordedDatabase database;

   @Override
   protected void init() {
      database = getServiceLocator(getKB()).getInstance(RecordedDatabase.class);
   }

   @Override
   public boolean evaluate(PTerm arg) {
      Numeric reference = TermUtils.castToNumeric(arg);
      database.erase(reference.getLong());
      return true;
   }
}