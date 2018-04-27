package nars.main;

import nars.operator.mental.Anticipate;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import nars.plugin.misc.RuntimeNARSettings;
import nars.plugin.mental.Emotions;
import nars.plugin.mental.Counting;
import nars.plugin.mental.Abbreviation;
import nars.language.SetInt;
import nars.language.Term;
import nars.plugin.perception.VisionChannel;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Plugins {

    public NAR init(NAR n) {         
        n.addPlugin(new RuntimeNARSettings());
        n.addPlugin(new Emotions());
        n.addPlugin(new Anticipate());      // expect an event 
        Term label = SetInt.make(new Term("bright"));
        int sensor_W = 5;
        int sensor_H = 5;
        n.addSensoryChannel(label.toString(),
                            new VisionChannel(label, n, n, sensor_H, sensor_W, sensor_W*sensor_H));
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
