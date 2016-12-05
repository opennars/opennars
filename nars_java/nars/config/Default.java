package nars.config;

import nars.storage.Memory;
import nars.NAR;
import nars.control.WorkingCycle;
import nars.io.DefaultTextPerception;
import nars.lab.operator.software.Javascript;
import nars.operator.Operator;
import nars.operator.mental.Anticipate;
import nars.plugin.mental.InternalExperience;
import nars.plugin.mental.RuntimeNARSettings;
import nars.storage.LevelBag;
import nars.plugin.mental.Emotions;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Default {

    public RuntimeParameters param = new RuntimeParameters();

    public Memory newMemory(RuntimeParameters p) {        
        return new Memory(p, 
                new WorkingCycle(new LevelBag(Parameters.CONCEPT_BAG_LEVELS, Parameters.CONCEPT_BAG_SIZE)), 
                new LevelBag<>(Parameters.NOVEL_TASK_BAG_LEVELS, Parameters.NOVEL_TASK_BAG_SIZE),
                new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE));
    }


    public NAR init(NAR n) {
        
        Javascript js=new Javascript();
        js.setEnabled(n, true);
        
        for (Operator o : Operators.get(n))
            n.memory.addOperator(o);
                
        n.addPlugin(new DefaultTextPerception());
        n.addPlugin(new RuntimeNARSettings());
        n.addPlugin(new Emotions());
        n.addPlugin(new Anticipate());      // expect an event
        
                 
        n.addPlugin(new InternalExperience());

        //full internal experience:
        //n.addPlugin(new FullInternalExperience());
        //n.addPlugin(new Abbreviation());
        //n.addPlugin(new Counting());
        
        return n;
    }
}
