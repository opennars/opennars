package nars.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import nars.storage.Memory;
import nars.NAR;
import nars.control.WorkingCycle;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.io.DefaultTextPerception;
import nars.io.TextInput;
import nars.language.Term;
import nars.operator.Operator;
import nars.operator.mental.Anticipate;
//import nars.lab.plugin.app.plan.TemporalParticlePlanner;
//import nars.lab.plugin.input.PerceptionAccel;
import nars.plugin.mental.Abbreviation;
import nars.lab.plugin.mental.Counting;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import nars.plugin.mental.RuntimeNARSettings;
import nars.storage.Bag;
import nars.storage.LevelBag;
import nars.plugin.mental.Emotions;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Default extends Parameters {

    public Param param = new Param();

    public Memory newMemory(Param p) {        
        return new Memory(p, 
                new WorkingCycle(new LevelBag(Parameters.CONCEPT_BAG_LEVELS, Parameters.CONCEPT_BAG_SIZE)), 
                new LevelBag<>(Parameters.NOVEL_TASK_BAG_LEVELS, Parameters.NOVEL_TASK_BAG_SIZE),
                new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE));
    }


    public NAR init(NAR n) {
        
        for (Operator o : DefaultOperators.get(n))
            n.memory.addOperator(o);
        for (Operator o : ExampleOperators.get())
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
