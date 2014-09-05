package nars.core;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nars.util.meter.util.AtomicDouble;

/**
 * NAR Parameters which can be changed during runtime.
 */
public class Param implements Serializable {

    /** Silent threshold for task reporting, in [0, 100]. 
     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
     */
    public final AtomicInteger noiseLevel = new AtomicInteger();
    
    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE */
    public final AtomicInteger conceptForgettingRate = new AtomicInteger();
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public final AtomicInteger beliefForgettingRate = new AtomicInteger();
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public final AtomicInteger taskForgettingRate = new AtomicInteger();

    
    /** How many inputs to process each cycle */
    public final AtomicInteger cycleInputTasks = new AtomicInteger();

    /** How many memory cycles to process each cycle */
    public final AtomicInteger cycleMemory = new AtomicInteger();

    /** How many concepts to fire each cycle */
    public final AtomicInteger cycleConcepts = new AtomicInteger();
    
    /** contraposition should have much lower priority considering the flood of implications coming from temporal knowledge.  a lower value means more frequent, must be > 0 */    
    public final AtomicDouble contrapositionPriority = new AtomicDouble(/*30 was the default*/);

    
    //let NARS use NARS+ ideas (counting etc.)
    public final AtomicBoolean experimentalNarsPlus = new AtomicBoolean();

    //let NARS use NAL9 operators to perceive its own mental actions
    public final AtomicBoolean internalExperience = new AtomicBoolean();
    
    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it: 
    public final AtomicInteger abbreviationMinComplexity = new AtomicInteger();
    public final AtomicDouble abbreviationMinQuality = new AtomicDouble();
    

    
    
}
