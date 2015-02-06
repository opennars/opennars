/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.plugin.input;

import java.util.ArrayList;
import nars.core.EventEmitter;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Plugin;
import nars.core.control.NAL;
import nars.entity.Concept;
import nars.entity.Task;
import nars.language.Conjunction;
import nars.language.Term;

/**
 *
 * @author tc
 */
public class PerceptionAccel implements Plugin, EventEmitter.EventObserver {

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        //register listening to new events:
        n.memory.event.set(this, enabled, Events.InduceSucceedingEvent.class);

        return true;
    }
    
    ArrayList<Task> eventbuffer=new ArrayList<>();
    int cur_maxlen=10;
    
    public void perceive() { //implement Peis idea here now
        
    }
    
    //keep track of how many conjunctions with related amount of component terms there are:
    int[] sv=new int[1000]; //use static array, should suffice for now
    public void handleConjunctionSequence(Term t, boolean Add) {
        if(!(t instanceof Conjunction)) {
            return;
        }
        Conjunction c=(Conjunction) t;
        if(Add) {
            sv[c.term.length]++;
        } else {
            sv[c.term.length]--;
        }
        
    }
    
    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.InduceSucceedingEvent.class) { //todo misleading event name, it is for a new incoming event
            Task newEvent = (Task)args[0];
            eventbuffer.add(newEvent);
            if(eventbuffer.size()>cur_maxlen) {
                eventbuffer.remove(0);
            }
            NAL nal= (NAL)args[1];
            perceive();
        }
        if(event == Events.ConceptForget.class) {
            Concept forgot=(Concept) args[0];
            handleConjunctionSequence(forgot.term,false);
        }
        if(event == Events.ConceptNew.class) {
            Concept newC=(Concept) args[0];
            handleConjunctionSequence(newC.term,true);
        }
    }
    
}
