/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.task.Task;

/** Firing a concept (reasoning event). Derives new Tasks via reasoning rules
 *
 *  Concept
 *     Task
 *     TermLinks
 *
 * */
public class ConceptProcess extends NAL  {

    protected final TaskLink taskLink;
    protected final Concept concept;
    private final int termlinksToReason;

    protected TermLink currentTermLink;


    private Task currentBelief;

    //essentially a cache for a concept lookup
    private transient Concept currentTermLinkConcept;



    private long now;

    public ConceptProcess(Memory memory, Concept concept, TaskLink taskLink, int termlinksFired) {
        super(memory);

        this.taskLink = taskLink;
        this.concept = concept;
        this.termlinksToReason = termlinksFired;
    }

    public ConceptProcess(Memory memory, Concept concept, TaskLink taskLink) {
        this(memory, concept, taskLink, memory.param.termLinkMaxReasoned.intValue());
    }



    @Override public Task getTask() {
        return getTaskLink().getTask();
    }


    /**
     * @return the current Concept
     */
    @Override
    public final Concept getConcept() {
        return concept;
    }


    protected void beforeFinish() {

    }

    @Override
    protected void afterDerive() {
        beforeFinish();

        if (derived!=null)
            memory.add(derived);
    }



    @Override
    public Task getBelief() {
        return currentBelief;
    }

    public void setBelief(Task nextBelief) {
        this.currentBelief = nextBelief;
    }



    protected void processTask() {
        setTermLink(null);
        getMemory().rules.fire(this);
    }

    protected void processTerms() {

        //TODO early termination condition of this loop when (# of termlinks) - (# of non-novel) <= 0
        int numTermLinks = getConcept().getTermLinks().size();
        if (numTermLinks == 0)
            return;




        concept.updateTermLinks();



        /** use the time since last cycle as a sort of estimate for how to divide this cycle into subcycles;
         * this isnt necessary for default mode but realtime mode and others may have
         * irregular or unpredictable clocks.
         */
        float cyclesSincePrevious = memory.timeSinceLastCycle();

        int termLinkSelectionAttempts = termlinksToReason;

        int termLinksSelected = 0;

        while (termLinkSelectionAttempts-- > 0) {


           final TermLink bLink = concept.nextTermLink(getTaskLink());

            if (bLink!=null) {

                if (Global.DEBUG_TERMLINK_SELECTED)
                    emit(Events.TermLinkSelected.class, bLink, this);

                processTerm(bLink);
                termLinksSelected++;
            }


        }


        /*if (termLinksSelected == 0) {
            System.err.println(now + ": " + currentConcept + ": " + termLinksSelected + "/" + termLinksToFire + " firings over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords() + " for TermLinks "
                    //+ currentConcept.getTermLinks().values()
            );
            //currentConcept.taskLinks.printAll(System.out);
        }*/
    }







    /**
     * Entry point of the logic engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     */
    protected void processTerm(TermLink bLink) {
        setTermLink(bLink);

        getMemory().rules.fire(this);

        emit(Events.BeliefReason.class, this);
    }

    @Override
    protected void beforeDerive() {
        memory.emotion.busy(this);
    }

    @Override
    protected void derive() {

        emit(Events.ConceptProcessed.class, this);
        memory.logic.TASKLINK_FIRE.hit();

        final long now = this.now = memory.time();

        concept.setUsed(now);
        taskLink.setUsed(now);

        final Bag<TermLinkKey, TermLink> tl = concept.getTermLinks();
        if (tl!=null)
            tl.setForgetNext(memory.param.termLinkForgetDurations, memory);

        processTask();

        if (taskLink.type != TermLink.TRANSFORM) {
            processTerms();
        }

        taskLink.setFired(now);
    }





    /**
     * @return the current termLink aka BeliefLink
     */
    @Override
    public TermLink getTermLink() {
        return currentTermLink;
    }

    /**
     * @param currentTermLink the currentBeliefLink to set
     */
    public void setTermLink(TermLink currentTermLink) {

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
     * @return the current TaskLink
     */
    @Override
    public TaskLink getTaskLink() {
        return taskLink;
    }





    /** the current termlink / belieflink's concept */
    public Concept getTermLinkConcept() {
        if (currentTermLinkConcept == null && getTermLink()!=null) {
            currentTermLinkConcept = memory.concept( getTermLink().getTarget() );
        }
        return currentTermLinkConcept;
    }

//    public float conceptPriority(Term target) {
////        //first check for any cached Concept
////        if (target == getTermLink().target) {
////            Concept c = getTermLinkConcept();
////
////            if (c == null) return 0; //if the concept does not exist, use priority = 0
////
////            return c.getPriority();
////        }
//        return super.conceptPriority(target);
//    }


    @Override
    public String toString() {
        return new StringBuilder()
        .append("ConceptProcess[").append(concept.toString()).append(':').append(taskLink).append(',').append(currentTermLink).append(']')
        .toString();
    }


}
