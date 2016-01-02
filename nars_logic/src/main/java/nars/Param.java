package nars;

import com.google.common.util.concurrent.AtomicDouble;
import nars.nal.Level;
import nars.util.data.MutableInteger;
import objenome.Container;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * NAR Parameters which can be changed during runtime.
 */
public abstract class Param extends Container implements Level {


    public final MutableInteger cyclesPerFrame = new MutableInteger(1);


    public Param() {    }

//    /** Silent threshold for task reporting, in [0, 100].
//     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
//     */
//    public final AtomicInteger outputVolume = new AtomicInteger();

    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    public final MutableInteger duration = new MutableInteger();

    public final MutableInteger shortTermMemoryHistory = new MutableInteger();



    /** converts durations to cycles */
    public final float durationToCycles(MutableFloat durations) {
        return durationToCycles(durations.floatValue());
    }

    public final float durationToCycles(float durations) {
        return duration.floatValue() * durations;
    }







    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE 
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.
     */
    @Deprecated public final MutableFloat conceptForgetDurations = new MutableFloat();
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    //TODO use separate termlink forget rates whether the termlink was actually selected for firing or not.
    @Deprecated public final MutableFloat termLinkForgetDurations = new MutableFloat();
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    @Deprecated public final MutableFloat taskLinkForgetDurations = new MutableFloat();
    

     /*
     BUDGET THRESHOLDS
     * Increasing this value decreases the resolution with which
     *   budgets are propagated or otherwise measured, which can result
     *   in a performance gain.      */


//    /** budget summary necessary to Conceptualize. this will compare the summary of the task during the TaskProcess */
//    public final AtomicDouble newConceptThreshold = new AtomicDouble(0);

    /** budget summary necessary to create a derived task. this will compare the summary of the raw original derivation */
    public final AtomicDouble derivationThreshold = new AtomicDouble(0);


//    /** budget summary necessary to execute a desired Goal */
//    public final AtomicDouble questionFromGoalThreshold = new AtomicDouble(0);

    /** budget summary necessary to run a TaskProcess for a given Task
     *  this should be equal to zero to allow subconcept seeding. */
    public final AtomicDouble taskProcessThreshold = new AtomicDouble(0);

    /** budget summary necessary to propagte tasklink activation */
    public final AtomicDouble taskLinkThreshold = new AtomicDouble(0);

    /** budget summary necessary to propagte termlink activation */
    public final AtomicDouble termLinkThreshold = new AtomicDouble(0);

    /** Minimum expectation for a desire value.
     *  the range of "now" is [-DURATION, DURATION]; */
    public final AtomicDouble executionExpectationThreshold = new AtomicDouble();




    /** Maximum number of beliefs kept in a Concept */
    public final AtomicInteger conceptBeliefsMax = new AtomicInteger();
    
    /** Maximum number of questions, and max # of quests kept in a Concept */
    public final AtomicInteger conceptQuestionsMax = new AtomicInteger();

    /** Maximum number of goals kept in a Concept */
    public final AtomicInteger conceptGoalsMax = new AtomicInteger();
    

}
