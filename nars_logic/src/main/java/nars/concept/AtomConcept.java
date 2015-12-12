package nars.concept;

import nars.Premise;
import nars.bag.Bag;
import nars.bag.NullBag;
import nars.budget.Budget;
import nars.concept.util.BeliefTable;
import nars.concept.util.TaskTable;
import nars.link.*;
import nars.task.Task;
import nars.term.Term;

import java.util.Collections;
import java.util.List;

/**
 * Created by me on 9/2/15.
 */
public class AtomConcept extends AbstractConcept  {

    protected final Bag<Task, TaskLink> taskLinks;
    protected final Bag<TermLinkKey, TermLink> termLinks;


    /** creates with no termlink and tasklink ability */
    public AtomConcept(Term atom, Budget budget) {
        this(atom, budget, new NullBag(), new NullBag());
    }

    public AtomConcept(Term atom, Budget budget, Bag<TermLinkKey, TermLink> termLinks, Bag<Task, TaskLink> taskLinks) {
        super(budget, atom);
        this.termLinks = termLinks;
        this.taskLinks = taskLinks;
    }



    /**
     * Task links for indirect processing
     */
    @Override
    public final Bag<Task, TaskLink> getTaskLinks() {
        return taskLinks;
    }

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    @Override
    public final Bag<TermLinkKey, TermLink> getTermLinks() {
        return termLinks;
    }

    @Override
    public TermLinkBuilder getTermLinkBuilder() {
        return null;
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
    public boolean processBelief(Premise nal) {
        throw new RuntimeException(shouldntProcess);
    }
    @Override
    public boolean processGoal(Premise nal) {
        throw new RuntimeException(shouldntProcess);
    }
    @Override
    public boolean processQuestion(Premise nal) {
        throw new RuntimeException(shouldntProcess);
    }
    @Override
    public final boolean processQuest(Premise nal) {
        return processQuestion(nal);
    }

    /**
     * Insert a new or activate an existing TermLink in the TermLink bag
     * via a caching TermLinkSelector which has been configured for the
     * target Concept and the current budget
     * <p>
     * called from buildTermLinks only
     * <p>
     * If the tlink already exists, the budgets will be merged
     *
     * @param termLink The termLink to be inserted
     * @return the termlink which was selected or updated
     */
    @Override
    public final TermLink activateTermLink(TermLinkBuilder termLink) {
        return getTermLinks().update(termLink);
    }

    @Override
    public final List<TermLinkTemplate> getTermLinkTemplates() {
        TermLinkBuilder b = getTermLinkBuilder();
        if (b!=null)
            return b.templates();
        return Collections.emptyList();
    }

}
