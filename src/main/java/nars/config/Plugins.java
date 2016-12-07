package nars.config;

import nars.NAR;
import nars.io.DefaultTextPerception;
import nars.operator.Operator;
import nars.operator.mental.Anticipate;
import nars.operator.misc.Javascript;
import nars.plugin.mental.Abbreviation;
import nars.plugin.mental.Emotions;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import nars.plugin.misc.Counting;
import nars.plugin.misc.RuntimeNARSettings;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Plugins {

    public RuntimeParameters param = new RuntimeParameters();

    public NAR init(NAR n) {
        
        //allow javascript 
        Javascript js=new Javascript();
        js.setEnabled(n, true);
        
        for (Operator o : Operators.get(n))
            n.memory.addOperator(o);
                
        n.addPlugin(new DefaultTextPerception());
        n.addPlugin(new RuntimeNARSettings());
        n.addPlugin(new Emotions());
        n.addPlugin(new Anticipate());      // expect an event  
        
        boolean full_interval_experience = false;
        if(!full_interval_experience) {
            n.addPlugin(new InternalExperience());
        }
        else {
            n.addPlugin(new FullInternalExperience());
            n.addPlugin(new Abbreviation());
            n.addPlugin(new Counting());
        }
        
        return n;
    }
}
