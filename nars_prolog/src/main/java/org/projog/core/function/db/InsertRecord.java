package org.projog.core.function.db;

import static org.projog.core.KnowledgeBaseServiceLocator.getServiceLocator;
import static org.projog.core.term.TermUtils.createAnonymousVariable;

import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 % Example of recorda/3.

 %QUERY recorda(a,q,X)
 %ANSWER X=0

 % Note: recorda/2 is equivalent to calling recorda/3 with the third argument as an anonymous variable.
 %TRUE recorda(a,w)

 %QUERY recorded(X,Y,Z)
 %ANSWER
 % X=a
 % Y=w
 % Z=1
 %ANSWER
 %ANSWER
 % X=a
 % Y=q
 % Z=0
 %ANSWER

 % Note: recorded/2 is equivalent to calling recorded/3 with the third argument as an anonymous variable.
 %QUERY recorded(a,Y)
 %ANSWER Y=w
 %ANSWER Y=q

 % Example of recordz/3.

 %QUERY recordz(b,q,X)
 %ANSWER X=2
 
 % Note: recordz/2 is equivalent to calling recordz/3 with the third argument as an anonymous variable.
 %TRUE recordz(b,w) 

 %QUERY recorded(b,Y)
 %ANSWER Y=q
 %ANSWER Y=w
 */
/**
 * <code>recorda(X,Y,Z)</code> / <code>recordz(X,Y,Z)</code> - associates a term with a key.
 * <p>
 * <code>recorda(X,Y,Z)</code> associates <code>Y</code> with <code>X</code>. The unique reference for this association
 * will be unified with <code>Z</code>. <code>Y</code> is added to the <i>start</i> of the list of terms already
 * associated with <code>X</code>.
 * </p>
 * <p>
 * <code>recordz(X,Y,Z)</code> associates <code>Y</code> with <code>X</code>. The unique reference for this association
 * will be unified with <code>Z</code>. <code>Y</code> is added to the <i>end</i> of the list of terms already
 * associated with <code>X</code>.
 * </p>
 */
public final class InsertRecord extends AbstractSingletonPredicate {
   public static InsertRecord recordA() {
      return new InsertRecord(false);
   }

   public static InsertRecord recordZ() {
      return new InsertRecord(true);
   }

   private final boolean insertLast;
   private RecordedDatabase database;

   private InsertRecord(boolean insertLast) {
      this.insertLast = insertLast;
   }

   @Override
   public void init() {
      database = getServiceLocator(getKnowledgeBase()).getInstance(RecordedDatabase.class);
   }

   @Override
   public boolean evaluate(PTerm key, PTerm value) {
      return evaluate(key, value, createAnonymousVariable());
   }

   @Override
   public boolean evaluate(PTerm key, PTerm value, PTerm reference) {
      if (!reference.type().isVariable()) {
         return false;
      }
      PredicateKey k = PredicateKey.createForTerm(key);
      PTerm result = database.add(k, value, insertLast);
      return reference.unify(result);
   }
}