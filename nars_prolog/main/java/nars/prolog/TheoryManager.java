/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.prolog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import nars.prolog.util.Tools;

/**
 * This class defines the Theory Manager who manages the clauses/theory often referred to as the Prolog database.
 * The theory (as a set of clauses) are stored in the ClauseDatabase which in essence is a HashMap grouped by functor/arity.
 * <p/>
 * The TheoryManager functions logically, as prescribed by ISO Standard 7.5.4
 * section. The effects of assertions and retractions shall not be undone if the
 * program subsequently backtracks over the assert or retract call, as prescribed
 * by ISO Standard 7.7.9 section.
 * <p/>
 * To use the TheoryManager one should primarily use the methods assertA, assertZ, consult, retract, abolish and find.
 * <p/>
 *
 * rewritten by:
 * @author ivar.orstavik@hist.no
 *
 * @see Theory
 */
@SuppressWarnings("serial")
public class TheoryManager implements Serializable {

	private ClauseDatabase dynamicDBase;
	private ClauseDatabase staticDBase;
	private ClauseDatabase retractDBase;
	private Prolog engine;
	private PrimitiveManager primitiveManager;
	private Stack<Term> startGoalStack;
	//Theory lastConsultedTheory;

	public void initialize(Prolog vm) {
		dynamicDBase = new ClauseDatabase();
		staticDBase = new ClauseDatabase();
		retractDBase = new ClauseDatabase();
//		lastConsultedTheory = new Theory();
		engine = vm;
		primitiveManager = engine.getPrimitiveManager();
	}

	/**
	 * inserting of a clause at the head of the dbase
	 */
	public synchronized void assertA(Struct clause, boolean dyn, String libName, boolean backtrackable) {
		ClauseInfo d = new ClauseInfo(toClause(clause), libName);
		String key = d.getHead().getPredicateIndicator();
		if (dyn) {
			dynamicDBase.addFirst(key, d);
			if (staticDBase.containsKey(key)) {
				engine.warn("A static predicate with signature " + key + " has been overriden.");
			}
		} else
			staticDBase.addFirst(key, d);
                if (engine.isSpy())
                    engine.spy("INSERTA: " + d.getClause() + "\n");
	}

	/**
	 * inserting of a clause at the end of the dbase
	 */
	public synchronized void assertZ(final Struct clause, final boolean dyn, final String libName, final boolean backtrackable) {
		ClauseInfo d = new ClauseInfo(toClause(clause), libName);
		String key = d.getHead().getPredicateIndicator();
		if (dyn) {
			dynamicDBase.addLast(key, d);
			if (staticDBase.containsKey(key)) {
				engine.warn("A static predicate with signature " + key + " has been overriden.");
			}
		} else
			staticDBase.addLast(key, d);
                if (engine.isSpy())
                    engine.spy("INSERTZ: " + d.getClause() + "\n");
	}

	/**
	 * removing from dbase the first clause with head unifying with clause
	 */
	public synchronized ClauseInfo retract(Struct cl) {
		Struct clause = toClause(cl);
		Struct struct = ((Struct) clause.getArg(0));
		FamilyClausesList family = dynamicDBase.get(struct.getPredicateIndicator());
		ExecutionContext ctx = engine.getEngineManager().getCurrentContext();
		
		/*creo un nuovo clause database x memorizzare la teoria all'atto della retract 
		 * questo lo faccio solo al primo giro della stessa retract 
		 * (e riconosco questo in base all'id del contesto)
		 * sara' la retract da questo db a restituire il risultato
		 */    
		FamilyClausesList familyQuery;
	    if(!retractDBase.containsKey("ctxId "+ctx.getId())){
	    	familyQuery=new FamilyClausesList();
	    	for (int i = 0; i < family.size(); i++) {
	 	       familyQuery.add(family.get(i));
	 	    }
	    	//familyQuery.addAll(family);
	    	retractDBase.put("ctxId "+ctx.getId(), familyQuery);
	    }
	   else {
		   familyQuery=retractDBase.get("ctxId "+ctx.getId());
	   }
		
	    if (familyQuery == null)
			return null;
		//fa la retract dalla teoria base
		if (family != null){
			for (Iterator<ClauseInfo> it = family.iterator(); it.hasNext();) {
				ClauseInfo d = it.next();
				if (clause.match(d.getClause())) {
					it.remove();
				}
			}
		}
		//fa la retract dal retract db
		for (Iterator<ClauseInfo> i = familyQuery.iterator(); i.hasNext();) {
			ClauseInfo d = i.next();
			if (clause.match(d.getClause())) {
				i.remove();
                                if (engine.isSpy())
                                    engine.spy("DELETE: " + d.getClause() + "\n");
				return new ClauseInfo(d.getClause(), null);
			}
		}
		return null;
	}

	/**
	 * removing from dbase all the clauses corresponding to the
	 * predicate indicator passed as a parameter
	 */
	public synchronized boolean abolish(Struct pi) {		
		if (!(pi instanceof Struct) || !pi.isGround() || !(pi.getArity() == 2))
			throw new IllegalArgumentException(pi + " is not a valid Struct");
		if(!pi.getName().equals("/"))
				throw new IllegalArgumentException(pi + " has not the valid predicate name. Espected '/' but was " + pi.getName());
		
		String arg0 = Tools.removeApices(pi.getArg(0).toString());
		String arg1 = Tools.removeApices(pi.getArg(1).toString());
		String key =  arg0 + "/" + arg1;
		List<ClauseInfo> abolished = dynamicDBase.abolish(key); /* Reviewed by Paolo Contessi: LinkedList -> List */
		if (abolished != null)
                    if (engine.isSpy())
			engine.spy("ABOLISHED: " + key + " number of clauses=" + abolished.size() + "\n");
		return true;
	}

	/**
	 * Returns a family of clauses with functor and arity equals
	 * to the functor and arity of the term passed as a parameter
	 *
	 * Reviewed by Paolo Contessi: modified according to new ClauseDatabase
	 * implementation
	 */
	public synchronized List<ClauseInfo> find(Term headt) {
		if (headt instanceof Struct) {
			//String key = ((Struct) headt).getPredicateIndicator();
			List<ClauseInfo> list = dynamicDBase.getPredicates(headt);
			if (list.isEmpty())
				list = staticDBase.getPredicates(headt);
			return list;
		}

		if (headt instanceof Var){
			//            List l = new LinkedList();
			//            for (Iterator iterator = clauseDBase.iterator(); iterator.hasNext();) {
			//                ClauseInfo ci =  (ClauseInfo) iterator.next();
			//                if(ci.dynamic)
			//                    l.add(ci);
			//            }
			//            return l;
			throw new RuntimeException();
		}
		return new LinkedList<>();
	}

	/**
	 * Consults a theory.
	 *
	 * @param theory        theory to add
	 * @param dynamicTheory if it is true, then the clauses are marked as dynamic
	 * @param libName       if it not null, then the clauses are marked to belong to the specified library
	 */
	public void consult(PrologTermIterator theory, boolean dynamicTheory, String libName) throws InvalidTheoryException {
            consult(theory.iterator(engine), dynamicTheory, libName);            
	}
        
	public void consult(final Struct theory, boolean dynamicTheory, String libName) throws InvalidTheoryException {
            startGoalStack = new Stack<>();            
            try {                
                if (!runDirective(theory))
                        assertZ(theory, dynamicTheory, libName, true);
            } catch (InvalidTermException e) {
                    throw new InvalidTheoryException(e.getMessage(), 0, e.line, e.pos);
            }
	}
	public void consult(final Iterator<? extends Term> theory, boolean dynamicTheory, String libName) throws InvalidTheoryException {
            startGoalStack = new Stack<>();
            int clause = 1;
            /**/
            // iterate and assert all clauses in theory
            try {
                    for (Iterator<? extends Term> it = theory; it.hasNext();) {
                            clause++;
                            Struct d = (Struct) it.next();
                            if (!runDirective(d))
                                    assertZ(d, dynamicTheory, libName, true);
                    }
            } catch (InvalidTermException e) {
                    throw new InvalidTheoryException(e.getMessage(), clause, e.line, e.pos);
            }
	}        
        
	/**
	 * Binds clauses in the database with the corresponding
	 * primitive predicate, if any
	 */
	public void rebindPrimitives() {
		for (ClauseInfo d:dynamicDBase){
			for(AbstractSubGoalTree sge:d.getBody()){
				Term t = ((SubGoalElement)sge).getValue();
				primitiveManager.identifyPredicate(t);
			}
		}
	}

	/**
	 * Clears the clause dbase.
	 */
	public synchronized void clear() {
		dynamicDBase = new ClauseDatabase();
	}

	/**
	 * remove all the clauses of lib theory
	 */
	public synchronized void removeLibraryTheory(String libName) {
		for (Iterator<ClauseInfo> allClauses = staticDBase.iterator(); allClauses.hasNext();) {
			ClauseInfo d = allClauses.next();
			if (d.libName != null && libName.equals(d.libName))
			{
				try 
				{
					// Rimuovendolo da allClauses si elimina solo il valore e non la chiave
					allClauses.remove();
				}
				catch (Exception e){}
			}
		}
	}


	private boolean runDirective(final Struct c) {
            if (c.getArity()!=1)
                return false;
            Term t = c.getTerm(0);
            if (!(t instanceof Struct))
                return false;
            
            Struct dir = (Struct)t;
            
            if ("':-'".equals(c.getName()) || ":-".equals(c.getName())) {


                    try {
                            if (!primitiveManager.evalAsDirective(dir))
                                    engine.warn("The directive " + dir.getPredicateIndicator() + " is unknown.");
                    } catch (Throwable th) {
                            engine.warn("An exception has been thrown during the execution of the " +
                                            dir.getPredicateIndicator() + " directive.\n" + th.getMessage());
                    }

                    return true;
            }
            return false;
	}

	/**
	 * Gets a clause from a generic Term
	 */
	private Struct toClause(Struct t) {		//PRIMITIVE
		// TODO bad, slow way of cloning. requires approx twice the time necessary
		t = (Struct) Term.createTerm(t.toString(), this.engine.getOperatorManager());
		if (!t.isClause())
			t = new Struct(":-", t, Struct.TRUE /* new Struct("true") */);
		primitiveManager.identifyPredicate(t);
		return t;
	}

	public synchronized SolveInfo solveTheoryGoal() {
		Struct s = null;
		while (!startGoalStack.empty()) {
			s = (s == null) ?
					(Struct) startGoalStack.pop() :
						new Struct(",", (Struct) startGoalStack.pop(), s);
		}
		if (s != null) {
			try {
				return engine.solve(s);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
                return null;
	}

	/**
	 * add a goal eventually defined by last parsed theory.
	 */
	public synchronized void addStartGoal(Struct g) {
		startGoalStack.push(g);
	}

	/**
	 * saves the dbase on a output stream.
	 */
	synchronized boolean save(OutputStream os, boolean onlyDynamic) {
		try {
			new DataOutputStream(os).writeBytes(getTheory(onlyDynamic));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Gets current theory
	 *
	 * @param onlyDynamic if true, fetches only dynamic clauses
	 */
	public synchronized String getTheory(boolean onlyDynamic) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<ClauseInfo> dynamicClauses = dynamicDBase.iterator(); dynamicClauses.hasNext();) {
			ClauseInfo d = dynamicClauses.next();
			buffer.append(d.toString(engine.getOperatorManager())).append("\n");
		}
		if (!onlyDynamic)
			for (Iterator<ClauseInfo> staticClauses = staticDBase.iterator(); staticClauses.hasNext();) {
				ClauseInfo d = staticClauses.next();
				buffer.append(d.toString(engine.getOperatorManager())).append("\n");
			}
		return buffer.toString();
	}

//	/**
//	 * Gets last consulted theory
//	 * @return  last theory
//	 */
//	public synchronized Theory getLastConsultedTheory() {
//		return lastConsultedTheory;
//	}
	
	public void clearRetractDB() {
		this.retractDBase=new ClauseDatabase();
	}

}