/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.lab.plugin.app.farg;

import nars.util.EventEmitter.EventObserver;
import nars.util.Events.CycleEnd;
import nars.main.NAR;
import nars.entity.Concept;
import nars.storage.LevelBag;

/**
 *
 * @author patrick.hammer
 */
public class Workspace {

    public double temperature=0.0;
    public NAR nar;
    public int n_concepts=0;
    
    public Workspace(FluidAnalogiesAgents farg, NAR nar) {
        this.nar=nar;
        Workspace ws=this;
        farg.coderack=new LevelBag(farg.codelet_level,farg.max_codelets);
        nar.on(CycleEnd.class, new EventObserver() { 

            @Override
            public void event(Class event, Object[] args) {
                for(int i=0;i<10;i++) { //process 10 codelets in each step
                    Codelet cod=farg.coderack.takeNext();
                    if(cod!=null) {
                        if(cod.run(ws)) {
                            farg.coderack.putIn(cod);
                        }
                    }
                    temperature=calc_temperature();
                }
                controller();
            }
        });
    }
    
    public void controller() { 
        //when to put in Codelets of different type, and when to remove them
        //different controller for different domains would inherit from FARG
    }
    
    public double calc_temperature() {
        double s=0.0f;
        n_concepts=0;
        for(Concept node : nar.memory) {
            if(!node.desires.isEmpty()) {
                s+=node.getPriority()*node.desires.get(0).sentence.truth.getExpectation();
            }
            n_concepts++;
        }
        return s/((double) n_concepts);
    }
}