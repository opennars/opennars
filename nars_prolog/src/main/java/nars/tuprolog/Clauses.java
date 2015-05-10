package nars.tuprolog;

import java.util.Iterator;

/**
 * Stores an index of clauses
 */
public interface Clauses extends Iterable<Clause> {

	 void addFirst(String key, Clause d);

	 void addLast(String key, Clause d);



	/**
	 * Retrieves a list of the predicates which has the same name and arity
	 * as the goal and which has a compatible first-arg for matching.
	 *
	 * @param headt The goal
	 * @return  The stream of matching-compatible predicates, or null if there are none.
	 * must return null if there are none, otherwise the callee doesn't know if it's empty
	 */
	Iterator<Clause> getPredicates(PTerm headt);

	/**
	 * Retrieves the list of clauses of the requested family
	 *
	 * @param key   Goal's Predicate Indicator
	 * @return      The stream of family clauses, or null if there are none.
	 * must return null if there are none, otherwise the callee doesn't know if it's empty
	 */
	Iterator<Clause> getPredicates(String key);

	@Override
	Iterator<Clause> iterator();

	boolean containsKey(String ctxID);

	ClauseIndex put(String ctxID, ClauseIndex index);

	ClauseIndex get(String ctxID);

	ClauseIndex remove(String ctxID);

	void clear();
}
