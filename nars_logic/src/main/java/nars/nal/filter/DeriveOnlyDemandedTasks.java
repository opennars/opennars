/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.filter;

import nars.Symbols;
import nars.nal.NAL;
import nars.nal.DerivationFilter;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.nal8.Operation;

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
