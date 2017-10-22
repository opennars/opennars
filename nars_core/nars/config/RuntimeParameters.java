package nars.config;

import nars.language.Interval.AtomicDuration;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import nars.control.DerivationContext.DerivationFilter;
import nars.language.Interval.PortableDouble;
import nars.language.Interval.PortableInteger;

/**
 * NAR Parameters which can be changed during runtime.
 */
public class RuntimeParameters implements Serializable {
    
    public RuntimeParameters() {    }

    /** Silent threshold for task reporting, in [0, 100]. 
     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
     */
    public final PortableInteger noiseLevel = new PortableInteger(100);
    
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
    public final PortableDouble conceptForgetDurations = new PortableDouble(2.0);
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public final PortableDouble termLinkForgetDurations = new PortableDouble(10.0);
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public final PortableDouble taskLinkForgetDurations = new PortableDouble(4.0);
    
    /** Sequence bag forget durations **/
    public final PortableDouble eventForgetDurations = new PortableDouble(4.0);
    
    /** How much priority a goal must have to trigger an automatic reaction **/
    public final PortableDouble reactionPriorityThreshold = new PortableDouble(0.1);

    
    /** Minimum expectation for a desire value. 
     *  the range of "now" is [-DURATION, DURATION]; */
    public final PortableDouble decisionThreshold = new PortableDouble(0.51);
    
    
//    //let NARS use NARS+ ideas (counting etc.)
//    public final AtomicBoolean experimentalNarsPlus = new AtomicBoolean();
//
//    //let NARS use NAL9 operators to perceive its own mental actions
//    public final AtomicBoolean internalExperience = new AtomicBoolean();
    
    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it: 
    //public final PortableInteger abbreviationMinComplexity = new PortableInteger();
    //public final PortableDouble abbreviationMinQuality = new PortableDouble();
    


    public List<DerivationFilter> defaultDerivationFilters = new ArrayList();
    public final List<DerivationFilter> getDerivationFilters() {
        return defaultDerivationFilters;
    }
}
