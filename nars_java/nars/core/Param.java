package nars.core;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

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
    

    
    
}
