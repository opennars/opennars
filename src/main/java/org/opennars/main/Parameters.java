/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.main;

import java.io.Serializable;

public class Parameters implements Serializable {
    /** what this value represents was originally equal to the termlink record length (10), but we may want to adjust it or make it scaled according to duration since it has more to do with time than # of records.  it can probably be increased several times larger since each item should remain in the recording queue for longer than 1 cycle */
    public int NOVELTY_HORIZON = 100000;

    /** Minimum expectation for a desire value to execute an operation.
     *  the range of "now" is [-DURATION, DURATION]; */
    public float DECISION_THRESHOLD = 0.51f;

    /** Size of ConceptBag and level amount */
    public int CONCEPT_BAG_SIZE = 10000;
    public int CONCEPT_BAG_LEVELS = 1000;
    
    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    
    public int DURATION = 5;

    /* ---------- logical parameters ---------- */
    /** Evidential Horizon, the amount of future evidence to be considered.
     * Must be >=1.0, usually 1 .. 2
     */
    public float HORIZON = 1;

    /** determines the internal precision used for TruthValue calculations.
     *  a value of 0.01 gives 100 truth value states between 0 and 1.0.
     *  other values may be used, for example, 0.02 for 50, 0.10 for 10, etc.
     *  Change at your own risk
     */
    public float TRUTH_EPSILON = 0.01f;

    public float BUDGET_EPSILON = 0.0001f;

    /* ---------- budget thresholds ---------- */
    /** The budget threshold rate for task to be accepted. */
    public float BUDGET_THRESHOLD = (float) 0.01;

    /* ---------- default input values ---------- */
    /** Default expectation for confirmation. */
    public float DEFAULT_CONFIRMATION_EXPECTATION = (float) 0.6;
    /** Default expectation for creation of concept. */
    public float DEFAULT_CREATION_EXPECTATION = (float) 0.66; //0.66
    /** Default expectation for creation of concept for goals. */
    public float DEFAULT_CREATION_EXPECTATION_GOAL = (float) 0.6; //0.66
    /** Default confidence of input judgment. */
    public float DEFAULT_JUDGMENT_CONFIDENCE = (float) 0.9;
    /** Default priority of input judgment */
    public float DEFAULT_JUDGMENT_PRIORITY = (float) 0.8;
    /** Default durability of input judgment */
    public float DEFAULT_JUDGMENT_DURABILITY = (float) 0.5; //was 0.8 in 1.5.5; 0.5 after
    /** Default priority of input question */
    public float DEFAULT_QUESTION_PRIORITY = (float) 0.9;
    /** Default durability of input question */
    public float DEFAULT_QUESTION_DURABILITY = (float) 0.9;


    /** Default confidence of input goal. */
    public float DEFAULT_GOAL_CONFIDENCE = (float) 0.9;
    /** Default priority of input judgment */
    public float DEFAULT_GOAL_PRIORITY = (float) 0.9;
    /** Default durability of input judgment */
    public float DEFAULT_GOAL_DURABILITY = (float) 0.9;
    /** Default priority of input question */
    public float DEFAULT_QUEST_PRIORITY = (float) 0.9;
    /** Default durability of input question */
    public float DEFAULT_QUEST_DURABILITY = (float) 0.9;


    /* ---------- space management ---------- */

    /** Level separation in LevelBag, one digit, for display (run-time adjustable) and management (fixed)
     */
    public float BAG_THRESHOLD = 1.0f;

    /** (see its use in budgetfunctions iterative forgetting) */
    public float FORGET_QUALITY_RELATIVE = 0.1f;

    public int REVISION_MAX_OCCURRENCE_DISTANCE = 10;

    /** Size of TaskLinkBag */
    public int TASK_LINK_BAG_SIZE = 100;  //was 200 in new experiment
    public int TASK_LINK_BAG_LEVELS = 10;
    /** Size of TermLinkBag */
    public int TERM_LINK_BAG_SIZE = 100;  //was 1000 in new experiment
    public int TERM_LINK_BAG_LEVELS = 10;
    /** Maximum TermLinks checked for novelty for each TaskLink in TermLinkBag */
    public int TERM_LINK_MAX_MATCHED = 10;
    /** Size of Novel Task Buffer */
    public int NOVEL_TASK_BAG_SIZE = 100;
    public int NOVEL_TASK_BAG_LEVELS = 10;
    /*  Size of derived sequence and input event bag */
    public int SEQUENCE_BAG_SIZE = 30;
    public int SEQUENCE_BAG_LEVELS = 10;
    /*  Size of remembered last operation tasks */
    public int OPERATION_BAG_SIZE = 10;
    public int OPERATION_BAG_LEVELS = 10;
    public int OPERATION_SAMPLES = 6; //should be at least 2 to not only consider last decision
    
    /** How fast events decay in confidence **/
    public double PROJECTION_DECAY = 0.1;

    /* ---------- avoiding repeated reasoning ---------- */
    /** Maximum length of the evidental base of the Stamp, a power of 2 */
    public int MAXIMUM_EVIDENTAL_BASE_LENGTH = 20000;

    /** Maximum length of Stamp, a power of 2 */
    //public int MAXIMUM_STAMP_LENGTH = 8;

    /** Maximum TermLinks used in reasoning for each Task in Concept */
    public int TERMLINK_MAX_REASONED = 3;


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
    public float reliance = 0.9f;



    /**
     * The rate of confidence decrease in mental operations Doubt and Hesitate
     * set to zero to disable this feature.
     */
    public float DISCOUNT_RATE = 0.5f;

    






    //RUNTIME PERFORMANCE (should not affect logic): ----------------------------------

    /**
     * max length of a Term name for which it can be storedally via String.intern().
     * set to zero to disable this feature.
     * The problem with indiscriminate use of intern() is that interned strings can not be garbage collected (i.e. permgen) - possible a memory leak if terms disappear.
     */
    //public int INTERNED_TERM_NAME_MAXLEN = 0;

    /**
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

    //temporary parameter for setting #threads to use, globally
    public boolean IMMEDIATE_ETERNALIZATION=true;


    // public int STM_SIZE = 1;
    public int SEQUENCE_BAG_ATTEMPTS = 10; //5 //20
    public int CONDITION_BAG_ATTEMPTS = 10; //5 //20

    public float DERIVATION_PRIORITY_LEAK = 0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs

    public float DERIVATION_DURABILITY_LEAK = 0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs

    public float CURIOSITY_BUSINESS_THRESHOLD=0.18f; //dont be curious if business is above
    public float CURIOSITY_PRIORITY_THRESHOLD=0.3f; //0.3f in 1.6.3
    public float CURIOSITY_CONFIDENCE_THRESHOLD=0.8f;
    public float CURIOSITY_DESIRE_CONFIDENCE_MUL=0.1f; //how much risk is the system allowed to take just to fullfill its hunger for knowledge?
    public float CURIOSITY_DESIRE_PRIORITY_MUL=0.1f; //how much priority should curiosity have?
    public float CURIOSITY_DESIRE_DURABILITY_MUL=0.3f; //how much durability should curiosity have?
    public boolean CURIOSITY_FOR_OPERATOR_ONLY=false; //for Peis concern that it may be overkill to allow it for all <a =/> b> statement, so that a has to be an operator
    public boolean CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF=false;

    public float HAPPY_EVENT_HIGHER_THRESHOLD=0.75f;
    public float HAPPY_EVENT_LOWER_THRESHOLD=0.25f;
    public float BUSY_EVENT_HIGHER_THRESHOLD=0.9f; //1.6.4, step by step^, there is already enough new things ^^
    public float BUSY_EVENT_LOWER_THRESHOLD=0.1f;

    public boolean CONSIDER_REMIND=false;
    public boolean BREAK_NAL_HOL_BOUNDARY=false;

    public boolean QUESTION_GENERATION_ON_DECISION_MAKING=false;
    public boolean HOW_QUESTION_GENERATION_ON_DECISION_MAKING=false;

    public float ANTICIPATION_CONFIDENCE = 0.90f;

    public float ANTICIPATION_TOLERANCE = 50.0f;
    
    public float CONSIDER_NEW_OPERATION_BIAS = 0.05f; //depriorizes older operation-related events in temporal inference
    
    public float TEMPORAL_INDUCTION_PRIORITY_PENALTY = 1.0f; //was 0.1
    
    public int AUTOMATIC_DECISION_USUAL_DECISION_BLOCK_CYCLES = 500;
    
    public float SATISFACTION_TRESHOLD = 0.0f; //decision threshold is enough for now
    
    public float COMPLEXITY_UNIT=1.0f; //1.0 - oo
    
    public float INTERVAL_ADAPT_SPEED = 4.0f;
 
    public int TASKLINK_PER_CONTENT = 4; //eternal/event are also seen extra
    
    /** Default priority of exection feedback */
    public float DEFAULT_FEEDBACK_PRIORITY = (float) 0.9;
    /** Default durability of exection feedback */
    public float DEFAULT_FEEDBACK_DURABILITY = (float) 0.5; //was 0.8 in 1.5.5; 0.5 after

    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.*/
    public float CONCEPT_FORGET_DURATIONS = 2.0f;

    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public float TERMLINK_FORGET_DURATIONS = 10.0f;

    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public float TASKLINK_FORGET_DURATIONS = 4.0f;

    /** Sequence bag forget durations **/
    public float EVENT_FORGET_DURATIONS = 4.0f;
}
