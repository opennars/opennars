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
import nars.operator.Operator;


public class BeRational implements DerivationFilter {

    Operator want;
    Operator believe;
    
    @Override
    public String reject(NAL nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief) {
        
        if (want == null) {
            want = nal.memory.getOperator("^want");
            believe = nal.memory.getOperator("^believe");
        }
        
        if (task.sentence.punctuation==Symbols.GOAL_MARK && task.sentence.content instanceof Operation) {
            Operation o = (Operation)task.sentence.content;
            if (o.getPredicate().equals(want) || o.getPredicate().equals(believe)) {
                return "Irrational";
            }
        }
        return null;
    }
    
}
