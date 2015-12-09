/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.op.app.farg;

import nars.NAR;
import nars.util.event.CycleReaction;

/**
 *
 * @author patrick.hammer
 */
public class Workspace extends CycleReaction {

    private final FluidAnalogiesAgents farg;
    public double temperature=0.0;
    public NAR nar;
    public int n_concepts=0;
    
    public Workspace(FluidAnalogiesAgents farg, NAR nar) {
        super(nar);

        this.farg = farg;
        this.nar=nar;


    }

    @Override
    public void onCycle() {
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
        n_concepts=0;
        double[] s = {0.0f};
        nar.forEachConcept(node -> {
            if(!node.getGoals().isEmpty()) {
                s[0] +=node.getPriority()* node.getGoals().top().getTruth().getExpectation();
            }
            n_concepts++;
        });
        return s[0] /(n_concepts);
    }
}