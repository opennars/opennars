package nars.rl.elsy;

import nars.rl.elsy.QLearner.Action;


/**
 * Main class of the framework, contains the whole Connectionist Q-learning
 * algorithm. Takes information from the Perception object and executes one of
 * the Actions.
 *
 * @author Elser
 */
public class Brain implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Neuron activation function mode
     */
    private boolean unipolar = true;
    /**
     * An instance of class extending Perception
     */
    private Perception perception;
    /**
     * Array of actions that can be taken
     */
    private Action[] actionsArray;
    private double[] input;
    private double[] Q;
    private double layerInput[][];
    /**
     * Neurons' activation values
     */
    private double activation[][];
    /**
     * Weight matrix [layer][i][j]
     */
    private double w[][][]; // weight   matrix [layer][i][j]
    /**
     * Eligibility traces matrix [layer][i][j]
     */
    private double e[][][]; // eligibility traces matrix [layer][i][j]
    /**
     * Gradient matrix [layer][i]
     */
    private double g[][];
    /**
     * Learning rate
     */
    private double alpha;
    private static final double ALPHA_DEFAULT = 0.1;
    /**
     * Eligibility traces forgetting rate
     */
    private double lambda;
    private static final double LAMBDA_DEFAULT = 0.9;
    /**
     * Q-learning Discount factor
     */
    private double gamma;
    private static final double GAMMA_DEFAULT = 0.9;
    /**
     * Maximum initial weight of neuron connection
     */
    private double maxWeight;
    private static final double MAX_WEIGHT_DEFAULT = 1.0;

    /**
     * Probability with which random action is selected instead of being
     * selected by the NN
     */
    private double randActions;
    private static final double RAND_ACTIONS_DEFAULT = 0.0;
    /**
     * Use Boltzmann probability (instead of maximum Q-value)
     */
    private boolean useBoltzmann;
    private static final boolean USE_BOLTZMANN_DEFAULT = false;
    /**
     * Boltzmann temperature
     */
    private double temperature;
    private static final double TEMPERATURE_DEFAULT = 0.002;

    /**
     * Maximal current Q-value
     */
    private double Qmax;
    /**
     * Q-value of previous action
     */
    private double QPrev;
    /**
     * Array used to calculate Boltzmann probability
     */
    private double boltzValues[];

    private int tactCounter;
    private int a;
    private int executionResult;
    private int[] neuronsNo;
    private double[][][] wBackup;

    /**
     * @param perception an instance of class extending Perception
     * @param actionsArray array of actions that can be taken
     * @param hiddenNeuronsNo numbers of neurons in hidden layers
     * @param alpha learning rate
     * @param lambda eligibility traces forgetting rate
     * @param gamma Q-learning Discount factor
     * @param maxWeight maximum initial weight of neuron connection
     */
    public Brain(Perception perception, Action[] actionsArray, int[] hiddenNeuronsNo, double alpha, double lambda, double gamma, boolean useBoltzmann, double temperature, double randActions, double maxWeight) {
        this.unipolar = perception.isUnipolar();
        perception.start();
        this.perception = perception;
        this.input = perception.getOutput();
        this.actionsArray = actionsArray;
        this.alpha = alpha;
        this.lambda = lambda;
        this.gamma = gamma;
        this.useBoltzmann = useBoltzmann;
        this.temperature = temperature;
        this.randActions = randActions;
        this.maxWeight = maxWeight;
        neuronsNo = new int[hiddenNeuronsNo.length + 1];
        System.arraycopy(hiddenNeuronsNo, 0, neuronsNo, 0, hiddenNeuronsNo.length);
        neuronsNo[neuronsNo.length - 1] = actionsArray.length;
        activation = createActivationTable(neuronsNo);
        layerInput = createLayerInputs(neuronsNo);
        w = createWeightTable(neuronsNo);
        e = createWeightTable(neuronsNo);
        g = createActivationTable(neuronsNo);
        Q = activation[activation.length - 1];
        boltzValues = new double[Q.length];
        randomize();
        tactCounter = 0;
    }

    /**
     * @param perception - an instance of class implementing Perception
     * @param actionsArray - array of actions that can be taken
     * @param hiddenNeuronsNo - numbers of neurons in hidden layers
     */
    public Brain(Perception perception, Action[] actionsArray, int[] hiddenNeuronsNo) {
        this(
                perception,
                actionsArray,
                hiddenNeuronsNo,
                ALPHA_DEFAULT,
                LAMBDA_DEFAULT,
                GAMMA_DEFAULT,
                USE_BOLTZMANN_DEFAULT,
                TEMPERATURE_DEFAULT,
                RAND_ACTIONS_DEFAULT,
                MAX_WEIGHT_DEFAULT
        );
    }

    /**
     * Use this constructor for one-layer neural network.
     *
     * @param perception - an instance of class implementing Perception
     * @param actionsArray - array of actions that can be taken
     */
    public Brain(Perception perception, Action[] actionsArray) {
        this(
                perception,
                actionsArray,
                new int[]{} // no hidden layers
        );
    }

    /**
     * One step of the Q-learning algorithm. Should be invoked at every time
     * step. It is responsible for selecting the action and updating weights.
     * DOES NOT execute any action. For this use Brain.execute() method.
     *
     * @see Brain#executeAction()
     */
    public void count() {
        a = selectAction();
        if (tactCounter > 0) {
            double r = perception.getReward();		// r(t-1)
            double error = r + gamma * Qmax - QPrev;
            updateWeights(error);		// w(t)
        }
        propagate();
        countEligibilities(a);		// e(t), g(t)
        tactCounter++;
        QPrev = Q[a];
    }

    /**
     * Selects an action to execute, basing on the values of Q-function.
     *
     * @return	number of the selected action
     */
    private int selectAction() {
        int a = -1;
        Qmax = -1;
        propagate();
        for (int i = 0; i < Q.length; i++) {
            if (useBoltzmann) {
                boltzValues[i] = Math.exp(Q[i] / temperature);
            }
            if (Qmax < Q[i]) {
                a = i;
                Qmax = Q[a];
            }
        }
        //int aMax = a;
        if (useBoltzmann) {
            a = RR.pickBestIndex(boltzValues);
        }
        /*if(a != aMax) {
         String qstr = "";
         for (int i = 0; i < Q.length; i++) {
         qstr += ", " + Q[i];
         }
         System.out.println("a(" + a + ") != aMax(" + aMax + ") " + qstr);
         }*/
        if (randActions != 0 && Rand.successWithPercent(randActions)) {
            a = Rand.randInt(Q.length);
        }
        Qmax = Q[a];
        return a;
    }

    /**
     * Counts gradients with respect to the chosen action only and updates all
     * the eligibility traces. See algorithm description for the details.
     *
     * @param action
     */
    private void countEligibilities(int action) {
        for (int l = g.length - 1; l >= 0; l--) {
            for (int i = 0; i < activation[l].length; i++) {
                double error = 0;
                if (l == g.length - 1) {
                    error = (i == action) ? 1 : 0;
                } else {
                    for (int j = 0; j < activation[l + 1].length; j++) {
                        error += w[l + 1][j][i] * g[l + 1][j];
                    }
                }
                double activ = activation[l][i];
                if (unipolar) {
                    g[l][i] = activ * (1 - activ) * error; //uni
                } else {
                    g[l][i] = 0.5 * (1 - activ * activ) * error; //bi
                }
                double gli = g[l][i];
                for (int j = 0; j < w[l][i].length; j++) {
                    e[l][i][j] = gamma * lambda * e[l][i][j] + gli * layerInput[l][j];
                }
            }
        }
    }

    /**
     * Randomizes all the weights of neurons' connections.
     */
    public void randomize() {
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                for (int j = 0; j < w[l][i].length; j++) {
                    w[l][i][j] = randWeight();
                }
            }
        }
    }

    public void inheritFrom(Brain brain, Brain brain2) {
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                System.arraycopy(brain.w[l][i], 0, w[l][i], 0, w[l][i].length);
            }
        }
    }

    /**
     * Gives random weight value
     *
     * @return random weight value
     */
    private double randWeight() {
        return Rand.d(-maxWeight, maxWeight);
    }

    /**
     * Propagates the input signal throughout the network to the output. In
     * other words, it updates the activations of all the neurons.
     */
    private void propagate() {
        double weightedSum = 0;
        double wli[];
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                weightedSum = 0;
                wli = w[l][i];
                for (int j = 0; j < wli.length; j++) {
                    weightedSum += wli[j] * layerInput[l][j];
                }
                if (unipolar) {
                    activation[l][i] = Mat.sigmoidUni(weightedSum);
                } else {
                    activation[l][i] = Mat.sigmoidBi(weightedSum);
                }
            }
        }
    }

    /**
     * Used to teach the neural network. Updates all the weights basing on
     * eligibility traces and the change value.
     *
     * @param change
     */
    private void updateWeights(final double change) {
        for (int l = w.length - 1; l >= 0; l--) {
            for (int i = 0; i < w[l].length; i++) {
                for (int j = 0; j < w[l][i].length; j++) {
                    w[l][i][j] += (alpha * change * e[l][i][j]);
                }
            }
        }
    }

    /**
     * Mutates the neural network by given percent. Usually it is not used in
     * the algorithm, however you may want use it, if you implement a genetic
     * algorithm.
     *
     * @param percent
     */
    public void mutate(final double percent) {
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                for (int j = 0; j < w[l][i].length; j++) {
                    if (Rand.successWithPercent(percent)) {
                        w[l][i][j] = randWeight();
                    }
                }
            }
        }
    }

    public void printStats() {
        double maxWght = 0;
        double avgWght = 0;
        int wghtNo = 0;
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                for (int j = 0; j < w[l][i].length; j++) {
                    double wabs = Math.abs(w[l][i][j]);
                    if (maxWght < wabs) {
                        maxWght = wabs;
                    }
                    avgWght += wabs;
                    wghtNo++;
                }
            }
        }
        avgWght /= wghtNo;
        System.out.println("Max=" + maxWght + " Avg=" + avgWght + " No=" + wghtNo);
    }

    /**
     * Resets the gradients and eligibility traces. Should be called everytime
     * before the new learning episode starts.
     */
    public void reset() {
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                for (int j = 0; j < w[l][i].length; j++) {
                    e[l][i][j] = 0;
                }
                g[l][i] = 0;
            }
        }
        tactCounter = 0;
    }

    /**
     * Returns the index of the selected action
     */
    public int getAction() {
        return a;
    }

    /**
     * Returns the result of Action.execute() method of previously executed
     * action.
     *
     * @see Action#execute()
     */
    public int getExecutionResult() {
        return executionResult;
    }

    public double[] getOutput() {
        return Q;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public boolean isUseBoltzmann() {
        return useBoltzmann;
    }

    public void setUseBoltzmann(boolean useBoltzmann) {
        this.useBoltzmann = useBoltzmann;
    }

    public boolean isUnipolar() {
        return unipolar;
    }

    public void setUnipolar(boolean unipolar) {
        this.unipolar = unipolar;
    }

    public double getRandActions() {
        return randActions;
    }

    public void setRandActions(double randActions) {
        this.randActions = randActions;
    }

    /**
     * Method allocating input arrays for all the NN layers
     *
     * @param neuronsNo
     * @return
     */
    private double[][] createLayerInputs(int[] neuronsNo) {
        double[][] ret = new double[neuronsNo.length][];
        for (int l = 0; l < neuronsNo.length; l++) {
            if (l == 0) {
                ret[l] = input;
            } else {
                ret[l] = activation[l - 1];
            }
        }
        return ret;
    }

    /**
     * Method allocating neuron activation values' arrays
     *
     * @param neuronsNo
     * @return
     */
    private double[][] createActivationTable(int[] neuronsNo) {
        double[][] ret = new double[neuronsNo.length][];
        for (int l = 0; l < ret.length; l++) {
            ret[l] = new double[neuronsNo[l]];
        }
        return ret;
    }

    /**
     * Method allocating neuron weights' arrays
     *
     * @param neuronsNo
     * @return
     */
    private double[][][] createWeightTable(int[] neuronsNo) {
        double[][][] ret = new double[neuronsNo.length][][];
        for (int l = 0; l < ret.length; l++) {
            ret[l] = new double[neuronsNo[l]][layerInput[l].length];
        }
        return ret;
    }

    /**
     * Returns the maximal absolute value of all the weights
     *
     * @return
     */
    public double getMaxW() {
        double ret = 0.0;
        int no = 0;
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                for (int j = 0; j < w[l][i].length; j++) {
                    ret += Math.abs(w[l][i][j]);
                    no++;
                }
            }
        }
        return ret / no;
    }

    public void backup() {
        if (wBackup == null) {
            wBackup = createWeightTable(neuronsNo);
        }
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                System.arraycopy(w[l][i], 0, wBackup[l][i], 0, w[l][i].length);
            }
        }
    }

    public void restore() {
        if (wBackup != null) {
            for (int l = 0; l < w.length; l++) {
                for (int i = 0; i < w[l].length; i++) {
                    System.arraycopy(wBackup[l][i], 0, w[l][i], 0, w[l][i].length);
                }
            }
        }
    }

    public void set(Brain brain) {
        for (int l = 0; l < w.length; l++) {
            for (int i = 0; i < w[l].length; i++) {
                System.arraycopy(brain.w[l][i], 0, w[l][i], 0, w[l][i].length);
            }
        }
    }

    public double[] getInput() {
        return input;
    }

    public double[][][] getE() {
        return e;
    }

    public double[][] getG() {
        return g;
    }

    public double[][][] getW() {
        return w;
    }

    public double[][] getActivation() {
        return activation;
    }

    public void setW(double[][][] w) {
        this.w = w;
    }

    public void save(String filename) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(w);
        out.close();
    }

    public void load(String filename) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
        w = (double[][][]) in.readObject();
        in.close();
    }

    public Perception getPerception() {
        return perception;
    }
}
