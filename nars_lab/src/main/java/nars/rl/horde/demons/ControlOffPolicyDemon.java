package nars.rl.horde.demons;


import nars.rl.horde.LinearLearner;
import nars.rl.horde.Policy;
import nars.rl.horde.functions.*;
import org.apache.commons.math3.linear.RealVector;

public class ControlOffPolicyDemon<A> implements Demon<A> {
    private static final long serialVersionUID = -7997723890930214800L;
    private final RewardFunction rewardFunction;
    private final OutcomeFunction outcomeFunction;
    private final GreedyGQ<A> gq;
    private final GammaFunction gammaFunction;

    public ControlOffPolicyDemon(RewardFunction rewardFunction, final GreedyGQ<A> gq) {
        this(gq, rewardFunction, new ConstantGamma(gq.gamma()), new ConstantOutcomeFunction(0));
    }

    public ControlOffPolicyDemon(GreedyGQ<A> gq, RewardFunction rewardFunction, GammaFunction gammaFunction,
                                 OutcomeFunction outcomeFunction) {
        this.rewardFunction = rewardFunction;
        this.gq = gq;
        this.outcomeFunction = outcomeFunction;
        this.gammaFunction = gammaFunction;
    }

    @Override
    public void update(RealVector x_t, A a_t, RealVector x_tp1) {
        gq.update(x_t, a_t, rewardFunction.reward(), gammaFunction.gamma(), outcomeFunction.outcome(), x_tp1, a_t);
    }

    public RewardFunction rewardFunction() {
        return rewardFunction;
    }

    public OutcomeFunction outcomeFunction() {
        return outcomeFunction;
    }

    public Predictor predictor() {
        return gq.predictor();
    }

    public Policy<A> targetPolicy() {
        return gq.targetPolicy();
    }

    @Override
    public LinearLearner learner() {
        return gq.gq();
    }
}
