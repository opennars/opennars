/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic.reason.filter;

import nars.io.Symbols;
import nars.logic.NAL;
import nars.logic.NAL.DerivationFilter;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.nal8.Operation;

/**
* only allowing derivation of tasks where a demand(goal) exists
* this is one of the aspects which make metacat fast
* that there is a global optimization criteria which controls the entire ting
* WARNING: this mode does not apply to AGI
 */
public class DeriveOnlyDemandedTasks implements DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        
        Sentence s = task.sentence;
        
        if ((s.punctuation == Symbols.JUDGMENT) && !(s.term instanceof Operation)) {
            
            boolean noConcept = (nal.memory.concept(s.term) == null);

            if (noConcept) {
                //there is no question and goal of this, return
                return "No demand exists";
            }
        }

        return null;
    }

}
