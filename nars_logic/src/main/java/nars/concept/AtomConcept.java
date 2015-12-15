package nars.concept;

import nars.NAR;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.concept.util.BeliefTable;
import nars.concept.util.TaskTable;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 9/2/15.
 */
public class AtomConcept extends AbstractConcept  {

    protected final Bag<Task> taskLinks;
    protected final Bag<Term> termLinks;


//    /** creates with no termlink and tasklink ability */
//    public AtomConcept(Term atom, Budget budget) {
//        this(atom, budget, new NullBag(), new NullBag());
//    }

    public AtomConcept(Term atom, Bag<Term> termLinks, Bag<Task> taskLinks) {
        super(atom);
        this.termLinks = termLinks;
        this.taskLinks = taskLinks;
    }



    /**
     * Task links for indirect processing
     */
    @Override
    public final Bag<Task> getTaskLinks() {
        return taskLinks;
    }

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    @Override
    public final Bag<Term> getTermLinks() {
        return termLinks;
    }


    @Override
    public BeliefTable getBeliefs() {
        return BeliefTable.EMPTY;
    }

    @Override
    public BeliefTable getGoals() {
        return BeliefTable.EMPTY;
    }

    @Override
    public TaskTable getQuestions() {
        return BeliefTable.EMPTY;
    }

    @Override
    public TaskTable getQuests() {
        return BeliefTable.EMPTY;
    }

    static final String shouldntProcess = "should not have attempted to process task here";

    @Override
    public boolean processBelief(Task task, NAR nar) {
        throw new RuntimeException(shouldntProcess);
    }
    @Override
    public boolean processGoal(Task task, NAR nar) {
        throw new RuntimeException(shouldntProcess);
    }
    @Override
    public boolean processQuestion(Task task, NAR nar) {
        throw new RuntimeException(shouldntProcess);
    }
    @Override
    public final boolean processQuest(Task task, NAR nar) {
        return processQuestion(task, nar );
    }


    /** atoms have no termlink templates, they are irreducible */
    @Override public final Term[] getTermLinkTemplates() {
        return null;
    }

    @Override
    public boolean link(Term t, Budget b, float scale, NAR nar) {
        return false;
    }

}
