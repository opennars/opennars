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

import org.opennars.main.NAR.PortableDouble;


/**
 * NAR operating parameters.
 * All static values will be removed so that this is an entirely dynamic class.
 */
public class Parameters {
    
    public static boolean SHOW_REASONING_ERRORS=false; //currently false because the sentence constructor is the only one
                                                       //who creates them but is not doing it because of an error.
    
    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    public static int DURATION = 5;
    
    /** use this for advanced error checking, at the expense of lower performance.
        it is enabled for unit tests automatically regardless of the value here.    */
    public static boolean DEBUG = false;
    public static boolean TEST_RUNNING = false;

    /** for thorough bag debugging (slow) */
    public static boolean DEBUG_BAG = false;
    public static boolean DEBUG_INVALID_SENTENCES = true;

    //FIELDS BELOW ARE BEING CONVERTED TO DYNAMIC, NO MORE STATIC: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //
    //Pei comments: parameters will be separated into a dynamic group and a static group
    //              and the latter contains "personality parameters" that cannot be changed
    //              in the lifetime of the system, though different systems may take different
    //              values. For example, to change HORIZON dynamically will cause inconsistency 
    //              in evidence evaluation.
        


    /* ---------- logical parameters ---------- */
    /** Evidential Horizon, the amount of future evidence to be considered. 
     * Must be >=1.0, usually 1 .. 2
     */
    public static float HORIZON = 1;
    


    
    /** determines the internal precision used for TruthValue calculations.
     *  a value of 0.01 gives 100 truth value states between 0 and 1.0.
     *  other values may be used, for example, 0.02 for 50, 0.10 for 10, etc.
     *  Change at your own risk
     */
    public static final float TRUTH_EPSILON = 0.01f;
    public static final float TRUTH_PRECISION = 1.0f / TRUTH_EPSILON;
    public static float MAX_CONFIDENCE = 1.0f - TRUTH_EPSILON;

    public static final float BUDGET_EPSILON = 0.0001f;
    
    /* ---------- budget thresholds ---------- */
    /** The budget threshold rate for task to be accepted. */
    public static final float BUDGET_THRESHOLD = (float) 0.01;

    /* ---------- default input values ---------- */
    /** Default expectation for confirmation. */
    public static final float DEFAULT_CONFIRMATION_EXPECTATION = (float) 0.6;
    /** Default expectation for creation of concept. */
    public static final float DEFAULT_CREATION_EXPECTATION = (float) 0.66; //0.66
     /** Default expectation for creation of concept for goals. */
    public static final float DEFAULT_CREATION_EXPECTATION_GOAL = (float) 0.6; //0.66
    /** Default confidence of input judgment. */
    public static final float DEFAULT_JUDGMENT_CONFIDENCE = (float) 0.9;
    /** Default priority of input judgment */
    public static float DEFAULT_JUDGMENT_PRIORITY = (float) 0.8;
    /** Default durability of input judgment */
    public static float DEFAULT_JUDGMENT_DURABILITY = (float) 0.5; //was 0.8 in 1.5.5; 0.5 after
    /** Default priority of input question */
    public static final float DEFAULT_QUESTION_PRIORITY = (float) 0.9;
    /** Default durability of input question */
    public static final float DEFAULT_QUESTION_DURABILITY = (float) 0.9;

    
     /** Default confidence of input goal. */
     public static final float DEFAULT_GOAL_CONFIDENCE = (float) 0.9;
     /** Default priority of input judgment */
     public static final float DEFAULT_GOAL_PRIORITY = (float) 0.9;
     /** Default durability of input judgment */
     public static final float DEFAULT_GOAL_DURABILITY = (float) 0.9;
     /** Default priority of input question */
     public static final float DEFAULT_QUEST_PRIORITY = (float) 0.9;
     /** Default durability of input question */
     public static final float DEFAULT_QUEST_DURABILITY = (float) 0.9;
 
    
    /* ---------- space management ---------- */
    
    /** Level separation in LevelBag, one digit, for display (run-time adjustable) and management (fixed)
     */
    public static final float BAG_THRESHOLD = 1.0f;

    /** (see its use in budgetfunctions iterative forgetting) */
    public static float FORGET_QUALITY_RELATIVE = 0.1f;
    
    public static int REVISION_MAX_OCCURRENCE_DISTANCE = 10;

    /** Size of ConceptBag and level amount */
    public static final int CONCEPT_BAG_SIZE = 10000;
    public static final int CONCEPT_BAG_LEVELS = 1000;
    /** Size of TaskLinkBag */
    public static final int TASK_LINK_BAG_SIZE = 100;  //was 200 in new experiment
    public static final int TASK_LINK_BAG_LEVELS = 10;
    /** Size of TermLinkBag */
    public static final int TERM_LINK_BAG_SIZE = 100;  //was 1000 in new experiment
    public static final int TERM_LINK_BAG_LEVELS = 10;
    /** Maximum TermLinks checked for novelty for each TaskLink in TermLinkBag */
    public static final int TERM_LINK_MAX_MATCHED = 10;
    /** Size of Novel Task Buffer */
    public static final int NOVEL_TASK_BAG_SIZE = 100;
    public static final int NOVEL_TASK_BAG_LEVELS = 10;
    /*  Size of derived sequence and input event bag */
    public static int SEQUENCE_BAG_SIZE = 30;
    public static int SEQUENCE_BAG_LEVELS = 10;
    /*  Size of remembered last operation tasks */
    public static int OPERATION_BAG_SIZE = 10;
    public static final int OPERATION_BAG_LEVELS = 10;
    public static int OPERATION_SAMPLES = 6; //should be at least 2 to not only consider last decision
    
    /** How fast events decay in confidence **/
    public static final PortableDouble projectionDecay = new PortableDouble(0.1);
    
    /* ---------- avoiding repeated reasoning ---------- */
        /** Maximum length of the evidental base of the Stamp, a power of 2 */
    public static final int MAXIMUM_EVIDENTAL_BASE_LENGTH = 20000;
    
    /** Maximum length of Stamp, a power of 2 */
    //public static final int MAXIMUM_STAMP_LENGTH = 8;
    
    /** Maximum TermLinks used in reasoning for each Task in Concept */
    public static final int TERMLINK_MAX_REASONED = 3;
    
    
    /** Record-length for newly created TermLink's */
    public static final int TERM_LINK_RECORD_LENGTH =10;
    
    /** Maximum number of beliefs kept in a Concept */
    public static final int CONCEPT_BELIEFS_MAX = 28; //was 7
    
    /** Maximum number of questions kept in a Concept */
    public static final int CONCEPT_QUESTIONS_MAX = 5;

    /** Maximum number of goals kept in a Concept */
    public static final int CONCEPT_GOALS_MAX = 7;
    
    /** Reliance factor, the empirical confidence of analytical truth.
        the same as default confidence  */        
    public static final float reliance = 0.9f;



    /** what this value represents was originally equal to the termlink record length (10), but we may want to adjust it or make it scaled according to duration since it has more to do with time than # of records.  it can probably be increased several times larger since each item should remain in the recording queue for longer than 1 cycle */
    public static final int NOVELTY_HORIZON = 100000;

    /**
     * The rate of confidence decrease in mental operations Doubt and Hesitate
     * set to zero to disable this feature.
     */
    public static float DISCOUNT_RATE = 0.5f;    

    /** enables the parsing of functional input format for operation terms: function(a,b,...) */
    public static boolean FUNCTIONAL_OPERATIONAL_FORMAT = true;
    
    
    
    
    
    
    //RUNTIME PERFORMANCE (should not affect logic): ----------------------------------
    
    /**
     * max length of a Term name for which it can be stored statically via String.intern().
     * set to zero to disable this feature.
     * The problem with indiscriminate use of intern() is that interned strings can not be garbage collected (i.e. permgen) - possible a memory leak if terms disappear.
     */
    //public static int INTERNED_TERM_NAME_MAXLEN = 0;
          
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
    
    /** max number of interval to combine in sequence to approximate a time period (cycles) */
    public static int TEMPORAL_INTERVAL_PRECISION = 1;
    

    
    /** equivalency based on Term contents; experimental mode - not ready yet, leave FALSE */
    public static boolean TERM_ELEMENT_EQUIVALENCY = false;
    
    //temporary parameter for setting #threads to use, globally
    public static boolean IMMEDIATE_ETERNALIZATION=true;
    
    
   // public static int STM_SIZE = 1;
    public static int SEQUENCE_BAG_ATTEMPTS = 10; //5 //20
    public static int CONDITION_BAG_ATTEMPTS = 10; //5 //20

    public static float DERIVATION_PRIORITY_LEAK = 0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
    
    public static float DERIVATION_DURABILITY_LEAK = 0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
    
    public static float CURIOSITY_BUSINESS_THRESHOLD=0.18f; //dont be curious if business is above
    public static float CURIOSITY_PRIORITY_THRESHOLD=0.3f; //0.3f in 1.6.3
    public static float CURIOSITY_CONFIDENCE_THRESHOLD=0.8f;
    public static float CURIOSITY_DESIRE_CONFIDENCE_MUL=0.1f; //how much risk is the system allowed to take just to fullfill its hunger for knowledge?
    public static float CURIOSITY_DESIRE_PRIORITY_MUL=0.1f; //how much priority should curiosity have?
    public static float CURIOSITY_DESIRE_DURABILITY_MUL=0.3f; //how much durability should curiosity have?
    public static boolean CURIOSITY_FOR_OPERATOR_ONLY=false; //for Peis concern that it may be overkill to allow it for all <a =/> b> statement, so that a has to be an operator
    public static boolean CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF=false;
    
    public static float HAPPY_EVENT_HIGHER_THRESHOLD=0.75f;
    public static float HAPPY_EVENT_LOWER_THRESHOLD=0.25f;
    public static float BUSY_EVENT_HIGHER_THRESHOLD=0.9f; //1.6.4, step by step^, there is already enough new things ^^
    public static float BUSY_EVENT_LOWER_THRESHOLD=0.1f;
    public static boolean REFLECT_META_HAPPY_GOAL=false;
    public static boolean CONSIDER_REMIND=false;
    public static boolean BREAK_NAL_HOL_BOUNDARY=false;
    
    public static boolean QUESTION_GENERATION_ON_DECISION_MAKING=false;
    public static boolean HOW_QUESTION_GENERATION_ON_DECISION_MAKING=true;
    
    public static float ANTICIPATION_CONFIDENCE = 0.90f;
    public static float ANTICIPATION_TOLERANCE = 50.0f;
    
    public static float CONSIDER_NEW_OPERATION_BIAS = 0.05f; //depriorizes older operation-related events in temporal inference
    
    public static float TEMPORAL_INDUCTION_PRIORITY_PENALTY = 1.0f; //was 0.1
    
    public static int AUTOMATIC_DECISION_USUAL_DECISION_BLOCK_CYCLES = 500;
    
    public static float SATISFACTION_TRESHOLD = 0.0f; //decision threshold is enough for now
    
    public static float COMPLEXITY_UNIT=1.0f; //1.0 - oo
    
    public static float INTERVAL_ADAPT_SPEED = 4.0f;
    
    public static boolean COMPOUND_OPERATIONS=true;
    
    public static int TASKLINK_PER_CONTENT = 4; //eternal/event are also seen extra
    
    /** Default priority of exection feedback */
    public static float DEFAULT_FEEDBACK_PRIORITY = (float) 0.9;
    /** Default durability of exection feedback */
    public static float DEFAULT_FEEDBACK_DURABILITY = (float) 0.5; //was 0.8 in 1.5.5; 0.5 after
    
    /* Priority penalty for sequences that start with an operation */
    public static float OPERATION_SEQUENCE_START_PENALTY = 0.05f;
    /* Priority penalty for sequences that end with an operation */
    public static float NOT_OPERATION_SEQUENCE_END_PENALTY = 0.5f;
    
     /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE 
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.*/
    public static final float CONCEPT_FORGET_DURATIONS = 2.0f;

    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public static final float TERMLINK_FORGET_DURATIONS = 10.0f;
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public static final float TASKLINK_FORGET_DURATIONS = 4.0f;
    
    /** Sequence bag forget durations **/
    public static final float EVENT_FORGET_DURATIONS = 4.0f;
    
    /** Minimum expectation for a desire value to execute an operation. 
     *  the range of "now" is [-DURATION, DURATION]; */
    public static final float DECISION_THRESHOLD = 0.51f;
    
}

