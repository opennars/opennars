/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.task.filter;

import nars.premise.Premise;
import nars.task.Task;

/**
<patham9_> desire value form the entire motivation management, what i did was allowing only input judgements and derived goals (no derived judgements) :D
<patham9_> only exception: allowed temporal induction derived judgements 
 */
public class DeriveOnlyDesired implements DerivationFilter {

    @Override
    public String reject(Premise nal, Task task, boolean solution, boolean revised) {
        if (!task.isGoal()) {
            if (task.isEternal())
                return "Not Goal";
        }
        return null;        
    }
    
}
