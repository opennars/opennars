/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic.reason;

import nars.core.Events;
import nars.core.Parameters;
import nars.logic.NAL;
import nars.logic.entity.*;

/** Firing a concept (reasoning event)  */
abstract public class ConceptFire extends NAL {

    protected final Term currentTerm;
    protected final TaskLink currentTaskLink;
    protected final Concept currentConcept;

    protected TermLink currentBeliefLink;
    private Concept currentBeliefConcept;
    private int termLinkCount;


    public ConceptFire(Concept concept, TaskLink taskLink) {
        this(concept, taskLink, concept.memory.param.termLinkMaxReasoned.get());
    }

    public ConceptFire(Concept concept, TaskLink taskLink, int termLinkCount) {
        super(concept.memory, taskLink.getTask());
        this.currentTaskLink = taskLink;
        this.currentConcept = concept;
        this.currentTerm = concept.getTerm();

        this.termLinkCount = termLinkCount;
    }



    /**
     * @return the currentConcept
     */
    public Concept getCurrentConcept() {
        return currentConcept;
    }


    abstract protected void beforeFinish();

    @Override
    protected void onFinished() {
        beforeFinish();

        currentConcept.termLinks.processNext(
                memory.param.termLinkForgetDurations,
                Parameters.TERMLINK_FORGETTING_ACCURACY,
                memory);

        currentConcept.taskLinks.processNext(
                memory.param.taskLinkForgetDurations,
                Parameters.TASKLINK_FORGETTING_ACCURACY,
                memory);

        /*
        System.err.println(this);
        for (Task t : tasksAdded) {
            System.err.println("  " + t);
        }
        System.err.println();
        */


        if (newTasks!=null && !newTasks.isEmpty())
            memory.addNewTasks(this);
    }


    @Override
    protected void reason() {

        setCurrentBeliefLink(null);

        reasoner.fire(this);

        if (currentTaskLink.type != TermLink.TRANSFORM) {

            int noveltyHorizon = memory.param.noveltyHorizon.get();

            int termLinkSelectionAttempts = termLinkCount;

            //TODO early termination condition of this loop when (# of termlinks) - (# of non-novel) <= 0
            //int numTermLinks = getCurrentConcept().termLinks.size();

            while (termLinkSelectionAttempts > 0)  {


                final TermLink beliefLink = currentConcept.selectNovelTermLink(currentTaskLink, memory.time(), noveltyHorizon);



                if (beliefLink == null) {
                    //no novel termlinks available
                    break;
                }

                termLinkSelectionAttempts--;

                int numAddedTasksBefore = newTasksCount();

                reason(currentTaskLink, beliefLink);

                int numAddedTasksAfter = newTasksCount();

                //TODO redudant to send both 'this' and currentTaskLink, etc.., so remove them
                emit(Events.TermLinkSelected.class, beliefLink, getCurrentTaskLink(), getCurrentConcept(), this, numAddedTasksBefore, numAddedTasksAfter);
                memory.logic.TERM_LINK_SELECT.hit();


            }
        }
                
        emit(Events.ConceptFired.class, this);
        memory.logic.TASKLINK_FIRE.hit();

    }



    /**
     * Entry point of the logic engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     */
    protected void reason(final TaskLink tLink, final TermLink bLink) {

        setCurrentBeliefLink(bLink);

        Sentence belief = getCurrentBelief();

        reasoner.fire(this);

        if (belief!=null) {
            emit(Events.BeliefReason.class, belief, tLink.getTarget(), this);
        }

    }

    /**
     * @return the currentBeliefLink
     */
    public TermLink getCurrentBeliefLink() {
        return currentBeliefLink;
    }

    /**
     * @param currentBeliefLink the currentBeliefLink to set
     */
    public void setCurrentBeliefLink(TermLink currentBeliefLink) {

        if (this.currentBeliefLink == currentBeliefLink) {

            if (currentBeliefLink != null)
                throw new RuntimeException("Setting the same current belief link");

            return;
        }

        if (currentBeliefLink == null) {
            this.currentBelief = null;
            this.currentBeliefLink = null;
            this.currentBeliefConcept = null;
            return;
        }

        this.currentBeliefLink = currentBeliefLink;

        Term beliefTerm = currentBeliefLink.getTerm();

        this.currentBeliefConcept = memory.concept(beliefTerm);

        this.currentBelief = (currentBeliefConcept != null) ? currentBeliefConcept.getBelief(this, getCurrentTask()) : null;
    }

    /**
     * @return the currentTerm
     */
    public Term getCurrentTerm() {
        return currentTerm;
    }

    /**
     * @return the currentTaskLink
     */
    public TaskLink getCurrentTaskLink() {
        return currentTaskLink;
    }





    /** the current belief concept */
    public Concept getCurrentBeliefConcept() {
        return currentBeliefConcept;
    }



    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + currentConcept + ',' + currentTaskLink + ']';
    }


}
