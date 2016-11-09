/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.timeline;

import java.io.File;
import java.io.FileNotFoundException;
import nars.util.EventEmitter.EventObserver;
import nars.util.Events;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Concept;
import nars.io.TextInput;
import nars.io.TextOutput;

/**
 *
 * @author me
 */
public class BeliefRedundancy {
    
    public static void main(String[] args) throws FileNotFoundException {
        NAR n = new NAR(new Default());
        
        TextInput i = new TextInput(new File("nal/TestChamber/TestChamberIndependentExperience/switch_on_door_opened.nal"));
    
        new TextOutput(n, System.out);

        n.on(Events.ConceptBeliefAdd.class, new EventObserver() {
            @Override public void event(Class event, Object[] a) {
                Concept c = (Concept)a[0];
                if (c.beliefs.size() > 2) {
                    CharSequence ss = c.getBeliefsSummary();
                    System.out.println("---- " + c + "  -------------");
                    System.out.println(ss);
                    System.out.println("-------------------");
                    
                }
            }            
        });
        
        n.addInput(i);
        
        n.run(1000);
        System.out.println(n.time());
        
    }
}
