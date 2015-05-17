package nars.nal.nal8;

import nars.nal.Task;
import nars.nal.concept.Concept;

/**
 * A method of deciding if an execution should proceed.
 */
public interface Decider {
    public boolean decide(Concept c, Operation op);
}
