/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.core.Events;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.*;
import nars.logic.nal1.Negation;
import nars.logic.nal5.Equivalence;
import nars.logic.nal5.Implication;
import nars.logic.nal5.SyllogisticRules;
import nars.logic.nal7.TemporalRules;
import nars.logic.rule.concept.ConceptFireRule;

import java.util.Set;

import static nars.io.Symbols.VAR_INDEPENDENT;

/** Concept reasoning context - a concept is "fired" or activated by applying the reasoner */
abstract public class FireConcept extends NAL {


    private Concept beliefConcept;

    public FireConcept(Memory mem, Concept concept, int numTaskLinks) {
        this(mem, concept, numTaskLinks, mem.param.termLinkMaxReasoned.get());
    }
    
    public FireConcept(Memory mem, Concept concept, int numTaskLinks, int termLinkCount) {
        super(mem);
        setKey(FireConcept.class);

        this.termLinkCount = termLinkCount;
        this.currentConcept = concept;
        this.currentTaskLink = null;
        this.numTaskLinks = numTaskLinks;
    }

    private int numTaskLinks;
    private int termLinkCount;

    transient private Set alreadyInducted;


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


        memory.addNewTasks(this);
    }


    @Override
    protected void reason() {


        if (currentTaskLink != null) {

            //TODO move this case to a separate implementation of FireConcept which takes 1 or more specific concepts to fire as constructor arg
            fireTaskLink(termLinkCount);
            returnTaskLink(currentTaskLink);

        } else {

            if (currentConcept.taskLinks.size() == 0)
                return;

            for (int i = 0; i < numTaskLinks; i++) {

                currentTaskLink = currentConcept.taskLinks.TAKENEXT();
                if (currentTaskLink == null)
                    return;

                try {

                    //if (currentTaskLink.budget.aboveThreshold()) {
                        fireTaskLink(termLinkCount);
                    //}

                    returnTaskLink(currentTaskLink);
                }
                catch (Exception e) {

                    returnTaskLink(currentTaskLink);

                    if (Parameters.DEBUG) {
                        e.printStackTrace();
                    }
                    throw e;
                }

            }
        }


    }
    
    protected void returnTaskLink(TaskLink t) {
        currentConcept.taskLinks.putBack(t, 
                memory.param.cycles(memory.param.taskLinkForgetDurations), memory);
        
    }

    protected void fireTaskLink(int termLinkSelectionAttempts) {

        final Task task = currentTaskLink.getTarget();
        setCurrentTerm(currentConcept.term);
        setCurrentTaskLink(currentTaskLink);
        setCurrentBelief(null);
        setCurrentBeliefLink(null);
        setCurrentTask(task); // one of the two places where this variable is set
        
        if (currentTaskLink.type == TermLink.TRANSFORM) {

            RuleTables.transformTask(currentTaskLink, this); // to turn this into structural logic as below?

            emit(Events.TermLinkTransform.class, currentTaskLink, currentConcept, this);
            memory.logic.TERM_LINK_TRANSFORM.hit();

        } else {


            reason(currentTaskLink);


//            //EXPERIMENTAL:
//            //if termlinks is less than novelty horizon, it can suppress any from being selected for up to novelty horizon cycles
//            int noveltyHorizon = Math.min(Parameters.NOVELTY_HORIZON,
//                        1+currentConcept.termLinks.size()/termLinkSelectionAttempts);
//            termLinkSelectionAttempts = Math.min(termLinkSelectionAttempts, currentConcept.termLinks.size());
//            //------

            //if termlinks is less than novelty horizon, it can suppress any from being selected for up to novelty horizon cycles
            /*int noveltyHorizon = Math.min(Parameters.NOVELTY_HORIZON,
                    currentConcept.termLinks.size() - 1);*/
            int noveltyHorizon = Parameters.NOVELTY_HORIZON;

            //int numTermLinks = getCurrentConcept().termLinks.size();
            //TODO early termination condition of this loop when (# of termlinks) - (# of non-novel) <= 0

            while (termLinkSelectionAttempts > 0)  {


                final TermLink termLink = currentConcept.selectTermLink(currentTaskLink, memory.time(), noveltyHorizon);

                termLinkSelectionAttempts--;

                //try again, because it may have selected a non-novel tlink
                if (termLink == null)
                    continue;
                
                setCurrentBeliefLink(termLink);

                int numAddedTasksBefore = produced.size();



                reason(currentTaskLink, termLink);
                //afterReason
                {
                    setCurrentBelief(null);
                }


                currentConcept.returnTermLink(termLink, true);

                int numAddedTasksAfter = produced.size();

                emit(Events.TermLinkSelected.class, termLink, getCurrentTaskLink(), getCurrentConcept(), this, numAddedTasksBefore, numAddedTasksAfter);
                memory.logic.TERM_LINK_SELECT.hit();


            }
        }
                
        emit(Events.ConceptFired.class, this);
        memory.logic.TASKLINK_FIRE.hit();

    }

    /** reasoning processes involving only the task itself */
    protected void reason(Sentence.Sentenceable taskLink) {

        final Sentence taskSentence = taskLink.getSentence();

        final Term taskTerm = taskSentence.term;         // cloning for substitution

        if ((taskTerm instanceof Implication) && taskSentence.isJudgment()) {
            //there would only be one concept which has a term equal to another term... so samplingis totally unnecessary

            //Concept d=memory.concepts.sampleNextConcept();
            //if(d!=null && d.term.equals(taskSentence.term)) {

            double n=taskTerm.getComplexity(); //don't let this rule apply every time, make it dependent on complexity
            double w=1.0/((n*(n-1))/2.0); //let's assume hierachical tuple (triangle numbers) amount for this
            if(Memory.randomNumber.nextDouble() < w) { //so that NARS memory will not be spammed with contrapositions

                StructuralRules.contraposition((Statement) taskTerm, taskSentence, this);
                //}
            }
        }

    }


    /**
     * Entry point of the logic engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     */
    protected void reason(final TaskLink tLink, final TermLink bLink) {

        setCurrentTask(tLink.getTarget());
        setBelief(bLink.target);
        Sentence belief = getCurrentBelief();

        reasoner.add(this, tLink, bLink);

        if (belief!=null) {
            emit(Events.BeliefReason.class, belief, getCurrentBelief(), getCurrentTask().getTerm(), this);
        }



//        final Task task = tLink.getTarget(); //== getCurrentTask();
//        final Sentence taskSentence = task.sentence;
//
//        final Term taskTerm = taskSentence.term;         // cloning for substitution
//        final Term beliefTerm = bLink.target;       // cloning for substitution


//        if (equalSubTermsInRespectToImageAndProduct(taskTerm, beliefTerm)) {
//            throw new RuntimeException("shoulld have been suppressed by the new rule impl");
//        }
//
//        if ((belief != null) && (LocalRules.match(task, belief, this))) {
//            throw new RuntimeException("shoulld have been suppressed by the new rule impl");
//        }




//        //current belief and task may have changed, so set again:
//        setCurrentBelief(belief);
//        setCurrentTask(task);




    }

    protected Concept setBelief(Term beliefTerm) {
        final Concept beliefConcept = memory.concept(beliefTerm);

        this.beliefConcept = beliefConcept;

        Sentence belief = (beliefConcept != null) ? beliefConcept.getBelief(this, getCurrentTask()) : null;

        setCurrentBelief( belief );  // may be null

        return beliefConcept;
    }

    /** the current belief concept */
    public Concept getBeliefConcept() {
        return beliefConcept;
    }

    @Override
    public Sentence setCurrentBelief(Sentence currentBelief) {
        this.currentBelief = currentBelief;

        //reset beliefConcept since it doesnt correlate with the currentBelief
        /*if (beliefConcept!=null && !this.currentBelief.getTerm().equals(beliefConcept.getTerm()))
            beliefConcept = null;*/

        return currentBelief;
    }


    @Override
    public String toString() {
        return "FireConcept[" + currentConcept + "," + currentTaskLink + "]";
    }


    @Override
    public void setCurrentConcept(Concept currentConcept) {
        throw new RuntimeException("FireConcept involves one specific concept");
    }
}
