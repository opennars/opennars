package nars.rl.horde;

import nars.rl.horde.demons.Predictor;
import nars.rl.horde.functions.StateToStateAction;
import nars.rl.horde.functions.Traces;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by me on 5/3/15.
 */

public class QLearningControl<A> implements HordeAgent.ControlLearner<A>, HordeAgent.OffPolicyLearner<A> {

    public static class Greedy<A> implements Policy<A> /*implements DiscreteActionPolicy, PolicyPrototype*/ {
        private static final long serialVersionUID = 1675962692054005355L;
        protected final StateToStateAction toStateAction;
        protected final Predictor predictor;
        protected final A[] actions;
        protected final double[] actionValues;
        protected A bestAction;
        private double bestValue;

        public Greedy(Predictor predictor, A[] actions, StateToStateAction toStateAction) {
            this.toStateAction = toStateAction;
            this.predictor = predictor;
            this.actions = actions;
            actionValues = new double[actions.length];
        }

        @Override
        public A sampleAction() {
            return bestAction;
        }

        @Override
        public void update(RealVector x_tp1) {
            updateActionValues(x_tp1);
            findBestAction();
        }

        private void findBestAction() {
            bestValue = actionValues[0];
            bestAction = actions[0];
            for (int i = 1; i < actions.length; i++) {
                double value = actionValues[i];
                if (value > bestValue) {
                    bestValue = value;
                    bestAction = actions[i];
                }
            }
        }

        private void updateActionValues(RealVector s_tp1) {
            for (int i = 0; i < actions.length; i++) {
                RealVector phi_sa = toStateAction.stateAction(s_tp1, actions[i]);
                actionValues[i] = predictor.predict(phi_sa);
            }
        }

        @Override
        public double pi(A a) {
            return a == bestAction ? 1 : 0;
        }

        public StateToStateAction toStateAction() {
            return toStateAction;
        }

        public A bestAction() {
            return bestAction;
        }

        public double bestActionValue() {
            return bestValue;
        }

        public double[] values() {
            return actionValues;
        }

        public A[] actions() {
            return actions;
        }

        public Policy duplicate() {
            throw new RuntimeException("not impl");
            //return new Greedy(predictor, actions, Utils.clone(toStateAction));
        }
    }

    public static class QLearning<A> implements Predictor, LinearLearner /*, EligibilityTraceAlgorithm*/ {

        protected final RealVector theta;
        private final Traces e;
        private final double lambda;
        private final double gamma;
        private final double alpha;
        private final StateToStateAction<A> toStateAction;
        private double delta;
        private final Greedy<A> greedy;

        public QLearning(A[] actions, double alpha, double gamma, double lambda, StateToStateAction toStateAction,
                         Traces prototype) {
            this.alpha = alpha;
            this.gamma = gamma;
            this.lambda = lambda;
            this.toStateAction = toStateAction;
            greedy = new Greedy(this, actions, toStateAction);
            theta = new ArrayRealVector(toStateAction.vectorSize());
            e = prototype.newTraces(toStateAction.vectorSize());
        }

        public double update(RealVector x_t, A a_t, RealVector x_tp1, A a_tp1, double r_tp1) {
            if (x_t == null)
                return initEpisode();
            greedy.update(x_t);
            A at_star = greedy.bestAction();
            greedy.update(x_tp1);
            RealVector phi_sa_t = toStateAction.stateAction(x_t, a_t);
            delta = r_tp1 + gamma * greedy.bestActionValue() - theta.dotProduct(phi_sa_t);
            if (a_t == at_star)
                e.update(gamma * lambda, phi_sa_t);
            else {
                e.clear();
                e.update(0, phi_sa_t);
            }
            theta.combineToSelf(1, alpha * delta, e.vect());
            return delta;
        }

        private double initEpisode() {
            if (e != null)
                e.clear();
            delta = 0.0;
            return delta;
        }

        @Override
        public double predict(RealVector phi_sa) {
            return theta.dotProduct(phi_sa);
        }

        public RealVector theta() {
            return theta;
        }

        @Override
        public void resetWeight(int index) {
            theta.setEntry(index, 0);
        }

        @Override
        public RealVector weights() {
            return theta;
        }

        @Override
        public double error() {
            return delta;
        }

        public Policy greedy() {
            return greedy;
        }

        public Traces traces() {
            return e;
        }
    }

    private static final long serialVersionUID = 5784749108581105369L;
    private final QLearning qlearning;
    private final Policy<A> behaviour;

    public QLearningControl(Policy<A> acting, QLearning qlearning) {
        this.qlearning = qlearning;
        this.behaviour = acting;
    }

    @Override
    public A step(RealVector x_t, A a_t, RealVector x_tp1, double r_tp1) {
        A a_tp1 = Policy.decide(behaviour, x_tp1);
        learn(x_t, a_t, x_tp1, a_tp1, r_tp1);
        return a_tp1;
    }

    @Override
    public Policy<A> policy() {
        return behaviour;
    }

    @Override
    public void learn(RealVector x_t, A a_t, RealVector x_tp1, A a_tp1, double r_tp1) {
        qlearning.update(x_t, a_t, x_tp1, a_tp1, r_tp1);
    }

    @Override
    public A proposeAction(RealVector x) {
        return Policy.decide(behaviour, x);
    }

    @Override
    public Policy targetPolicy() {
        throw new RuntimeException("not impl");
    }

    @Override
    public QLearning predictor() {
        return qlearning;
    }
}
