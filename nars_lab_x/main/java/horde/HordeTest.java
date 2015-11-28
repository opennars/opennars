package nars.rl.horde.test;

import nars.rl.horde.LinearLearner;
import nars.rl.horde.demons.Demon;
import nars.rl.horde.demons.PredictionDemon;
import nars.rl.horde.demons.PredictionDemonVerifier;
import nars.rl.horde.functions.RewardFunction;
import nars.rl.horde.functions.TD;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Assert;
import org.junit.Test;


public class HordeTest {

    interface RewardFunctionTest extends RewardFunction {
        void update(int time);
    }

    static final private RewardFunction RewardFunction01 = new RewardFunctionTest() {
        @Override
        public double reward() {
            return 2.0;
        }

        @Override
        public void update(int time) {
        }
    };

    static class CustomRewardFunction implements RewardFunctionTest {
        private final int bufferSize;
        private double reward;

        public CustomRewardFunction(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        @Override
        public void update(int time) {
            reward = (time % bufferSize) * 100 + 100;
        }

        @Override
        public double reward() {
            return reward;
        }
    }

    interface TimeToState {
        RealVector get(int time);
    }

    private final TimeToState noState = new TimeToState() {
        @Override
        public RealVector get(int time) {
            return new ArrayRealVector(new double[]{1.0});
        }
    };

    @Test
    public void testPredictionDemon() {
        TD td = new TD(0.0, 0.1, 1);
        PredictionDemon predictionDemon = new PredictionDemon(RewardFunction01, td);
        PredictionDemonVerifier verifier = new PredictionDemonVerifier(td.gamma(), predictionDemon);
        runExperiment(predictionDemon, verifier);
        Assert.assertEquals(RewardFunction01.reward(), predictionDemon.prediction(), 1.0);
    }

    private void runExperiment(PredictionDemon predictionDemon, PredictionDemonVerifier verifier) {
        runExperiment(predictionDemon, verifier, noState, 1000);
    }

    protected void runExperiment(PredictionDemon predictionDemon, PredictionDemonVerifier demonVerifier,
                                 TimeToState timeToState, int maxStep) {
        RealVector x_t = null;
        int time = 0;
        PredictionDemonVerifier.TDErrorMonitor verifier = demonVerifier.errorMonitor();
        while (!verifier.errorComputed() || Math.abs(verifier.error()) >= verifier.precision()) {
            RealVector x_tp1 = timeToState.get(time);
            ((RewardFunctionTest) predictionDemon.rewardFunction()).update(time);
            predictionDemon.update(x_t, null, x_tp1);
            demonVerifier.update(false);
            x_t = x_tp1;
            time++;
            Assert.assertTrue(time < maxStep);
        }
    }

    @Test
    public void testPredictionDemonGamma09() {
        double gamma = 0.9;
        TD td = new TD(gamma, 0.1, 1);
        PredictionDemon predictionDemon = new PredictionDemon(RewardFunction01, td);
        PredictionDemonVerifier verifier = new PredictionDemonVerifier(td.gamma(), predictionDemon);
        runExperiment(predictionDemon, verifier);
        Assert.assertEquals(RewardFunction01.reward() / (1 - gamma), predictionDemon.prediction(), 1.0);
    }

    @Test
    public void testPredictionDemonGamma09MultipleState() {
        final int bufferSize = 50;
        double gamma = 0.9;
        TD td = new TD(gamma, 0.1, bufferSize);
        CustomRewardFunction rewardFunction = new CustomRewardFunction(bufferSize);
        PredictionDemon predictionDemon = new PredictionDemon(rewardFunction, td);
        PredictionDemonVerifier verifier = new PredictionDemonVerifier(td.gamma(), predictionDemon);
        TimeToState timeToState = new TimeToState() {
            @Override
            public RealVector get(int time) {
                RealVector r = new ArrayRealVector(bufferSize);
                r.setEntry(time % bufferSize, 1);
                return r;
            }
        };
        runExperiment(predictionDemon, verifier, timeToState, 1000 * bufferSize);
    }

    static class TestDemon<Action> implements Demon<Action> {
        int nbUpdate = 0;

        @Override
        public void update(RealVector x_t, Action a_t, RealVector x_tp1) {
            nbUpdate++;
        }

        @Override
        public LinearLearner learner() {
            return null;
        }
    }


//
//  @Test
//  public void testSerialization() {
//    Horde horde = new Horde(1);
//    horde.addDemon(new TestDemon());
//    horde.update(null, null, null, null);
//    horde.update(null, null, null, null);
//    horde = null;
//    horde.update(null, null, null, null);
//    horde.update(null, null, null, null);
//  }

}
