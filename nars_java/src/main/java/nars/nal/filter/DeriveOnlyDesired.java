/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.filter;

import nars.nal.NAL;
import nars.nal.DerivationFilter;
import nars.nal.Sentence;
import nars.nal.Task;

/**
<patham9_> desire value form the entire motivation management, what i did was allowing only input judgements and derived goals (no derived judgements) :D
<patham9_> only exception: allowed temporal induction derived judgements 
 */
public class DeriveOnlyDesired implements DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.sentence.isGoal()) {
            if (task.sentence.isEternal())
                return "Not Goal";
        }
        return null;        
    }
    
}
