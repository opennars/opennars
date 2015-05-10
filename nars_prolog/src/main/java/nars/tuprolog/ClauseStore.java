package nars.tuprolog;

import nars.nal.term.Term;
import nars.tuprolog.util.OneWayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * A list of clauses belonging to the same family as a goal. A family is
 * composed by clauses with the same functor and arity.
 */
public class ClauseStore {


    private OneWayList<Clause> clauses;
    protected final PTerm goal;
    protected final List<Var> vars;
    private boolean haveAlternatives;

    protected ClauseStore(PTerm goal, List<Var> vars) {
        this.goal = goal;
        this.vars = vars;
        clauses = null;
    }


    /**
     * Carica una famiglia di clausole
     * <p>
     * Reviewed by Paolo Contessi:
     * OneWayList.transform(List) -> OneWayList.transform2(List)
     *
     * @param familyClauses
     */
    public static ClauseStore build(PTerm goal, List<Var> vars, Iterator<Clause> familyClauses) {
        ClauseStore clauseStore = new ClauseStore(goal, vars);
        if ((clauseStore.clauses = OneWayList.transform2(familyClauses)) == null
                || !clauseStore.existCompatibleClause())
            return null;
        return clauseStore;
    }


    /**
     * Restituisce la clausola da caricare
     */
    public Clause fetch() {
        if (clauses == null) return null;
        deunify(vars);
        if (!checkCompatibility(goal))
            return null;
        Clause clause = clauses.getHead();
        clauses = clauses.getTail();
        haveAlternatives = checkCompatibility(goal);
        return clause;
    }

    @Deprecated
    public boolean checkCompatibility(PTerm goal) {
        //v1 and v2 will be re-used during the loop
        ArrayList<nars.tuprolog.Var> v1 = new ArrayList(), v2 = new ArrayList();
        long now = System.currentTimeMillis();
        return checkCompatibility(goal, v1, v2, now);
    }


    public boolean haveAlternatives() {
        return haveAlternatives;
    }


    /**
     * Verify if there is a term in compatibleGoals compatible with goal.
     *
     * @param goal
     * @param compGoals
     * @return true if compatible or false otherwise.
     */
    protected boolean existCompatibleClause(ArrayList<Var> v1, ArrayList<Var> v2, long now) {
        List<Term> saveUnifications = deunify(vars);
        boolean found = checkCompatibility(goal, v1, v2, now);
        reunify(vars, saveUnifications);
        return found;
    }

    protected boolean existCompatibleClause() {
        //v1 and v2 will be re-used during the loop
        ArrayList<nars.tuprolog.Var> v1 = new ArrayList();
        ArrayList<nars.tuprolog.Var> v2 = new ArrayList();
        long now = System.currentTimeMillis();
        return existCompatibleClause(v1, v2, now);
    }


    /**
     * Salva le unificazioni delle variabili da deunificare
     *
     * @param varsToDeunify
     * @return unificazioni delle variabili
     */
    private List<Term> deunify(List<Var> varsToDeunify) {
        List<Term> saveUnifications = new ArrayList<>(varsToDeunify.size());
        for (Var v : varsToDeunify) {
            saveUnifications.add(v.getLink());
            v.free();
        }
        return saveUnifications;
    }


    /**
     * Restore previous unifications into variables.
     *
     * @param varsToReunify
     * @param saveUnifications
     */
    private void reunify(List<Var> varsToReunify, List<Term> saveUnifications) {
        int size = varsToReunify.size();
        //TODO dont use ListIterator instances here: just a simple for loop on an ArrayList
        ListIterator<Var> it1 = varsToReunify.listIterator(size);
        ListIterator<Term> it2 = saveUnifications.listIterator(size);
        // Only the first occurrence of a variable gets its binding saved;
        // following occurrences get a null instead. So, to avoid clashes
        // between those values, and avoid random variable deunification,
        // the reunification is made starting from the end of the list.
        while (it1.hasPrevious()) {
            it1.previous().setLink(it2.previous());
        }
    }


    /**
     * Verify if a clause exists that is compatible with goal.
     * As a side effect, clauses that are not compatible get
     * discarded from the currently examined family.
     *
     * @param goal
     */
    private boolean checkCompatibility(PTerm goal, ArrayList<Var> v1, ArrayList<Var> v2, long now) {
        OneWayList<Clause> cc = this.clauses;
        if (cc == null) return false;
        Clause clause;


        do {
            clause = cc.getHead();
            if (goal.match(clause.getHead(), now, v1, v2)) return true;
            cc = cc.getTail();
        } while (cc != null);
        return false;
    }


    public String toString() {
        return "clauses: " + clauses + '\n' +
                "goal: " + goal + '\n' +
                "vars: " + vars + '\n';
    }
    
    
    /*
     * Methods for spyListeners
     */

//    public List<Clause> getClauses() {
//        ArrayList<Clause> l = new ArrayList<>();
//        OneWayList<Clause> t = clauses;
//        while (t != null) {
//            l.add(t.getHead());
//            t = t.getTail();
//        }
//        return l;
//    }

    public PTerm getMatchGoal() {
        return goal;
    }

    public List<Var> getVarsForMatch() {
        return vars;
    }


}