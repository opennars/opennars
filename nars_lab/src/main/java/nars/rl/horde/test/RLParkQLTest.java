package nars.rl.horde.test;

import nars.io.Texts;
import nars.rl.horde.Policy;
import nars.rl.horde.QLearningControl;
import nars.rl.horde.functions.GQ;
import nars.rl.horde.functions.TabularAction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;

/**
 * Created by me on 5/3/15.
 */
public class RLParkQLTest {

	public class RandomPolicy implements Policy<Integer> {

		int numOfActions;

		public RandomPolicy(int nActions){
			numOfActions=nActions;
		}

        @Override
        public void update(RealVector x) {

        }

        @Override
		public double pi(Integer s) {
			return 1.0d/numOfActions;
		}

		@Override
		public Integer sampleAction() {
			return null;
		}

	}

    public static double r = 0;

    public static void main(String[] args) {


        Integer[] actions = new Integer[] { 0, 1 };

        int features = 2;

        TabularAction ta = new TabularAction(actions, 1, features);

        final double alpha = .1;
        final double gamma = .99;
        final double lambda = .3;


        GQ gq = new GQ(alpha, 0.0, 1 - gamma, lambda, features);

        QLearningControl.Greedy acting = new QLearningControl.Greedy(gq, actions, ta);

        QLearningControl<Integer> q = new QLearningControl(
            acting,
            new QLearningControl.QLearning(actions, alpha, gamma, lambda, ta, gq.traces())
        );


        ArrayRealVector xt = null;
        int nextA = 0;
        for (int i = 0; i < 1000; i++) {

            double x1 = Math.random();
            double x2 = Math.random();

            System.out.println(Texts.n4(r) + " " + nextA);
            System.out.println(Arrays.toString(gq.traces().vect().toArray()));


            nextA = q.step(xt, nextA, xt = new ArrayRealVector(new double[]{x1, x2}), r);

            r = Math.abs(nextA - x2) - 0.5;

        }

    }
}
