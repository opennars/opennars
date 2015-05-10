package nars.tuprolog;


import nars.nal.term.Term;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * <code>FamilyClausesList</code> is a common <code>LinkedList</code>
 * which stores {@link Clause} objects. Internally it indexes stored data
 * in such a way that, knowing what type of clauses are required, only
 * goal compatible clauses are returned
 *
 * @author Paolo Contessi
 * @since 2.2
 * 
 * @see LinkedList
 */
@SuppressWarnings("serial")
public class ClauseIndex extends ArrayList<Clause> {
	
	private FamilyClausesIndex<PNum> numCompClausesIndex;
	private FamilyClausesIndex<String> constantCompClausesIndex;
	private FamilyClausesIndex<String> structCompClausesIndex;
	private LinkedList<Clause> listCompClausesList;

	//private LinkedList<ClauseInfo> clausesList;

	public ClauseIndex(){
		super();

		numCompClausesIndex = new FamilyClausesIndex<>();
		constantCompClausesIndex = new FamilyClausesIndex<>();
		structCompClausesIndex = new FamilyClausesIndex<>();

		listCompClausesList = new LinkedList<>();
	}

	/**
	 * Adds the given clause as first of the family
	 *
	 * @param ci    The clause to be added (with related informations)
	 */	
	public void addFirst(Clause ci){
		super.add(0, ci);

		// Add first in type related storage
		register(ci, true);
	}

	/**
	 * Adds the given clause as last of the family
	 *
	 * @param ci    The clause to be added (with related informations)
	 */
	public void addLast(Clause ci){
		super.add(ci);

		// Add last in type related storage
		register(ci, false);
	}

	@Override
	public boolean add(Clause o) {
		addLast(o);

		return true;
	}

	public Clause removeFirst() {
		Clause ci = get(0);
		if (remove(ci)){
			return ci;
		}

		return null;
	}

	public Clause removeLast() {
		Clause ci = get(size()-1);
		if (remove(ci)){
			return ci;
		}
		return null;
	}
	
	public Clause remove(){
		return removeFirst();
	}

	@Override
	public Clause remove(int index){
		Clause ci = super.get(index);

		if(remove(ci)){
			return ci;
		}

		return null;
	}

	@Override
	public boolean remove(Object ci){
		if(super.remove(ci))
		{
			unregister((Clause) ci);

			return true;
		}
		return false;
	}

	@Override
	public void clear(){
		while(size() > 0){
			removeFirst();
		}
	}

	/**
	 * Retrieves a sublist of all the clauses of the same family as the goal
	 * and which, in all probability, could match with the given goal
	 *
	 * @param goal  The goal to be resolved
	 * @return      The list of goal-compatible predicates
	 */
	public Iterator<Clause> get(PTerm goal){
		// Gets the correct list and encapsulates it in ReadOnlyLinkedList
		if(goal instanceof Struct){
			Struct g = (Struct) goal.getTerm();

			/*
			 * If no arguments no optimization can be applied
			 * (and probably no optimization is needed)
			 */
			if(g.size() == 0){
				return iterator();
			}

			/* Retrieves first argument and checks type */
			Term t = g.getTermX(0).getTerm();
			if(t instanceof Var){
				/*
				 * if first argument is an unbounded variable,
				 * no reasoning is possible, all family must be returned
				 */
				return iterator();
			} else if(t.isAtomic()){
				if(t instanceof PNum){
					/* retrieves clauses whose first argument is numeric (or Var)
					 * and same as goal's first argument, if no clauses
					 * are retrieved, all clauses with a variable
					 * as first argument
					 */
					return numCompClausesIndex.get((PNum) t).iterator();
				} else if(t instanceof Struct){
					/* retrieves clauses whose first argument is a constant (or Var)
					 * and same as goal's first argument, if no clauses
					 * are retrieved, all clauses with a variable
					 * as first argument
					 */
					return constantCompClausesIndex.get(((Struct) t).getName()).iterator();
				}
			} else if(t instanceof Struct){
				if(isAList((Struct) t)){
					/* retrieves clauses which has a list  (or Var) as first argument */
					return listCompClausesList.iterator();
				} else {
					/* retrieves clauses whose first argument is a struct (or Var)
					 * and same as goal's first argument, if no clauses
					 * are retrieved, all clauses with a variable
					 * as first argument
					 */
					return structCompClausesIndex.get(((Struct) t).getPredicateIndicator()).iterator();
				}
			}
		}

		/* Default behaviour: no optimization done */
		return iterator();
	}

	@Override
	public Iterator<Clause> iterator(){
		return listIterator(0);
	}

	@Override
	public ListIterator<Clause> listIterator(){
		return new ListItr(this,0).getIt();
	}

	private ListIterator<Clause> superListIterator(int index){
		return super.listIterator(index);
	}

	@Override
	public ListIterator<Clause> listIterator(int index){
		return new ListItr(this,index).getIt();
	}

	private boolean isAList(Struct t) {
		/*
		 * Checks if a Struct is also a list.
		 * A list can be an empty list, or a Struct with name equals to "."
		 * and arity equals to 2.
		 */
		return t.isEmptyList() || (t.getName().equals(".") && t.size() == 2);

	}

	// Updates indexes, storing informations about the last added clause
	private void register(Clause ci, boolean first){
		// See FamilyClausesList.get(Term): same concept
		PTerm clause = ci.getHead();
		if(clause instanceof Struct){
			Struct g = (Struct) clause.getTerm();

			if(g.size() == 0){
				return;
			}

			Term t = g.getTermX(0).getTerm();
			if(t instanceof Var){
				numCompClausesIndex.insertAsShared(ci, first);
				constantCompClausesIndex.insertAsShared(ci, first);
				structCompClausesIndex.insertAsShared(ci, first);

				if(first){
					listCompClausesList.addFirst(ci);
				} else {
					listCompClausesList.addLast(ci);
				}
			} else if(t.isAtomic()){
				if(t instanceof PNum){
					numCompClausesIndex.insert((PNum) t,ci, first);
				} else if(t instanceof Struct){
					constantCompClausesIndex.insert(((Struct) t).getName(), ci, first);
				}
			} else if(t instanceof Struct){
				if(isAList((Struct) t)){
					if(first){
						listCompClausesList.addFirst(ci);
					} else {
						listCompClausesList.addLast(ci);
					}
				} else {
					structCompClausesIndex.insert(((Struct) t).getPredicateIndicator(), ci, first);
				}
			}
		}
	}

	// Updates indexes, deleting informations about the last removed clause
	public void unregister(Clause ci) {
		PTerm clause = ci.getHead();
		if(clause instanceof Struct){
			Struct g = (Struct) clause.getTerm();

			if(g.size() == 0){
				return;
			}

			Term t = g.getTermX(0).getTerm();
			if(t instanceof Var){
				numCompClausesIndex.removeShared(ci);
				constantCompClausesIndex.removeShared(ci);
				structCompClausesIndex.removeShared(ci);

				listCompClausesList.remove(ci);
			} else if(t.isAtomic()){
				if(t instanceof PNum){
					numCompClausesIndex.remove((PNum) t, ci);
				} else if(t instanceof Struct){
					constantCompClausesIndex.remove(((Struct) t).getName(), ci);
				}
			} else if(t instanceof Struct){
				if(t.isList()){
					listCompClausesList.remove(ci);
				} else {
					structCompClausesIndex.remove(((Struct) t).getPredicateIndicator(),ci);
				}
			}
		}
	}

	private class ListItr implements ListIterator<Clause> {

		private ListIterator<Clause> it;
		private ClauseIndex l;
		private int currentIndex = 0;

		public ListItr(ClauseIndex list, int index){
			l = list;
			it = list.superListIterator(index);
		}

		public boolean hasNext() {
			return it.hasNext();
		}

		public Clause next() {
			// Alessandro Montanari - alessandro.montanar5@studio.unibo.it
			currentIndex = it.nextIndex();

			return it.next();
		}

		public boolean hasPrevious() {
			return it.hasPrevious();
		}

		public Clause previous() {
			// Alessandro Montanari - alessandro.montanar5@studio.unibo.it
			currentIndex = it.previousIndex();

			return it.previous();
		}

		public int nextIndex() {
			return it.nextIndex();
		}

		public int previousIndex() {
			return it.previousIndex();
		}

		public void remove() {
			// Alessandro Montanari - alessandro.montanar5@studio.unibo.it
			Clause ci = l.get(currentIndex);

			it.remove();

			unregister(ci);
		}

		public void set(Clause o) {
			it.set(o);
			//throw new UnsupportedOperationException("Not supported.");
		}

		public void add(Clause o) {
                    l.addLast(o);
                    

		}

		public ListIterator<Clause> getIt(){
			return this;
		}    


	}

	// Short test about the new implementation of the ListItr
	// Alessandro Montanari - alessandro.montanar5@studio.unibo.it
	@SuppressWarnings("unused")
	private static class ListItrTest{

		private static ClauseIndex clauseList = new ClauseIndex();

		public static void main(String[] args) {
			Clause first = new Clause(new Struct(new Struct("First"),new Struct("First")),"First Element");
			Clause second = new Clause(new Struct(new Struct("Second"),new Struct("Second")),"Second Element");
			Clause third = new Clause(new Struct(new Struct("Third"),new Struct("Third")),"Third Element");
			Clause fourth = new Clause(new Struct(new Struct("Fourth"),new Struct("Fourth")),"Fourth Element");

			clauseList.add(first);
			clauseList.add(second);
			clauseList.add(third);
			clauseList.add(fourth);
			
			// clauseList = [First, Second, Third, Fourh]
			
			ListIterator<Clause> allClauses = clauseList.listIterator();
			// Get the first object and remove it
			allClauses.next();
			allClauses.remove();
			if(clauseList.contains(first))
			{
				System.out.println("Error!");
				System.exit(-1);
			}

			// First object removed
			// clauseList = [Second, Third, Fourh]

			// Get the second object
			allClauses.next();
			// Get the third object
			allClauses.next();
			// Get the third object
			allClauses.previous();
			// Get the second object and remove it
			allClauses.previous();
			allClauses.remove();
			if(clauseList.contains(second))
			{
				System.out.println("Error!");
				System.exit(-2);
			}
			
			// clauseList = [Third, Fourh]

			System.out.println("Ok!!!");
		}
	}

}


