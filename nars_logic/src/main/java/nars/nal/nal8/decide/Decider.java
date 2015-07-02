package nars.nal.nal8.decide;

import nars.concept.Concept;
import nars.nal.nal8.Operation;

/**
 * A method of deciding if an execution should proceed.
 */
public interface Decider {
    public boolean decide(Concept c, Operation op);
}
