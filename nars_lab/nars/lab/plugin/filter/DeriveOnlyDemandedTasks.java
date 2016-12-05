/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.plugin.filter;

import nars.control.DerivationContext;
import nars.control.DerivationContext.DerivationFilter;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Symbols;
import nars.operator.Operation;

/**
* only allowing derivation of tasks where a demand(goal) exists
* this is one of the aspects which make metacat fast
* that there is a global optimization criteria which controls the entire ting
* WARNING: this mode does not apply to AGI
 */
public class DeriveOnlyDemandedTasks implements DerivationFilter {

    @Override
    public String reject(DerivationContext nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief) {
        
        Sentence s = task.sentence;
        
        if ((s.punctuation == Symbols.JUDGMENT_MARK) && !(s.term instanceof Operation)) {
            
            boolean noConcept = (nal.memory.concept(s.term) == null);

            if (noConcept) {
                //there is no question and goal of this, return
                return "No demand exists";
            }
        }

        return null;
    }

}
