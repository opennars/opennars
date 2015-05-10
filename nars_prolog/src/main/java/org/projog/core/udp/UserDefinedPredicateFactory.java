package org.projog.core.udp;

import java.util.Iterator;

import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;

/**
 * Maintains a record of the clauses that define a user defined predicate.
 * <p>
 * A user defined predicate is a predicate that is constructed from Prolog syntax consulted at runtime.
 * <p>
 * <img src="doc-files/UserDefinedPredicateFactory.png">
 */
public interface UserDefinedPredicateFactory extends PredicateFactory {
   /**
    * Adds a clause to the beginning of the predicate's list of clauses.
    * 
    * @param clauseModel the clause to add to the beginning of the predicate
    */
   void addFirst(ClauseModel clauseModel);

   /**
    * Adds a clause to the end of the predicate's list of clauses.
    * 
    * @param clauseModel the clause to add to the end of the predicate
    */
   void addLast(ClauseModel clauseModel);

   /**
    * Returns the key for the predicate this object represents
    * 
    * @return the key for the predicate this object represents
    */
   PredicateKey getPredicateKey();

   /**
    * Returns an iterator over the clauses in the predicate in proper sequence.
    * 
    * @return an iterator over the clauses in the predicate in proper sequence.
    */
   Iterator<ClauseModel> getImplications();

   /**
    * Returns {@code true} is this predicate is dynamic.
    * <p>
    * A "dynamic" predicate is a user defined predicate that can have clauses added or removed after is first defined.
    * 
    * @return {@code true} is this predicate is dynamic
    */
   boolean isDynamic();

   /**
    * Returns the clause at the specified position in this predicate's list of clauses.
    * 
    * @param index index of the clause to return
    * @return the clause at the specified position in this predicate's list of clauses or {@code null} if out of bounds
    */
   public ClauseModel getClauseModel(int index);
}