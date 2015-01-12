package nars.rl;

import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import java.util.Random;

/**
 *
 * @author me
 */
public class RandomAgent extends RLAgent {

    private Random random = new Random();


    public RandomAgent() {
    }

    public Action agent_start(Observation o) {
        randomify(action);
        return action;
    }
    

    public Action agent_step(double reward, Observation o) {
        System.out.println(o);
        System.out.println(reward);
        
        randomify(action);
        System.out.println(action);
        System.out.println();
        return action;
    }

    private void randomify(Action action) {
        for (int i = 0; i < problem.getNumDiscreteActionDims(); i++) {
            IntRange thisActionRange = problem.getDiscreteActionRange(i);
            action.intArray[i] = random.nextInt(thisActionRange.getRangeSize()) + thisActionRange.getMin();
        }
        for (int i = 0; i < problem.getNumContinuousActionDims(); i++) {
            DoubleRange thisActionRange = problem.getContinuousActionRange(i);
            action.doubleArray[i] = random.nextDouble() * (thisActionRange.getRangeSize()) + thisActionRange.getMin();
        }
    }

    public static void main(String[] args) {
        AgentLoader L = new AgentLoader(new RandomAgent());
        L.run();
    }

}
