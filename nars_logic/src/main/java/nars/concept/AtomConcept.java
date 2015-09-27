package nars.concept;

import infinispan.com.google.common.collect.Iterators;
import javolution.util.function.Equality;
import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.link.*;
import nars.premise.Premise;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;

import java.util.Iterator;
import java.util.List;

/**
 * Created by me on 9/2/15.
 */
public class AtomConcept extends AbstractConcept {


    protected final Bag<Sentence, TaskLink> taskLinks;
    protected final Bag<TermLinkKey, TermLink> termLinks;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    protected transient final TermLinkBuilder termLinkBuilder;
    protected transient final TaskLinkBuilder taskLinkBuilder;

    public AtomConcept(Term atom, Budget budget, final Bag<TermLinkKey, TermLink> termLinks, final Bag<Sentence, TaskLink> taskLinks, Memory memory) {
        super(atom, budget, memory);
        this.termLinks = termLinks;
        this.taskLinks = taskLinks;
        this.taskLinkBuilder = new TaskLinkBuilder(memory);
        this.termLinkBuilder = new TermLinkBuilder(this);
    }



    /**
     * Task links for indirect processing
     */
    public Bag<Sentence, TaskLink> getTaskLinks() {
        return taskLinks;
    }

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    public Bag<TermLinkKey, TermLink> getTermLinks() {
        return termLinks;
    }



    @Override
    public TaskLinkBuilder getTaskLinkBuilder() {
        return taskLinkBuilder;
    }



    @Override
    public TermLinkBuilder getTermLinkBuilder() {
        return termLinkBuilder;
    }


    static final BeliefTable emptyTable = new BeliefTable() {

        @Override
        final public Iterator<Task> iterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public void setCapacity(int newCapacity) {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void clear() {

        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Task add(Task q, Equality<Task> e, Concept c) {
            return null;
        }

        @Override
        public Task add(Task input, Ranker r, Concept c, Premise nal) {
            return null;
        }

        @Override
        public Ranker getRank() {
            return null;
        }

        @Override
        public Task top(boolean eternal, boolean temporal) {
            return null;
        }
    };

    @Override
    public BeliefTable getBeliefs() {
        return emptyTable;
    }

    @Override
    public BeliefTable getGoals() {
        return emptyTable;
    }

    @Override
    public TaskTable getQuestions() {
        return emptyTable;
    }

    @Override
    public TaskTable getQuests() {
        return emptyTable;
    }

    @Override
    public boolean processBelief(Premise nal, Task task) {
        return false;
    }

    @Override
    public boolean processGoal(Premise nal, Task task) {
        return false;
    }

    @Override
    public Task processQuestion(Premise nal, Task task) {
        return null;
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
    public TermLink activateTermLink(final TermLinkBuilder termLink) {
        return getTermLinks().update(termLink);
    }

    public List<TermLinkTemplate> getTermLinkTemplates() {
        return getTermLinkBuilder().templates();
    }


}
