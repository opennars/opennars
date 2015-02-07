/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.plugin.input;

import java.util.ArrayList;
import nars.core.EventEmitter;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.TemporalRules;
import static nars.inference.TemporalRules.ORDER_BACKWARD;
import static nars.inference.TemporalRules.ORDER_FORWARD;
import nars.inference.TruthFunctions;
import nars.io.Symbols;
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
    int cur_maxlen=0;
    
    public void perceive(NAL nal) { //implement Peis idea here now
        for(int Len=2;Len<=cur_maxlen;Len++) {
            //ok, this is the length we have to collect, measured from the end of event buffer
            Term[] relterms=new Term[2*Len-1]; //there is a interval term for every event
            //measuring its distance to the next event, but for the last event this is obsolete
            //thus it are 2*Len-1] terms

            Task newEvent=eventbuffer.get(eventbuffer.size()-1);
            TruthValue truth=newEvent.sentence.truth;
            Stamp st=new Stamp(nal.memory);
            
            int k=0;
            for(int i=0;i<Len;i++) {
                int j=eventbuffer.size()-1-2*(Len-1)+i*2; //we count in 2-sized steps size amount of elements till to the end of the event buffer
                //
                Task current=eventbuffer.get(j);
                if(j!=0) {
                    truth=TruthFunctions.deduction(truth, current.sentence.truth);
                }
                st.getChain().add(current.sentence.term);
                relterms[k]=current.sentence.term;
                if(i!=Len-1) { //if its not the last one, then there is a next one for which we have to put an interval
                    Task next=eventbuffer.get(j+1);
                    relterms[k+1]=Interval.interval(next.sentence.getOccurenceTime()-current.sentence.getOccurenceTime(), nal.memory);
                }
                k+=2;
            }
            
            //decide on the tense of &/ by looking if the first event happens parallel with the last one
            //Todo refine in 1.6.3 if we want to allow input of difference occurence time
            boolean after=newEvent.sentence.after(eventbuffer.get(eventbuffer.size()-1-(Len-1)).sentence, nal.memory.param.duration.get());
            
            Conjunction C=(Conjunction) Conjunction.make(relterms, after ? ORDER_FORWARD : ORDER_BACKWARD);
            
            Sentence S=new Sentence(C,Symbols.JUDGMENT_MARK,truth,st);
            Task T=new Task(S,new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,Parameters.DEFAULT_JUDGMENT_DURABILITY,truth));
            
            nal.derivedTask(T, false, false, newEvent, S); //lets make the new event the parent task, and derive it
        }
    }
    
    //keep track of how many conjunctions with related amount of component terms there are:
    int sz=100;
    int[] sv=new int[sz]; //use static array, should suffice for now
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
        //determine cur_maxlen 
        //by finding the first complexity which exists
        for(int i=sz-1;i>=0;i--) {
            if(sv[i]>0) {
                cur_maxlen=i+1;
                break;
            }
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
