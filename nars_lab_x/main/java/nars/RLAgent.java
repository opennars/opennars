package nars.rl;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;



public abstract class RLAgent implements AgentInterface {

    protected Action action;
    TaskSpec problem = null;

    public RLAgent() {
    }

    public void agent_cleanup() {
    }

    public void agent_end(double arg0) {
    }

    public void agent_freeze() {
    }

    public void agent_init(String taskSpec) {
        problem = new TaskSpec(taskSpec);
        //        if (problem.getVersionString().equals("Mario-v1")) {
        //            TaskSpecVRLGLUE3 hardCodedTaskSpec = new TaskSpecVRLGLUE3();
        //            hardCodedTaskSpec.setEpisodic();
        //            hardCodedTaskSpec.setDiscountFactor(1.0d);
        //            //Run
        //            hardCodedTaskSpec.addDiscreteAction(new IntRange(-1, 1));
        //            //Jump
        //            hardCodedTaskSpec.addDiscreteAction(new IntRange(0, 1));
        //            //Speed
        //            hardCodedTaskSpec.addDiscreteAction(new IntRange(0, 1));
        //            problem = new TaskSpec(hardCodedTaskSpec);
        //        }
        action = new Action(problem.getNumDiscreteActionDims(), problem.getNumContinuousActionDims());
    }

    public String agent_message(String arg0) {
        return null;
    }

    abstract public Action agent_start(Observation o);

    public abstract Action agent_step(double reward, Observation o);
}