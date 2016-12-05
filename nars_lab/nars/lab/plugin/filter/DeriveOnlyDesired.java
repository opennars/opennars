/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.plugin.filter;

import nars.control.DerivationContext;
import nars.control.DerivationContext.DerivationFilter;
import nars.entity.Sentence;
import nars.entity.Task;

/**
<patham9_> desire value form the entire motivation management, what i did was allowing only input judgements and derived goals (no derived judgements) :D
<patham9_> only exception: allowed temporal induction derived judgements 
 */
public class DeriveOnlyDesired implements DerivationFilter {

    @Override
    public String reject(DerivationContext nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief) {
        if (!task.sentence.isGoal()) {
            if (task.sentence.isEternal())
                return "Not Goal";
        }
        return null;        
    }
    
}
