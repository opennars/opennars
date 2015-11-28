package nars.rl.horde.demons;

import nars.rl.horde.functions.RewardFunction;

import java.io.Serializable;

public class PredictionDemonVerifier<A> implements Serializable {
    private static final long serialVersionUID = 6127406364376542150L;
    private final PredictionDemon<A> predictionDemon;
    private final RewardFunction rewardFunction;
    private final TDErrorMonitor errorMonitor;

    public static class TDErrorMonitor implements Serializable {
        private static final long serialVersionUID = 6441800170099052600L;
        private final int bufferSize;
        private final double[] gammas;
        private final double[] predictionHistory;
        private final double[] observedHistory;
        private int current;
        private boolean cacheFilled;
        private double error;
        private double prediction, observed;
        private boolean errorComputed;
        private final double precision;
        private final double gamma;

        public TDErrorMonitor(double gamma, double precision) {
            this.gamma = gamma;
            this.precision = precision;
            bufferSize = computeBufferSize(gamma, precision);
            predictionHistory = new double[bufferSize];
            observedHistory = new double[bufferSize];
            gammas = new double[bufferSize];
            for (int i = 0; i < gammas.length; i++)
                gammas[i] = Math.pow(gamma, i);
            current = 0;
            cacheFilled = false;
        }

        static public int computeBufferSize(double gamma, double precision) {
            return gamma > 0 ? (int) Math.ceil(Math.log(precision) / Math.log(gamma)) : 1;
        }

        private void reset() {
            current = 0;
            cacheFilled = false;
            errorComputed = false;
            error = 0;
            prediction = 0;
            observed = 0;
        }

        public double update(double prediction_t, double reward_tp1, boolean endOfEpisode) {
            if (endOfEpisode) {
                reset();
                return 0.0;
            }
            if (cacheFilled) {
                errorComputed = true;
                prediction = predictionHistory[current];
                observed = observedHistory[current];
                error = observed - prediction;
            }
            observedHistory[current] = 0;
            for (int i = 0; i < bufferSize; i++)
                observedHistory[(current - i + bufferSize) % bufferSize] += reward_tp1 * gammas[i];
            predictionHistory[current] = prediction_t;
            updateCurrent();
            return error;
        }

        protected void updateCurrent() {
            current++;
            if (current >= bufferSize) {
                cacheFilled = true;
                current = 0;
            }
        }

        public double error() {
            return error;
        }

        public boolean errorComputed() {
            return errorComputed;
        }

        public double precision() {
            return precision;
        }

        public double returnValue() {
            return observed;
        }

        public double gamma() {
            return gamma;
        }

        public int bufferSize() {
            return bufferSize;
        }

        public double prediction() {
            return prediction;
        }
    }


    public PredictionDemonVerifier(double gamma, PredictionDemon<A> predictionDemon) {
        this(gamma, predictionDemon, 0.01);
    }

    public PredictionDemonVerifier(double gamma, PredictionDemon<A> predictionDemon, double precision) {
        this.predictionDemon = predictionDemon;
        rewardFunction = predictionDemon.rewardFunction();
        errorMonitor = new TDErrorMonitor(gamma, precision);
    }

//    public PredictionDemonVerifier(PredictionDemon predictionDemon) {
//        this(extractGamma(predictionDemon.predicter()), predictionDemon);
//    }
//
//    static public double extractGamma(OnPolicyTD learner) {
//        if (learner instanceof TD)
//            return ((TD) learner).gamma();
//        if (learner instanceof TDLambdaAutostep)
//            return ((TDLambdaAutostep) learner).gamma();
//        throw new NotImplemented();
//    }

    public TDErrorMonitor errorMonitor() {
        return errorMonitor;
    }

    public double update(boolean endOfEpisode) {
        return errorMonitor.update(predictionDemon.prediction(), rewardFunction.reward(), endOfEpisode);
    }
}
