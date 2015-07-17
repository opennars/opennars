package nars.process;

import nars.Memory;
import nars.premise.Premise;
import nars.task.Task;

/** called after a ConceptProcess has derived Tasks
 *  these tasks have not been input to memory yet, and
 *  this method is responsible for inputting them
 *  after any postprocessing it wants to apply.
 * */
public interface DerivationReaction {
    public void onDerivation(Premise p, Iterable<Task> derived, Memory m);
}
