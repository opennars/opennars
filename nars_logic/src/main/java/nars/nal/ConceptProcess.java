/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal;

import nars.Events;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;

/** Firing a concept (reasoning event). Derives new Tasks via reasoning rules
 *
 *  Concept
 *     Task
 *     TermLinks
 *
 * */
public class ConceptProcess extends NAL {

    protected final TaskLink currentTaskLink;
    protected final Concept currentConcept;

    protected TermLink currentTermLink;


    //essentially a cache for a concept lookup
    private transient Concept currentTermLinkConcept;


    private int termLinksToFire;
    private int termlinkMatches;


    public ConceptProcess(Concept concept, TaskLink taskLink) {
        this(concept, taskLink, concept.memory.param.termLinkMaxReasoned.get());
    }

    public ConceptProcess(Concept concept, TaskLink taskLink, int termLinkCount) {
        super(concept.memory, taskLink.getTask());


        this.currentTaskLink = taskLink;
        this.currentConcept = concept;
        this.termLinksToFire = termLinkCount;


    }



    /**
     * @return the currentConcept
     */
    public Concept getCurrentConcept() {
        return currentConcept;
    }


    protected void beforeFinish() {

    }

    @Override
    protected void onFinished() {
        beforeFinish();


        emit(Events.ConceptProcessed.class, this);
        memory.logic.TASKLINK_FIRE.hit();

    }


    protected void processTask() {

        currentTaskLink.setUsed(memory.time());

        setCurrentTermLink(null);
        reasoner.fire(this);
    }

    protected void processTerms() {

        final int noveltyHorizon = memory.param.noveltyHorizon.get();

        int termLinkSelectionAttempts = termLinksToFire;

        //TODO early termination condition of this loop when (# of termlinks) - (# of non-novel) <= 0
        int numTermLinks = getCurrentConcept().termLinks.size();

        currentConcept.updateTermLinks();

        int termLinksSelected = 0;
        while (termLinkSelectionAttempts-- > 0) {


            final TermLink bLink = nextTermLink(currentTaskLink, memory.time(), noveltyHorizon);
            if (bLink != null) {
                //novel termlink available

                processTerm(bLink);

                termLinksSelected++;


                emit(Events.TermLinkSelected.class, bLink, this);
                memory.logic.TERM_LINK_SELECT.hit();
            }

        }
        /*
        System.out.println(termLinksSelected + "/" + termLinksToFire + " took " +  termlinkMatches + " matches over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords());
        currentConcept.taskLinks.printAll(System.out);*/
    }

    final Concept.TermLinkNoveltyFilter termLinkNovel = new Concept.TermLinkNoveltyFilter();

    /**
     * Replace default to prevent repeated logic, by checking TaskLink
     *
     * @param taskLink The selected TaskLink
     * @param time The current time
     * @return The selected TermLink
     */
    TermLink nextTermLink(final TaskLink taskLink, final long time, int noveltyHorizon) {

        final int links = currentConcept.termLinks.size();
        if (links == 0) return null;

        int toMatch = memory.param.termLinkMaxMatched.get();

        //optimization case: if there is only one termlink, we will never get anything different from calling repeatedly
        if (links == 1) toMatch = 1;

        termLinkNovel.set(taskLink, time, noveltyHorizon, memory.param.termLinkRecordLength.get());

        for (int i = 0; (i < toMatch); i++) {

            final TermLink termLink = currentConcept.termLinks.forgetNext(memory.param.termLinkForgetDurations, memory);
            termlinkMatches++;

            if (termLink == null)
                return null;

            if (termLinkNovel.apply(termLink)) {
                return termLink;
            }

        }

        return null;
    }


    /**
     * Entry point of the logic engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     */
    protected void processTerm(TermLink bLink) {
        setCurrentTermLink(bLink);

        reasoner.fire(this);

        emit(Events.BeliefReason.class, getCurrentBelief(), getCurrentTask(), this);
    }

    @Override
    public void run() {
        if (!currentConcept.isActive()) return;

        super.run();
    }

    @Override
    protected void process() {

        currentConcept.setUsed(memory.time());

        processTask();

        if (currentTaskLink.type != TermLink.TRANSFORM) {

            processTerms();

        }
    }





    /**
     * @return the currentBeliefLink
     */
    public TermLink getCurrentTermLink() {
        return currentTermLink;
    }

    /**
     * @param currentTermLink the currentBeliefLink to set
     */
    public void setCurrentTermLink(TermLink currentTermLink) {

        if (currentTermLink == null) {
            this.currentTermLink = null;
            this.currentTermLinkConcept = null;
        }
        else {
            this.currentTermLink = currentTermLink;
            this.currentTermLinkConcept = null; //this will be fetched if requested, and cached until the termlink changes
            currentTermLink.setUsed(memory.time());
        }
    }


    /**
     * @return the currentTerm
     */
    public Term getCurrentTerm() {
        return currentConcept.getTerm();
    }

    /**
     * @return the currentTaskLink
     */
    public TaskLink getCurrentTaskLink() {
        return currentTaskLink;
    }





    /** the current belief concept */
    public Concept getCurrentTermLinkConcept() {
        if (currentTermLinkConcept == null && getCurrentTermLink()!=null) {
            currentTermLinkConcept = memory.concept( getCurrentTermLink().target );
        }
        return currentTermLinkConcept;
    }

    public float conceptPriority(Term target) {
        //first check for any cached Concept
        if (target == getCurrentTermLink().target) {
            Concept c = getCurrentTermLinkConcept();

            if (c == null) return 0; //if the concept does not exist, use priority = 0

            return c.getPriority();
        }
        return super.conceptPriority(target);
    }


    @Override
    public String toString() {
        return new StringBuilder()
        .append("ConceptProcess[").append(currentConcept.toString()).append(':').append(currentTaskLink).append(',').append(currentTermLink).append(']')
        .toString();
    }


}
