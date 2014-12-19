/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.inference;

import java.util.List;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Task;
import nars.io.ExampleFileInput;
import nars.io.TextOutput;
import nars.io.condition.OutputCondition;
import nars.io.condition.OutputContainsCondition;

/**
 * Graph analysis of reasoning processes to determine essential and non-essential
 * activity
 */
public class LogicPerformance {
    
    public static void main(String[] args) throws Exception {
        
        NAR n = new NAR(new Default());
        
        ExampleFileInput example = ExampleFileInput.getExample("test/nal1.0");
                
        List<OutputCondition> conditions = example.getConditions(n, 5);

        n.addInput(example);
        
        new TextOutput(n, System.out);
        
        n.run(100);
        
        for (OutputCondition o : conditions) {
            if (o instanceof OutputContainsCondition) {
                OutputContainsCondition c = (OutputContainsCondition)o;
                System.out.println(c);
                List<Task> t = c.getTrueReasons();
                
            }
                
            
        }
        
    }
}
