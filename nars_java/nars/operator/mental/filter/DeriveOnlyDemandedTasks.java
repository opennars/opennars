/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.operator.mental.filter;

import nars.core.control.NAL;
import nars.core.control.NAL.DerivationFilter;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Symbols;
import nars.operator.Operation;

/**
 *
 * @author me
 */
public class DeriveOnlyDemandedTasks implements DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief) {
        
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
