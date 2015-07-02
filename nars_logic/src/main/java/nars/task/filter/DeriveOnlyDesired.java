/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.task.filter;

import nars.nal.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;

/**
<patham9_> desire value form the entire motivation management, what i did was allowing only input judgements and derived goals (no derived judgements) :D
<patham9_> only exception: allowed temporal induction derived judgements 
 */
public class DeriveOnlyDesired implements DerivationFilter {

    @Override
    public String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.isGoal()) {
            if (task.isEternal())
                return "Not Goal";
        }
        return null;        
    }
    
}
