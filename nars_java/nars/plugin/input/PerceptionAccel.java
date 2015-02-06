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
import nars.language.Interval;
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
    
    public void perceive(NAL nal) { //implement Peis idea here now
        for(int Len=1;Len<=cur_maxlen;Len++) {
            //ok, this is the length we have to collect, measured from the end of event buffer
            Term[] relterms; //=new Term[2*Len-1]; //there is a interval term for every event
            //measuring its distance to the next event, but for the last event this is obsolete
            //thus it are 2*Len-1] terms
            
            //but it is not that easy, because it can also happen, that tasks happen in parallel,
            //in which a parallel conjunction has to be formed.
            //such a parallel conjunction then forms just one term!
            //so we have to determine how many of them exist at first,
            //if all of them happen at the same time the entire statement should
            //become an parallel conjunction..
            
            int k=0;
            for(int i=0;i<Len;i++) {
                int j=eventbuffer.size()-1-2*(Len-1)+i*2; //we count in 2-sized steps size amount of elements till to the end of the event buffer
                //
                Task current=eventbuffer.get(j);
                relterms[k]=current.sentence.term;
                if(i!=Len-1) { //if its not the last one, then there is a next one for which we have to put an interval
                    Task next=eventbuffer.get(j+1);
                    relterms[k+1]=Interval.interval(next.sentence.getOccurenceTime()-current.sentence.getOccurenceTime(), nal.memory);
                }
                
            }
            
        }
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
            while(eventbuffer.size()>cur_maxlen) {
                eventbuffer.remove(0);
            }
            NAL nal= (NAL)args[1];
            perceive(nal);
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
