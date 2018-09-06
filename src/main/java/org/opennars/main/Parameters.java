/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.main;

import java.io.Serializable;

/**
 * Reasoner local settings and deriver options
 *
 * @author Patrick Hammer
 */
// TODO< rename this after MVP0 to "ReasonerArguments" >
public class Parameters implements Serializable {
    /** what this value represents was originally equal to the termlink record length (10), but we may want to adjust it or make it scaled according to duration since it has more to do with time than # of records.  it can probably be increased several times larger since each item should remain in the recording queue for longer than 1 cycle */
    public volatile int NOVELTY_HORIZON = 100000;

    /** Minimum expectation for a desire value to execute an operation.
     *  the range of "now" is [-DURATION, DURATION]; */
    public volatile float DECISION_THRESHOLD = 0.51f;

    /** Size of ConceptBag and level amount */
    //not changeable at runtime as bags would have to be re-constructed
    public int CONCEPT_BAG_SIZE = 10000;
    public int CONCEPT_BAG_LEVELS = 1000;
    
    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    
    public volatile int DURATION = 5;

    /* ---------- logical parameters ---------- */
    /** Evidential Horizon, the amount of future evidence to be considered.
     * Must be &gt;=1.0, usually 1 .. 2, not changeable at runtime as evidence measurement would change
     */
    public float HORIZON = 1;

    /** determines the internal precision used for TruthValue calculations.
     *  a value of 0.01 gives 100 truth value states between 0 and 1.0.
     *  other values may be used, for example, 0.02 for 50, 0.10 for 10, etc.
     *  Change at your own risk, but can't be changed at runtime
     */
    public float TRUTH_EPSILON = 0.01f;

    public float BUDGET_EPSILON = 0.0001f;

    /* ---------- budget thresholds ---------- */
    /** The budget threshold rate for task to be accepted. */
    public volatile float BUDGET_THRESHOLD = (float) 0.01;

    /* ---------- default input values ---------- */
    /** Default expectation for confirmation on anticipation. */
    public volatile float DEFAULT_CONFIRMATION_EXPECTATION = (float) 0.6;
    /** Ignore expectation for creation of concept. */
    public volatile boolean ALWAYS_CREATE_CONCEPT = true;
    /** Default expectation for creation of concept. */
    public volatile float DEFAULT_CREATION_EXPECTATION = (float) 0.66; //0.66
    /** Default expectation for creation of concept for goals. */
    public volatile float DEFAULT_CREATION_EXPECTATION_GOAL = (float) 0.6; //0.66
    /** Default confidence of input judgment. */
    public volatile float DEFAULT_JUDGMENT_CONFIDENCE = (float) 0.9;
    /** Default priority of input judgment */
    public volatile float DEFAULT_JUDGMENT_PRIORITY = (float) 0.8;
    /** Default durability of input judgment */
    public volatile float DEFAULT_JUDGMENT_DURABILITY = (float) 0.5; //was 0.8 in 1.5.5; 0.5 after
    /** Default priority of input question */
    public volatile float DEFAULT_QUESTION_PRIORITY = (float) 0.9;
    /** Default durability of input question */
    public volatile float DEFAULT_QUESTION_DURABILITY = (float) 0.9;


    /** Default confidence of input goal. */
    public volatile float DEFAULT_GOAL_CONFIDENCE = (float) 0.9;
    /** Default priority of input judgment */
    public volatile float DEFAULT_GOAL_PRIORITY = (float) 0.9;
    /** Default durability of input judgment */
    public volatile float DEFAULT_GOAL_DURABILITY = (float) 0.9;
    /** Default priority of input question */
    public volatile float DEFAULT_QUEST_PRIORITY = (float) 0.9;
    /** Default durability of input question */
    public volatile float DEFAULT_QUEST_DURABILITY = (float) 0.9;


    /* ---------- space management ---------- */

    /** Level separation in LevelBag, one digit
     */
    public float BAG_THRESHOLD = 1.0f;

    /** (see its use in budgetfunctions iterative forgetting) */
    public volatile float QUALITY_RESCALED = 0.1f;

    public volatile int REVISION_MAX_OCCURRENCE_DISTANCE = 10;

    /** Size of TaskLinkBag */
    public int TASK_LINK_BAG_SIZE = 100;  //was 200 in new experiment
    public int TASK_LINK_BAG_LEVELS = 10;
    /** Size of TermLinkBag */
    public int TERM_LINK_BAG_SIZE = 100;  //was 1000 in new experiment
    public int TERM_LINK_BAG_LEVELS = 10;
    /** Maximum TermLinks checked for novelty for each TaskLink in TermLinkBag */
    public volatile int TERM_LINK_MAX_MATCHED = 10;
    /** Size of Novel Task Buffer */
    public int NOVEL_TASK_BAG_SIZE = 100;
    public int NOVEL_TASK_BAG_LEVELS = 10;
    /**  Size of derived sequence and input event bag */
    public int SEQUENCE_BAG_SIZE = 30;
    public int SEQUENCE_BAG_LEVELS = 10;
    /**  Size of remembered last operation tasks */
    public int OPERATION_BAG_SIZE = 10;
    public int OPERATION_BAG_LEVELS = 10;
    public volatile int OPERATION_SAMPLES = 6; //should be at least 2 to not only consider last decision
    
    /** How fast events decay in confidence **/
    public volatile double PROJECTION_DECAY = 0.1;

    /* ---------- avoiding repeated reasoning ---------- */
    /** Maximum length of the evidental base of the Stamp, a power of 2 */
    public int MAXIMUM_EVIDENTAL_BASE_LENGTH = 20000;

    /** Maximum length of Stamp, a power of 2 */
    //public int MAXIMUM_STAMP_LENGTH = 8;

    /** Maximum TermLinks used in reasoning for each Task in Concept */
    public volatile int TERMLINK_MAX_REASONED = 3;


    /** Record-length for newly created TermLink's */
    public int TERM_LINK_RECORD_LENGTH =10;

    /** Maximum number of beliefs kept in a Concept */
    public int CONCEPT_BELIEFS_MAX = 28; //was 7

    /** Maximum number of questions kept in a Concept */
    public int CONCEPT_QUESTIONS_MAX = 5;

    /** Maximum number of goals kept in a Concept */
    public int CONCEPT_GOALS_MAX = 7;

    /** Reliance factor, the empirical confidence of analytical truth.
     the same as default confidence  */
    public volatile float reliance = 0.9f;



    /**
     * The rate of confidence decrease in mental operations Doubt and Hesitate
     * set to zero to disable this feature.
     */
    public volatile float DISCOUNT_RATE = 0.5f;

    






    //RUNTIME PERFORMANCE (should not affect logic): ----------------------------------

    /*
     * max length of a Term name for which it can be storedally via String.intern().
     * set to zero to disable this feature.
     * The problem with indiscriminate use of intern() is that interned strings can not be garbage collected (i.e. permgen) - possible a memory leak if terms disappear.
     */
    //public int INTERNED_TERM_NAME_MAXLEN = 0;

    /*
     * Determines when TermLink and TaskLink should use Rope implementation for its Key,
     * rather than String/StringBuilder.
     *
     * Set to -1 to disable the Rope entirely, 0 to use always, or a larger number as a threshold
     * below which uses contiguous char[] implementation, and above which uses
     * FastConcatenationRope.
     *
     * While a Rope is potentially more memory efficient (because it can re-use String instances
     * in its components without a redundant copy being stored) it can be more
     * computationally costly than a character array.
     *
     * The value needs to be weighed against the overhead of the comparison and iteration costs.
     *
     * Optimal value to be determined.
     */

    /** whether eternalization should happen on every derivation */
    public volatile boolean IMMEDIATE_ETERNALIZATION=true;


    // public int STM_SIZE = 1;
    public volatile int SEQUENCE_BAG_ATTEMPTS = 10; //5 //20
    public volatile int CONDITION_BAG_ATTEMPTS = 10; //5 //20

    public volatile float DERIVATION_PRIORITY_LEAK = 0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs

    public volatile float DERIVATION_DURABILITY_LEAK = 0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs

    /** how much risk is the system allowed to take just to fullfill its hunger for knowledge? */
    public volatile float CURIOSITY_DESIRE_CONFIDENCE_MUL=0.1f;

    /** how much priority should curiosity have? */
    public volatile float CURIOSITY_DESIRE_PRIORITY_MUL=0.1f;

    /** how much durability should curiosity have? */
    public volatile float CURIOSITY_DESIRE_DURABILITY_MUL=0.3f;


    public volatile boolean CURIOSITY_FOR_OPERATOR_ONLY=false; //for Peis concern that it may be overkill to allow it for all &lt;a =/&gt; b&gt; statement, so that a has to be an operator

    public volatile boolean BREAK_NAL_HOL_BOUNDARY=false;

    public volatile boolean QUESTION_GENERATION_ON_DECISION_MAKING=false;

    public volatile boolean HOW_QUESTION_GENERATION_ON_DECISION_MAKING=false;

    /** eternalized induction confidence to revise A =/&gt; B beliefs */
    public volatile float ANTICIPATION_CONFIDENCE = 0.33f;

    public volatile float ANTICIPATION_TOLERANCE = 50.0f;
    
    public volatile float SATISFACTION_TRESHOLD = 0.0f; //decision threshold is enough for now
    
    public volatile float COMPLEXITY_UNIT=1.0f; //1.0 - oo
    
    public volatile float INTERVAL_ADAPT_SPEED = 4.0f;
 
    public int TASKLINK_PER_CONTENT = 4; //eternal/event are also seen extra
    
    /** Default priority of execution feedback */
    public volatile float DEFAULT_FEEDBACK_PRIORITY = (float) 0.9;
    /** Default durability of execution feedback */
    public volatile float DEFAULT_FEEDBACK_DURABILITY = (float) 0.5; //was 0.8 in 1.5.5; 0.5 after

    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.*/
    public volatile float CONCEPT_FORGET_DURATIONS = 2.0f;

    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public volatile float TERMLINK_FORGET_DURATIONS = 10.0f;

    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public volatile float TASKLINK_FORGET_DURATIONS = 4.0f;

    /** Sequence bag forget durations */
    public volatile float EVENT_FORGET_DURATIONS = 4.0f;
    
    /** Maximum attempted combinations in variable introduction.*/
    public volatile int VARIABLE_INTRODUCTION_COMBINATIONS_MAX = 8;
    
    /** Maximum anticipations about its content stored in a concept */
    public volatile int ANTICIPATIONS_PER_CONCEPT_MAX = 8;
    
    /** Default threads amount at startup */
    public volatile int THREADS_AMOUNT = 1;
    
    /** Default volume at startup */
    public volatile int VOLUME = 0;
    
    /** Default miliseconds per step at startup */
    public volatile int MILLISECONDS_PER_STEP = 0;
    
    /** Timing mode, steps or real time */
    public volatile boolean STEPS_CLOCK = true;
}
