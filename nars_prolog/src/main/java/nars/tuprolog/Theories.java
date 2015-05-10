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
package nars.tuprolog;

import nars.nal.AbstractSubGoalTree;
import nars.nal.term.Term;
import nars.tuprolog.util.Tools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

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
 * <p/>
 * rewritten by:
 *
 * @author ivar.orstavik@hist.no
 * @see Theory
 */
@SuppressWarnings("serial")
public class Theories implements Serializable {

    private final Clauses dynamicDBase;
    private final Clauses staticDBase;
    private final Clauses retractDBase;
    private final Prolog engine;
    private final Primitives primitives;
    private final Deque<PTerm> startGoalStack = new ArrayDeque();
    //Theory lastConsultedTheory;

    public Theories(Prolog vm, Clauses dynamic, Clauses statics) {
        engine = vm;
        dynamicDBase = dynamic;
        staticDBase = statics;
        retractDBase = new MutableClauses();
//		lastConsultedTheory = new Theory();
        primitives = vm.getPrimitives();
    }

    /**
     * inserting of a clause at the head of the dbase
     */
    public synchronized boolean assertA(Struct clause, boolean dyn, String libName, boolean backtrackable) {
        Clause d = new Clause(toClause(clause), libName);
        String key = d.getHead().getPredicateIndicator();
        if (dyn) {
            dynamicDBase.addFirst(key, d);
            if (staticDBase.containsKey(key)) {
                engine.warn("A static predicate with signature " + key + " has been overriden.");
                return false;
            }
        } else
            staticDBase.addFirst(key, d);
        if (engine.isSpy())
            engine.spy("INSERTA: " + d.getClause() + '\n');
        return true;
    }

    /**
     * inserting of a clause at the end of the dbase
     */
    public synchronized boolean assertZ(final Struct clause, final boolean dyn, final String libName, final boolean backtrackable) {
        Clause d = new Clause(toClause(clause), libName);
        String key = d.getHead().getPredicateIndicator();
        if (dyn) {
            dynamicDBase.addLast(key, d);
            if (staticDBase.containsKey(key)) {
                engine.warn("A static predicate with signature " + key + " has been overriden.");
                return false;
            }
            return true;
        } else
            staticDBase.addLast(key, d);
        if (engine.isSpy())
            engine.spy("INSERTZ: " + d.getClause() + '\n');
        return true;
    }

    /**
     * removing from dbase the first clause with head unifying with clause
     */
    public synchronized Clause retract(Struct cl) {
        Struct clause = toClause(cl);
        Struct struct = ((Struct) clause.getTermX(0));
        ClauseIndex family = dynamicDBase.get(struct.getPredicateIndicator());
        ExecutionContext ctx = engine.getCurrentContext();

		/*creo un nuovo clause database x memorizzare la teoria all'atto della retract 
		 * questo lo faccio solo al primo giro della stessa retract 
		 * (e riconosco questo in base all'id del contesto)
		 * sara' la retract da questo db a restituire il risultato
		 */
        ClauseIndex familyQuery;
        String ctxID = "ctxId " + ctx.getId();
        if (!retractDBase.containsKey(ctxID)) {
            familyQuery = new ClauseIndex();
            for (int i = 0; i < family.size(); i++) {
                familyQuery.add(family.get(i));
            }
            //familyQuery.addAll(family);
            retractDBase.put(ctxID, familyQuery);
        } else {
            familyQuery = retractDBase.get(ctxID);
        }

        if (familyQuery == null)
            return null;
        //fa la retract dalla teoria base
        if (family != null) {
            for (Iterator<Clause> it = family.iterator(); it.hasNext(); ) {
                Clause d = it.next();
                if (clause.match(d.getClause())) {
                    it.remove();
                }
            }
        }
        //fa la retract dal retract db
        for (Iterator<Clause> i = familyQuery.iterator(); i.hasNext(); ) {
            Clause d = i.next();
            if (clause.match(d.getClause())) {
                i.remove();
                if (engine.isSpy())
                    engine.spy("DELETE: " + d.getClause() + '\n');
                return new Clause(d.getClause(), null);
            }
        }
        return null;
    }

    /**
     * removing from dbase all the clauses corresponding to the
     * predicate indicator passed as a parameter
     */
    public synchronized boolean remove(Struct pi) {
        if (!(pi instanceof Struct) || !pi.isGround() || !(pi.size() == 2))
            throw new IllegalArgumentException(pi + " is not a valid Struct");
        if (!pi.getName().equals("/"))
            throw new IllegalArgumentException(pi + " has not the valid predicate name. Espected '/' but was " + pi.getName());

        String arg0 = Tools.removeApices(pi.getTermX(0).toString());
        String arg1 = Tools.removeApices(pi.getTermX(1).toString());
        String key = arg0 + '/' + arg1;
        List<Clause> abolished = dynamicDBase.remove(key); /* Reviewed by Paolo Contessi: LinkedList -> List */
        if (abolished != null)
            if (engine.isSpy())
                engine.spy("ABOLISHED: " + key + " number of clauses=" + abolished.size() + '\n');
        return true;
    }

    /**
     * Returns a family of clauses with functor and arity equals
     * to the functor and arity of the term passed as a parameter
     * <p/>
     * Reviewed by Paolo Contessi: modified according to new ClauseDatabase
     * implementation
     */
    public synchronized Iterator<Clause> find(PTerm headt) {
        if (headt instanceof Struct) {
            //String key = ((Struct) headt).getPredicateIndicator();
            Iterator<Clause> list = dynamicDBase.getPredicates(headt);
            if (list == null)
                list = staticDBase.getPredicates(headt);
            return list;
        }

        if (headt instanceof Var) {
            //            List l = new LinkedList();
            //            for (Iterator iterator = clauseDBase.iterator(); iterator.hasNext();) {
            //                ClauseInfo ci =  (ClauseInfo) iterator.next();
            //                if(ci.dynamic)
            //                    l.add(ci);
            //            }
            //            return l;
            throw new RuntimeException();
        }
        return null;
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
        startGoalStack.clear();
        try {
            if (!runDirective(theory))
                assertZ(theory, dynamicTheory, libName, true);
        } catch (InvalidTermException e) {
            throw new InvalidTheoryException(e.getMessage(), 0, e.line, e.pos);
        }
    }

    public void consult(final Iterator<? extends Term> theory, boolean dynamicTheory, String libName) throws InvalidTheoryException {
        startGoalStack.clear();
        int clause = 1;
            /**/
        // iterate and assert all clauses in theory
        try {
            for (Iterator<? extends Term> it = theory; it.hasNext(); ) {
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
        for (Clause d : dynamicDBase) {
            for (AbstractSubGoalTree sge : d.getBody()) {
                PTerm t = ((SubGoalElement) sge).getValue();
                primitives.identifyPredicate(t);
            }
        }
    }

    /**
     * Clears the clause dbase.
     */
    public synchronized void clear() {
        dynamicDBase.clear();
    }

    /**
     * remove all the clauses of lib theory
     */
    public synchronized void removeLibraryTheory(String libName) {
        for (Iterator<Clause> allClauses = staticDBase.iterator(); allClauses.hasNext(); ) {
            Clause d = allClauses.next();
            if (d.libName != null && libName.equals(d.libName)) {
                try {
                    // Rimuovendolo da allClauses si elimina solo il valore e non la chiave
                    allClauses.remove();
                } catch (Exception e) {
                }
            }
        }
    }


    private boolean runDirective(final Struct c) {
        if (c.size() != 1)
            return false;
        Term t = c.getTerm(0);
        if (!(t instanceof Struct))
            return false;

        Struct dir = (Struct) t;

        final String n = c.getName();
        final int ns = n.length();
        if (n.startsWith(":-")) {

            if (ns == 2 || (ns == 3 && n.charAt(2) == '\'')) {
                //"':-'".equals(c.getName()) || ":-".equals(c.getName())) {

                try {
                    if (!primitives.evalAsDirective(dir))
                        engine.warn("The directive " + dir.getPredicateIndicator() + " is unknown.");
                } catch (Throwable th) {
                    engine.warn("An exception has been thrown during the execution of the " +
                            dir.getPredicateIndicator() + " directive.\n" + th.getMessage());
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Gets a clause from a generic Term
     */
    private Struct toClause(Struct t) {        //PRIMITIVE
        // TODO bad, slow way of cloning. requires approx twice the time necessary
        t = (Struct) PTerm.createTerm(t.toString(), this.engine.getOperators());
        if (!t.isClause())
            t = new Struct(":-", t, Struct.TRUE /* new Struct("true") */);
        primitives.identifyPredicate(t);
        return t;
    }

    public synchronized SolveInfo solveTheoryGoal() {
        Struct s = null;
        while (!startGoalStack.isEmpty()) {

            PTerm popped = startGoalStack.pop();
            s = (s == null) ?
                    (Struct) popped :
                    new Struct(",", popped, s);
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
        StringBuilder buffer = new StringBuilder();
        for (Iterator<Clause> dynamicClauses = dynamicDBase.iterator(); dynamicClauses.hasNext(); ) {
            Clause d = dynamicClauses.next();
            buffer.append(d.toString(engine.getOperators())).append('\n');
        }
        if (!onlyDynamic)
            for (Iterator<Clause> staticClauses = staticDBase.iterator(); staticClauses.hasNext(); ) {
                Clause d = staticClauses.next();
                buffer.append(d.toString(engine.getOperators())).append('\n');
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
        this.retractDBase.clear();
    }


}