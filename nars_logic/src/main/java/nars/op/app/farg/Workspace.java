/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.op.app.farg;

import nars.Events.CycleEnd;
import nars.NAR;
import nars.event.NARReaction;
import nars.concept.Concept;

/**
 *
 * @author patrick.hammer
 */
public class Workspace extends NARReaction {

    private final FluidAnalogiesAgents farg;
    public double temperature=0.0;
    public NAR nar;
    public int n_concepts=0;
    
    public Workspace(FluidAnalogiesAgents farg, NAR nar) {
        super(nar, CycleEnd.class);

        this.farg = farg;
        this.nar=nar;


    }

    @Override
    public void event(Class event, Object[] args) {
        for(int i=0;i<10;i++) { //process 10 codelets in each step
            Codelet cod=farg.coderack.pop();
            if(cod!=null) {
                if(cod.run(this)) {
                    farg.coderack.put(cod);
                }
            }
            temperature=calc_temperature();
        }
        controller();
    }

    public void controller() { 
        //when to put in Codelets of different type, and when to remove them
        //different controller for different domains would inherit from FARG
    }
    
    public double calc_temperature() {
        double s=0.0f;
        n_concepts=0;
        for(Concept node : nar.memory.cycle) {
            if(!node.getGoals().isEmpty()) {
                s+=node.getPriority()* node.getGoals().get(0).getTruth().getExpectation();
            }
            n_concepts++;
        }
        return s/((double) n_concepts);
    }
}