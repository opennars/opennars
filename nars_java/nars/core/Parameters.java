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
package nars.core;


/**
 * NAR operating parameters.
 * All static values will be removed so that this is an entirely dynamic class.
 */
public class Parameters {
    
    
    //FIELDS BELOW ARE BEING CONVERTED TO DYNAMIC, NO MORE STATIC: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //
    //Pei comments: parameters will be separated into a dynamic group and a static group
    //              and the latter contains "personality parameters" that cannot be changed
    //              in the lifetime of the system, though different systems may take different
    //              values. For example, to change HORIZON dynamically will cause inconsistency 
    //              in evidence evaluation.
    
    //internal experience has less durability?
    public static float INTERNAL_EXPERIENCE_DURABILITY_MUL=0.5f;
    //internal experience has less priority?
    public static float INTERNAL_EXPERIENCE_PRIORITY_MUL=0.5f;
    //internal experience has less quality?
    public static float INTERNAL_EXPERIENCE_QUALITY_MUL=0.5f;
    
    
    public static float MAX_CONFIDENCE = 0.9999f;

    
    

    /* ---------- logical parameters ---------- */
    /** Evidential Horizon, the amount of future evidence to be considered. 
     * Must be >=1.0, usually 1 .. 2
     */
    public static final float HORIZON = 1;
    
    /** Reliance factor, the empirical confidence of analytical truth. */
    public static final float RELIANCE = (float) 0.9;    // the same as default confidence
    

    
    /** determines the internal precision used for TruthValue calculations.
     *  a value of 0.01 gives 100 truth value states between 0 and 1.0.
     *  other values may be used, for example, 0.02 for 50, 0.10 for 10, etc.
     *  Change at your own risk
     */
    public static final float TRUTH_EPSILON = 0.01f;
    public static final float TRUTH_PRECISION = 1.0f / TRUTH_EPSILON;

    
    /* ---------- budget thresholds ---------- */
    /** The budget threshold rate for task to be accepted. */
    public static final float BUDGET_THRESHOLD = (float) 0.01;

    /* ---------- default input values ---------- */
    /** Default expectation for confirmation. */
    public static final float DEFAULT_CONFIRMATION_EXPECTATION = (float) 0.8;
    /** Default expectation for confirmation. */
    public static final float DEFAULT_CREATION_EXPECTATION = (float) 0.66;
    /** Default confidence of input judgment. */
    public static final float DEFAULT_JUDGMENT_CONFIDENCE = (float) 0.9;
    /** Default priority of input judgment */
    public static float DEFAULT_JUDGMENT_PRIORITY = (float) 0.8;
    /** Default durability of input judgment */
    public static final float DEFAULT_JUDGMENT_DURABILITY = (float) 0.5; //was 0.8 in 1.5.5; 0.5 after
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
    
    /** Level separation in Bag, one digit, for display (run-time adjustable) and management (fixed) */
    public static final float BAG_THRESHOLD = 0.1f;
    

    
    
    /* ---------- avoiding repeated reasoning ---------- */
        /** Maximum length of the evidental base of the Stamp, a power of 2 */
    public static final int MAXIMUM_EVIDENTAL_BASE_LENGTH = 20;
    /** Maximum length of the Derivation Chain of the stamp */
    public static final int MAXIMUM_DERIVATION_CHAIN_LENGTH = 20;
    
    /** Maximum length of Stamp, a power of 2 */
    //public static final int MAXIMUM_STAMP_LENGTH = 8;

    


    /**
     * The rate of confidence decrease in mental operations Doubt and Hesitate
     * set to zero to disable this feature.
     */
    public static float DISCOUNT_RATE = 0.5f;    

    
    
    
    
    
    
    //RUNTIME PERFORMANCE (should not affect logic): ----------------------------------
    
    /**
     * max length of a Term name for which it can be stored statically via String.intern().
     * set to zero to disable this feature.
     * The problem with indiscriminate use of intern() is that interned strings can not be garbage collected (i.e. permgen) - possible a memory leak if terms disappear.
     */
    public static int INTERNED_TERM_NAME_MAXLEN = 0;
          
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
    public static int ROPE_TERMLINK_TERM_SIZE_THRESHOLD = 64;
}

