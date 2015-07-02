/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.task.filter;

import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.nal.nal8.Operation;
import nars.task.TaskSeed;
import nars.term.Compound;

/**
* only allowing derivation of tasks where a demand(goal) exists
* this is one of the aspects which make metacat fast
* that there is a global optimization criteria which controls the entire ting
* WARNING: this mode does not apply to AGI
 */
public class DeriveOnlyDemandedTasks implements DerivationFilter {

    @Override
    public String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        

        Compound x = task.getTerm();
        
        if ((task.isJudgment()) && !(x instanceof Operation)) {
            
            boolean noConcept = (nal.memory.concept(x) == null);

            if (noConcept) {
                //there is no question and goal of this, return
                return "No demand exists";
            }
        }

        return null;
    }

}
