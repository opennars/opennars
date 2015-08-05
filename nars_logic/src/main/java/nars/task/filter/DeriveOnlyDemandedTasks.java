/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.task.filter;

import nars.nal.nal8.Operation;
import nars.premise.Premise;
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
    public final String reject(Premise nal, TaskSeed task, boolean solution, boolean revised) {
        

        Compound x = task.getTerm();
        
        if ((task.isJudgment()) && !(x instanceof Operation)) {
            
            boolean noConcept = (nal.concept(x) == null);

            if (noConcept) {
                //there is no question and goal of this, return
                return "No demand exists";
            }
        }

        return null;
    }

}
