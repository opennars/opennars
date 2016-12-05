package nars.config;

import java.util.concurrent.atomic.AtomicInteger;
import nars.language.Interval.AtomicDuration;
import com.google.common.util.concurrent.AtomicDouble;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import nars.control.DerivationContext.DerivationFilter;

/**
 * NAR Parameters which can be changed during runtime.
 */
public class Param implements Serializable {
    
    public Param() {    }

    /** Silent threshold for task reporting, in [0, 100]. 
     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
     */
    public final AtomicInteger noiseLevel = new AtomicInteger(100);
    
    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    public final AtomicDuration duration = new AtomicDuration(Parameters.DURATION);
    
    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE 
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.
     */
    public final AtomicDouble conceptForgetDurations = new AtomicDouble(2.0);
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public final AtomicDouble termLinkForgetDurations = new AtomicDouble(10.0);
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public final AtomicDouble taskLinkForgetDurations = new AtomicDouble(4.0);
    
    /** Sequence bag forget durations **/
    public final AtomicDouble sequenceForgetDurations = new AtomicDouble(4.0);
    
    /** novel task bag forget duration **/
    public final AtomicDouble novelTaskForgetDurations = new AtomicDouble(2.0);

    
    /** Minimum expectation for a desire value. 
     *  the range of "now" is [-DURATION, DURATION]; */
    public final AtomicDouble decisionThreshold = new AtomicDouble(0.6);
    
    
//    //let NARS use NARS+ ideas (counting etc.)
//    public final AtomicBoolean experimentalNarsPlus = new AtomicBoolean();
//
//    //let NARS use NAL9 operators to perceive its own mental actions
//    public final AtomicBoolean internalExperience = new AtomicBoolean();
    
    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it: 
    //public final AtomicInteger abbreviationMinComplexity = new AtomicInteger();
    //public final AtomicDouble abbreviationMinQuality = new AtomicDouble();
    


    public List<DerivationFilter> defaultDerivationFilters = new ArrayList();
    public final List<DerivationFilter> getDerivationFilters() {
        return defaultDerivationFilters;
    }
}
