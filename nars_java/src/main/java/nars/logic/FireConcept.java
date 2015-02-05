/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.core.Events;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.*;
import nars.logic.nal1.LocalRules;
import nars.logic.nal1.Negation;
import nars.logic.nal5.Equivalence;
import nars.logic.nal5.Implication;
import nars.logic.nal5.SyllogisticRules;
import nars.logic.nal7.TemporalRules;

import java.util.Set;

import static nars.io.Symbols.VAR_INDEPENDENT;
import static nars.logic.Terms.equalSubTermsInRespectToImageAndProduct;

/** Concept reasoning context - a concept is "fired" or activated by applying the reasoner */
abstract public class FireConcept extends NAL {


    public FireConcept(Memory mem, Concept concept, int numTaskLinks) {
        this(mem, concept, numTaskLinks, mem.param.termLinkMaxReasoned.get());
    }
    
    public FireConcept(Memory mem, Concept concept, int numTaskLinks, int termLinkCount) {
        super(mem);
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

        inputTasksToMemory();
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
        setCurrentBeliefLink(null);
        setCurrentTask(task); // one of the two places where this variable is set
        
        if (currentTaskLink.type == TermLink.TRANSFORM) {
            setCurrentBelief(null);
            
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

                int numAddedTasksBefore = tasksAdded.size();

                reason(currentTaskLink, termLink);

                currentConcept.returnTermLink(termLink, true);

                int numAddedTasksAfter = tasksAdded.size();

                emit(Events.TermLinkSelected.class, termLink, getCurrentTaskLink(), getCurrentConcept(), this, numAddedTasksBefore, numAddedTasksAfter);
                memory.logic.TERM_LINK_SELECT.hit();


            }
        }
                
        emit(Events.ConceptFired.class, this);
        memory.logic.TASKLINK_FIRE.hit();

    }

    /** reasoning processes involving only the task itself */
    protected void reason(TaskLink taskLink) {

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


        final Memory memory = mem();


        final Task task = tLink.getTarget(); //== getCurrentTask();
        final Sentence taskSentence = task.sentence;

        final Term taskTerm = taskSentence.term;         // cloning for substitution


        final Term beliefTerm = bLink.target;       // cloning for substitution

        if(equalSubTermsInRespectToImageAndProduct(taskTerm,beliefTerm))
            return;

        //final Concept currentConcept = getCurrentConcept();
        final Concept beliefConcept = memory.concept(beliefTerm);

        Sentence belief = (beliefConcept != null) ? beliefConcept.getBelief(this, task) : null;

        setCurrentBelief( belief );  // may be null

        if (belief != null) {

            if (LocalRules.match(task, belief, this)) {
                return;
            }

            if (nal(7)) {
                //this is a new attempt/experiment to make nars effectively track temporal coherences
                if (beliefTerm instanceof Implication &&
                        (beliefTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {

                    final int chainSamples = Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES;

                    //prevent duplicate inductions
                    if (alreadyInducted == null)
                        alreadyInducted = Parameters.newHashSet(chainSamples);
                    else
                        alreadyInducted.clear();

                    for (int i = 0; i < chainSamples; i++) {

                        Concept next = memory.concepts.sampleNextConcept();
                        if (next == null) continue;

                        Term t = next.getTerm();

                        if ((t instanceof Implication) && (alreadyInducted.add(t))) {

                            Implication implication = (Implication) t;

                            if (!next.beliefs.isEmpty() && (implication.isForward() || implication.isConcurrent())) {

                                Sentence s = next.beliefs.get(0);

                                TemporalRules.temporalInductionChain(s, belief, this);
                                TemporalRules.temporalInductionChain(belief, s, this);

                            }
                        }
                    }
                }
            }

            emit(Events.BeliefReason.class, belief, beliefTerm, taskTerm, this);

        }



        // to be invoked by the corresponding links
        if (CompositionalRules.dedSecondLayerVariableUnification(task, this)) {
            //unification ocurred, done reasoning in this cycle if it's judgment
            if (taskSentence.isJudgment())
                return;
        }


        //current belief and task may have changed, so set again:
        setCurrentBelief(belief);
        setCurrentTask(task);

        /*if ((memory.getNewTaskCount() > 0) && taskSentence.isJudgment()) {
            return;
        }*/

        CompositionalRules.dedConjunctionByQuestion(taskSentence, belief, this);

        final short tIndex = tLink.getIndex(0);
        short bIndex = bLink.getIndex(0);
        switch (tLink.type) {          // dispatch first by TaskLink type
            case TermLink.SELF:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        RuleTables.compoundAndSelf((CompoundTerm) taskTerm, beliefTerm, true, bIndex, this);
                        break;
                    case TermLink.COMPOUND:
                        RuleTables.compoundAndSelf((CompoundTerm) beliefTerm, taskTerm, false, bIndex, this);
                        break;
                    case TermLink.COMPONENT_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Statement) {
                                SyllogisticRules.detachment(taskSentence, belief, bIndex, this);
                            }
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            SyllogisticRules.detachment(belief, taskSentence, bIndex, this);
                        }
                        break;
                    case TermLink.COMPONENT_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) taskTerm, bIndex, beliefTerm, tIndex, this);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication) && (beliefTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, tIndex, this);
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        RuleTables.compoundAndCompound((CompoundTerm) taskTerm, (CompoundTerm) beliefTerm, bIndex, this);
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        RuleTables.compoundAndStatement((CompoundTerm) taskTerm, tIndex, (Statement) beliefTerm, bIndex, beliefTerm, this);
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            if (beliefTerm instanceof Implication) {
                                Term[] u = new Term[] { beliefTerm, taskTerm };
                                if (Variables.unify(VAR_INDEPENDENT, ((Statement) beliefTerm).getSubject(), taskTerm, u)) {
                                    Sentence newBelief = belief.clone(u[0]);
                                    Sentence newTaskSentence = taskSentence.clone(u[1]);
                                    RuleTables.detachmentWithVar(newBelief, newTaskSentence, bIndex, this);
                                } else {
                                    SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, -1, this);
                                }

                            } else if (beliefTerm instanceof Equivalence) {
                                SyllogisticRules.conditionalAna((Equivalence) beliefTerm, bIndex, taskTerm, -1, this);
                            }
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND_STATEMENT:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        if (taskTerm instanceof Statement) {
                            RuleTables.componentAndStatement((CompoundTerm) getCurrentTerm(), bIndex, (Statement) taskTerm, tIndex, this);
                        }
                        break;
                    case TermLink.COMPOUND:
                        if (taskTerm instanceof Statement) {
                            RuleTables.compoundAndStatement((CompoundTerm) beliefTerm, bIndex, (Statement) taskTerm, tIndex, beliefTerm, this);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            RuleTables.syllogisms(tLink, bLink, taskTerm, beliefTerm, this);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            bIndex = bLink.getIndex(1);
                            if ((taskTerm instanceof Statement) && (beliefTerm instanceof Implication)) {

                                RuleTables.conditionalDedIndWithVar((Implication) beliefTerm, bIndex, (Statement) taskTerm, tIndex, this);
                            }
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND_CONDITION:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        if (belief != null) {
                            RuleTables.detachmentWithVar(taskSentence, belief, tIndex, this);
                        }
                        break;

                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Implication) // TODO maybe put instanceof test within conditionalDedIndWithVar()
                            {
                                Term subj = ((Statement) taskTerm).getSubject();
                                if (subj instanceof Negation) {
                                    if (taskSentence.isJudgment()) {
                                        RuleTables.componentAndStatement((CompoundTerm) subj, bIndex, (Statement) taskTerm, tIndex, this);
                                    } else {
                                        RuleTables.componentAndStatement((CompoundTerm) subj, tIndex, (Statement) beliefTerm, bIndex, this);
                                    }
                                } else {
                                    RuleTables.conditionalDedIndWithVar((Implication) taskTerm, tIndex, (Statement) beliefTerm, bIndex, this);
                                }
                            }
                            break;

                        }
                        break;
                }
        }

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
