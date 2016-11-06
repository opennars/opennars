/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.TruthValue;
import nars.plugin.app.plan.MultipleExecutionManager;
import nars.plugin.app.plan.MultipleExecutionManager.Execution;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * TODO test an operation sequence
 */
public class ExecutiveTest {
    
    @Test
    public void testTaskExecution() {
        NAR n = new NAR(new Default());
        MultipleExecutionManager e = n.memory.executive;
        
        e.setNumActiveTasks(1);
        
        e.tasks.add(new Execution(e, new TruthValue(1.0f, 0.5f)));
        e.tasks.add(new Execution(e, new TruthValue(1.0f, 0.05f)));
        
        assertEquals(1, e.tasks.size());
        assertEquals(0.75f, e.tasks.first().getDesire(), 0.01);
        
        e.tasks.clear();
        
        e.setNumActiveTasks(2);
        
        e.tasks.add(new Execution(e, new TruthValue(1.0f, 0.5f)));
        e.tasks.add(new Execution(e, new TruthValue(1.0f, 0.05f)));
        
        assertEquals(2, e.tasks.size());
        assertEquals(0.75f, e.tasks.first().getDesire(), 0.01);
        assertEquals(0.52f, e.tasks.last().getDesire(), 0.01);
        
        e.tasks.add(new Execution(e, new TruthValue(1.0f, 0.06f)));
        assertEquals(2, e.tasks.size());
    }
    
}
