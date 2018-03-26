package nars.config;

import nars.NAR;
import nars.operator.mental.Anticipate;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import nars.plugin.misc.RuntimeNARSettings;
import nars.plugin.mental.Emotions;
import nars.plugin.mental.Counting;
import nars.plugin.mental.Abbreviation;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Plugins {

    public NAR init(NAR n) {         
        n.addPlugin(new RuntimeNARSettings());
        n.addPlugin(new Emotions());
        n.addPlugin(new Anticipate());      // expect an event  
        
        boolean full_internal_experience = false;
        if(!full_internal_experience) {
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
