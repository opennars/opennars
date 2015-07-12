package nars.process;

import nars.Memory;
import nars.nal.Premise;
import nars.task.Task;

import java.util.List;

/** called after a ConceptProcess has derived Tasks */
public interface DerivationReaction {
    public void onDerivation(Premise p, Iterable<Task> derived, Memory m);
}
