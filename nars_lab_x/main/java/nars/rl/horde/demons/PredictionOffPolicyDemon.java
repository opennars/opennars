package nars.rl.horde.demons;

import nars.rl.horde.LinearLearner;
import nars.rl.horde.Policy;
import nars.rl.horde.functions.*;
import org.apache.commons.math3.linear.RealVector;


public class PredictionOffPolicyDemon<A> implements Demon<A> {

    private final RewardFunction rewardFunction;

    private final GVF gtd;

    protected final Policy<A> target;
    protected final Policy<A> behaviour;

    private final OutcomeFunction outcomeFunction;
    private final GammaFunction gammaFunction;

    public PredictionOffPolicyDemon(Policy<A> target, Policy<A> behaviour, GTDLambda gtd, RewardFunction rewardFunction) {
        this(target, behaviour, gtd, rewardFunction, new ConstantGamma(gtd.gamma()), new ConstantOutcomeFunction(0));
    }

    public PredictionOffPolicyDemon(Policy<A> target, Policy<A> behaviour, GVF gtd, RewardFunction rewardFunction,
                                    GammaFunction gammaFunction, OutcomeFunction outcomeFunction) {
        this.rewardFunction = rewardFunction;
        this.gammaFunction = gammaFunction;
        this.outcomeFunction = outcomeFunction;
        this.gtd = gtd;
        this.target = target;
        this.behaviour = behaviour;
    }

    @Override
    public void update(RealVector x_t, A a_t, RealVector x_tp1) {
        double rho_t = a_t != null ? target.pi(a_t) / behaviour.pi(a_t) : 0;
        gtd.update(1, 1, x_t, x_tp1, rewardFunction.reward(), gammaFunction.gamma(), outcomeFunction.outcome());
    }

    public RewardFunction rewardFunction() {
        return rewardFunction;
    }

    public Predictor predicter() {
        return gtd;
    }

    public Policy<A> targetPolicy() {
        return target;
    }

    @Override
    public LinearLearner learner() {
        return gtd;
    }

//  @Override
//  public String label() {
//    return "offpolicyDemon" + Labels.label(rewardFunction);
//  }
}
