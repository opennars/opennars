package nars.nal.nal5;

import nars.nal.task.TaskSeed;
import nars.nal.term.Compound;
import nars.nal.term.DefaultCompound;
import nars.nal.term.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction extends DefaultCompound {

    public Junction(final Term[] arg) {
        super(arg);
    }


}
