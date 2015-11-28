package nars.rl.horde.demons;

import nars.rl.horde.LinearLearner;
import nars.rl.horde.functions.RewardFunction;
import org.apache.commons.math3.linear.RealVector;


public class PredictionDemon<A> implements Demon<A> {
    private static final long serialVersionUID = -6966208035134604865L;
    private final RewardFunction rewardFunction;
    private final OnPolicyTD td;

    public PredictionDemon(RewardFunction rewardFunction, OnPolicyTD td) {
        this.rewardFunction = rewardFunction;
        this.td = td;
    }

    @Override
    public void update(RealVector x_t, A a_t, RealVector x_tp1) {
        td.update(x_t, x_tp1, rewardFunction.reward());
    }

    public double prediction() {
        return td.prediction();
    }

    public RewardFunction rewardFunction() {
        return rewardFunction;
    }

    public OnPolicyTD predicter() {
        return td;
    }

  /*
  @Override
  public String label() {
    return "demon" + Labels.label(rewardFunction);
  }*/

    @Override
    public LinearLearner learner() {
        return td;
    }
}
