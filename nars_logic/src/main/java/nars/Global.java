/*
 * Parameters.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars;


import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import nars.nal.Task;


import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Global NAR operating parameters (static scope)
 * Contains many static values which will eventually be migrated to
 * dynamic parameters specific to a component (allowing independent control).
 * (They began here for developmentconvenience)
 *
 */
public class Global {



    public static int DEFAULT_NAL_LEVEL = 8;

    /** use this for advanced error checking, at the expense of lower performance.
        it is enabled for unit tests automatically regardless of the value here.    */
    public static boolean DEBUG = false;
    public static boolean DEBUG_BAG = true; // for complete bag debugging
    public static final boolean DEBUG_BAG_MASS = false;
    public static boolean DEBUG_TRACE_EVENTS = false; //shows all emitted events
    public static boolean DEBUG_DERIVATION_STACKTRACES = false; //includes stack trace in task's derivation rule string
    public static boolean DEBUG_INVALID_SENTENCES = true;
    public static boolean DEBUG_NONETERNAL_QUESTIONS = false;
    public static boolean DEBUG_TASK_HISTORY = true; //false disables task history completely
    public static boolean EXIT_ON_EXCEPTION = true;


    //FIELDS BELOW ARE BEING CONVERTED TO DYNAMIC, NO MORE STATIC: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //
    //Pei comments: parameters will be separated into a dynamic group and a static group
    //              and the latter contains "personality parameters" that cannot be changed
    //              in the lifetime of the system, though different systems may take different
    //              values. For example, to change HORIZON dynamically will cause inconsistency 
    //              in evidence evaluation.



    public static final int METRICS_HISTORY_LENGTH = 256;

    /* ---------- logical parameters ---------- */
    /** Evidential Horizon, the amount of future evidence to be considered. 
     * Must be >=1.0, usually 1 .. 2
     */
    public static float HORIZON = 1;








    /** minimum difference necessary to cause a modifciation in budget components */
    public static float BUDGET_EPSILON = 0.01f/2f;


    /* ---------- default input values ---------- */
    /** Default expectation for confirmation. */
    public static final float DEFAULT_CONFIRMATION_EXPECTATION = 0.8f;

    /** Default confidence of input judgment. */
    public static final float DEFAULT_JUDGMENT_CONFIDENCE = 0.9f;
    /** Default priority of input judgment */
    public static float DEFAULT_JUDGMENT_PRIORITY = 0.8f;
    /** Default durability of input judgment */
    public static float DEFAULT_JUDGMENT_DURABILITY = 0.5f; //was 0.8 in 1.5.5; 0.5 after
    /** Default priority of input question */
    public static final float DEFAULT_QUESTION_PRIORITY = 0.9f;
    /** Default durability of input question */
    public static final float DEFAULT_QUESTION_DURABILITY = 0.9f;

    
     /** Default confidence of input goal. */
     public static final float DEFAULT_GOAL_CONFIDENCE = 0.9f;
     /** Default priority of input judgment */
     public static final float DEFAULT_GOAL_PRIORITY = 0.9f;
     /** Default durability of input judgment */
     public static final float DEFAULT_GOAL_DURABILITY = 0.9f;
     /** Default priority of input question */
     public static final float DEFAULT_QUEST_PRIORITY = 0.9f;
     /** Default durability of input question */
     public static final float DEFAULT_QUEST_DURABILITY = 0.9f;
 
    
    /* ---------- space management ---------- */
    
    /** (see its use in budgetfunctions iterative forgetting) */
    public static float MIN_FORGETTABLE_PRIORITY = 0f;

    
    
    /* ---------- avoiding repeated reasoning ---------- */
        /** Maximum length of the evidental base of the Stamp, a power of 2 */
    public static final int MAXIMUM_EVIDENTAL_BASE_LENGTH = 8;
    /** Maximum length of the Derivation Chain of the stamp */
    public static final int MAXIMUM_DERIVATION_CHAIN_LENGTH = 8;



    /**
     * The rate of confidence decrease in mental operations Doubt and Hesitate
     * set to zero to disable this feature.
     */
    public static float DISCOUNT_RATE = 0.5f;    

    /** enables the parsing of functional input format for operation terms: function(a,b,...) */
    public static boolean FUNCTIONAL_OPERATIONAL_FORMAT = true;
    
    
    
    
    
    
    //RUNTIME PERFORMANCE (should not affect logic): ----------------------------------

          

    public static final float TESTS_TRUTH_ERROR_TOLERANCE = 0.05f;

    //default
    public static float DEFAULT_TRUTH_EPSILON = 0.01f;

    public static float MAX_CONFIDENCE = 1.0f; //0.99f;// - TRUTH_EPSILON;
    public static float OPERATOR_EXECUTION_CONFIDENCE = MAX_CONFIDENCE - DEFAULT_TRUTH_EPSILON;

    //temporary parameter for setting #threads to use, globally
    @Deprecated public static int THREADS = 1;

    public static boolean IMMEDIATE_ETERNALIZATION=false; //WARNING: not working right



//    /** how many maximum cycles difference in ocurrence time
//     * are two non-eternal sentences considered equal, if all
//     * other features (term, punctuation, truth, ..) are equal.
//     * this is similar to Duration parameter
//     */
//    public static final int SentenceOcurrenceTimeCyclesEqualityThreshold = 1;

    /**
     * this determines the percentage of additional items (concepts, termlinks, tasklinks, ...etc)
     * which have their priority reduced by forgetting each cycle.
     *
     * forgetting an item can be applied as often
     * as possible since it is governed by rate over time.  however we can
     * afford to update item priority less frequently than every cycle.
     * an accuracy of 1.0 means to process approximatley all concepts every cycle,
     * while an accuracy of 0.5 would mean to process approximately half.
     *
     * since the items selected for update are determined by bag selection,
     * higher priority items will tend to get updated more frequently.
     *
     * an estimate for average "error" due to latency can be calculated
     * in terms of # of items, forgetting rate, and the accuracy rate.
     * more accuracy = lower error because concepts are more likely to receive forget sooner
     *
     * a lower bound on accuracy is when the expected latency exceeds the forgetting time,
     * in which case the forgetting will have been applied some amount of time past
     * when it would have completed its forget descent.
     */
    public static final float CONCEPT_FORGETTING_ACCURACY = 0.06f;

    //TODO scale these by % per cycle so it can perform more forgetting depending on time. if % exceeds ~100% it just means to do all of them and can avoid stochastic bag result
    public static final float TERMLINK_FORGETTING_ACCURACY = 0.05f;
    public static final float TASKLINK_FORGETTING_ACCURACY = 0.2f;

    /** global switch to allow derivation evidence to overlap */
    public static boolean OVERLAP_ALLOW = false;


    /** approx max percentage of concepts considered during a unification attempt */
    public static float DED_SECOND_UNIFICATION_DEPTH = 0.01f;
    public static final int DED_SECOND_UNIFICATION_ATTEMPTS = 1;


    /** hard upper-bound limit on Compound term complexity;
     * if this is exceeded it may indicate a recursively
     * malformed term due to a serious inference bug */
    public static short COMPOUND_MASS_LIMIT = 1024;


    public static float TEMPORAL_INDUCTION_CHAIN_SAMPLE_DEPTH(float taskPriority) {
        return 0.02f + taskPriority * 0.02f; //search up to 4% of concepts
    }

    public static int TEMPORAL_INDUCTION_CHAIN_SAMPLES = 1; //normal inference rule , this should be 10 to restore 1.6.1 behavior




    public static boolean equals(double a, double b) {
        return Math.abs(a - b) <= Double.MIN_VALUE*2;
    }
    public static boolean equals(float a, float b) {
        return Math.abs(a - b) <= Float.MIN_VALUE*2;
    }


    public static <X> List<X> newArrayList() {
        return new FastList(); //GS
        //return new ArrayList();
    }

    public static <K, V> Map<K,V> newHashMap(int capacity) {
        //return new FastMap<>(); //javolution http://javolution.org/apidocs/javolution/util/FastMap.html
        return new UnifiedMap<K,V>(capacity);
        //return new HashMap<>(capacity);
        //return new LinkedHashMap(capacity);
    }

    public static <X> List<X> newArrayList(int capacity) {
        return new FastList(capacity);
        //return new ArrayList(capacity);
    }

    public static <X> Set<X> newHashSet(int capacity) {
        return new UnifiedSet(capacity);
        //return new SimpleHashSet(capacity);
        //return new HashSet(capacity);
        //return new LinkedHashSet(capacity);
    }

    public static <X> Set<X> newHashSet(Collection<X> values) {
        Set<X> s = newHashSet(values.size());
        s.addAll(values);
        return s;
    }


//    public static <X,Y> HugeHashMap<X,Y> newHugeMap(int size, Class<? extends X> xClass, Class<? extends Y> yClass) {
//
//        HugeConfig config = HugeConfig.DEFAULT.clone()
//                .setSegments(128)
//                .setSmallEntrySize(128)
//                .setCapacity(size);
//
//        return new HugeHashMap(config, xClass, yClass);
//    }

    public static <K,V> Map<K, V> newHashMap() {
        return newHashMap(0);
    }


    public static Reference<Task> reference(Task t) {
        if (t == null) return null;
        return new SoftReference(t);
    }

    public static <C> Reference<C> reference(C s) {
        if (s == null) return null;
        return new SoftReference(s);
    }
    public static <C> C dereference(Reference<C> s) {
        if (s == null) return null;
        return s.get();
    }





    //TODO eventually sort out in case that a parameter is not needed anymore

    public static float CURIOSITY_BUSINESS_THRESHOLD=0.15f; //dont be curious if business is above
    public static float CURIOSITY_PRIORITY_THRESHOLD=0.3f; //0.3f in 1.6.3
    public static float CURIOSITY_CONFIDENCE_THRESHOLD=0.8f;
    public static float CURIOSITY_DESIRE_CONFIDENCE_MUL=0.1f; //how much risk is the system allowed to take just to fullfill its hunger for knowledge?
    public static float CURIOSITY_DESIRE_PRIORITY_MUL=0.1f; //how much priority should curiosity have?
    public static float CURIOSITY_DESIRE_DURABILITY_MUL=0.3f; //how much durability should curiosity have?
    public static boolean CURIOSITY_FOR_OPERATOR_ONLY=false; //for Peis concern that it may be overkill to allow it for all <a =/> b> statement, so that a has to be an operator
    public static boolean CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF=true;
    
    public static float HAPPY_EVENT_HIGHER_THRESHOLD=0.75f;
    public static float HAPPY_EVENT_LOWER_THRESHOLD=0.25f;
    public static float BUSY_EVENT_HIGHER_THRESHOLD=0.9f; //1.6.4, step by step^, there is already enough new things ^^
    public static float BUSY_EVENT_LOWER_THRESHOLD=0.1f;
    public static boolean REFLECT_META_HAPPY_GOAL=false;
    public static boolean CONSIDER_REMIND=false;

    public static boolean BREAK_NAL_HOL_BOUNDARY=true;
    
    public static boolean QUESTION_GENERATION_ON_DECISION_MAKING=false;
    public static boolean HOW_QUESTION_GENERATION_ON_DECISION_MAKING=false;
    
    public static float ANTICIPATION_CONFIDENCE=0.95f;
    
    public static float SATISFACTION_TRESHOLD = 0.0f; //decision threshold is enough for now



}

