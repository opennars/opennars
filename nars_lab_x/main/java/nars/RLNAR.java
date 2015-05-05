package nars.rl;

import nars.core.NAR;
import nars.model.Default;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

/**
 *
 * @author me
 */
public class RLNAR extends RLAgent {

    private final NAR nar;
    int thoughtCycles = 1000;

    public static void main(String[] args) {
        NAR n = new Default().build();

        AgentLoader L = new AgentLoader(new RLNAR(n));
        L.run();
    }

    public RLNAR(NAR nar) {
        super();
        this.nar = nar;
    }

    //initial axioms, etc
    protected void start() {

    }

    protected void inputObservation(Observation o) {

    }

    protected void inputReward(double r) {

    }

    protected void think() {
        nar.run(thoughtCycles);
    }

    @Override
    public Action agent_start(Observation o) {
        start();

        inputObservation(o);

        think();

        action.intArray[0] = decideAction(0);
        return action;
    }

    protected void resetActionDecisions() {

    }

    protected int decideAction(int actionSet) {
        return 0;
    }

    @Override
    public Action agent_step(double reward, Observation o) {
        inputReward(reward);
        inputObservation(o);

        resetActionDecisions();

        think();

        action.intArray[0] = decideAction(0);

        return action;
    }

}
