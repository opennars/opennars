package nars.rl.elsy;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.System.arraycopy;
import static nars.rl.elsy.Mat.sigmoidBi;
import static nars.rl.elsy.Mat.sigmoidUni;
import static nars.rl.elsy.Rand.*;



/**
 * Main class of the framework, contains the whole Error Backpropagation algorithm.
 * Takes information from the Perception object and learns on the basis of
 * desired output.  
 * @author Elser
 */
public class ErrorBackpropagationNN implements Serializable{
	private static final long serialVersionUID = 1L;
    private static final double ALPHA_DEFAULT = 0.4;
    private static final double MAX_WEIGHT_DEFAULT = 0.5;
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
	private double[] input;
	private double layerInput[][];
	/**
	 * Neurons' activation values
	 */
	private double activation[][];
	/**
	 * Weight   matrix [layer][i][j]
	 */
	private double w[][][]; // weight   matrix [layer][i][j]
	/**
	 * Weight   matrix [layer][i][j]
	 */
	private double wDelta[][][]; // weight  delta matrix [layer][i][j]
	/**
	 * Gradient matrix [layer][i]
	 */
	private double g[][];
	/**
	 * Learning rate
	 */
	private double alpha;
	/**
	 * Maximum initial weight of neuron connection
	 */
	private double maxWeight;
	
	private int[] neuronsNo;
	private double[][][] wBackup;
	private double[] desiredOutput;
	private double error;
	private double momentum = 0;
	private double[] output;

	/**
	 * @param perception an instance of class extending Perception
	 * @param actionsArray array of actions that can be taken
	 * @param hiddenNeuronsNo numbers of neurons in hidden layers
	 * @param alpha learning rate
	 * @param lambda eligibility traces forgetting rate
	 * @param gamma Q-learning Discount factor
	 * @param maxWeight maximum initial weight of neuron connection
	 */
	public ErrorBackpropagationNN(Perception perception, double[] desiredOutput, int[] hiddenNeuronsNo, double alpha, double maxWeight) {
		this.unipolar = perception.isUnipolar();
		perception.start();
		this.perception = perception;
		this.desiredOutput = desiredOutput;
		this.input = perception.getOutput();
		this.alpha = alpha;
		this.maxWeight = maxWeight;
		neuronsNo = new int[hiddenNeuronsNo.length+1];
            arraycopy(hiddenNeuronsNo, 0, neuronsNo, 0, hiddenNeuronsNo.length);
		neuronsNo[neuronsNo.length-1] = desiredOutput.length;
		activation = createActivationTable(neuronsNo);
		output = activation[activation.length - 1];
		layerInput = createLayerInputs(neuronsNo);
		w = createWeightTable(neuronsNo);
		wDelta = createWeightTable(neuronsNo);
		g = createActivationTable(neuronsNo);
		randomize();
	}

	/**
	 * @param perception - an instance of class implementing Perception
	 * @param actionsArray - array of actions that can be taken
	 * @param hiddenNeuronsNo - numbers of neurons in hidden layers
	 */
	public ErrorBackpropagationNN(Perception perception, double[] desiredOutput, int[] hiddenNeuronsNo) {
		this(
			perception,
			desiredOutput,
			hiddenNeuronsNo,
			ALPHA_DEFAULT,
			MAX_WEIGHT_DEFAULT
		);
	}

	/**
	 * Use this constructor for one-layer neural network.
	 * @param perception - an instance of class implementing Perception
	 * @param actionsArray - array of actions that can be taken
	 */
	public ErrorBackpropagationNN(Perception perception, double[] desiredOutput) {
		this(
				perception,
				desiredOutput,
				new int[] {}	// no hidden layers
		);
	}
	/**
	 * One step of the Q-learning algorithm. Should be invoked at every time step.
	 * It is responsible for selecting the action and updating weights.
	 * DOES NOT execute any action. For this use Brain.execute() method.
	 * @see ErrorBackpropagationNN#executeAction() 
	 */
	public void learn() {
		countGradients();		// g(t)
		updateWeights();		// w(t)
	}
	
	/**
	 * Counts gradients with respect to the chosen action only and
	 * updates all the eligibility traces. See algorithm description
	 * for the details.
	 * @param action
	 */
	private void countGradients() {
		double sumSqrError = 0;
		for (int l = g.length-1; l>=0; l--) {
			for (int i = 0; i < activation[l].length; i++) {
				double error = 0;
				if(l == g.length-1) {
					error = desiredOutput[i] - output[i];
					sumSqrError += error * error;
				} else {
					for (int j = 0; j < activation[l+1].length; j++) {
						error += w[l+1][j][i] * g[l+1][j];
					}
				}
				double activ = activation[l][i];
				if(unipolar) {
					g[l][i] = activ * (1 - activ) * error; //uni
				} else {
					g[l][i] = 0.5 * (1 - activ*activ) * error; //bi
				}
			}
		}
		this.error = sqrt(sumSqrError) / desiredOutput.length;
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
	/**
	 * Gives random weight value
	 * @return random weight value
	 */
	private double randWeight() {
		return d(-maxWeight, maxWeight);
	}
	
	/**
	 * Propagates the input signal throughout the network to the output.
	 * In other words, it updates the activations of all the neurons.
	 */
	public void propagate() {
		double weightedSum = 0;
		double wli[];
		for (int l = 0; l < w.length; l++) {
			for (int i = 0; i < w[l].length; i++) {
				weightedSum = 0;
				wli = w[l][i];
				for (int j = 0; j < wli.length; j++) {
					weightedSum += wli[j] * layerInput[l][j];
				}
				if(unipolar) {
					activation[l][i] = sigmoidUni(weightedSum);
				} else {
					activation[l][i] = sigmoidBi(weightedSum);
				}
			}
		}
	}
	
	/**
	 * Used to teach the neural network. Updates all the weights
	 * basing on eligibility traces and the change value.
	 * @param change
	 */
	private void updateWeights() {
		for (int l = w.length-1; l >= 0; l--) {
			for (int i = 0; i < w[l].length; i++) {
				for (int j = 0; j < w[l][i].length; j++) {
					wDelta[l][i][j] = alpha * g[l][i] * layerInput[l][j] + momentum * wDelta[l][i][j];
					w[l][i][j] += wDelta[l][i][j];
				}
			}
		}
	}

	/**
	 * Mutates the neural network by given percent.
	 * Usually it is not used in the algorithm, however you may want use it,
	 * if you implement a genetic algorithm.
	 * @param percent
	 */
	public void mutate(double percent) {
		for (int l = 0; l < w.length; l++) {
			for (int i = 0; i < w[l].length; i++) {
				for (int j = 0; j < w[l][i].length; j++) {
					if(successWithPercent(percent)) {
						w[l][i][j] = randWeight();
					}
				}
			}
		}
	}
	

	public void inheritFrom(ErrorBackpropagationNN father, ErrorBackpropagationNN mother) {
		for (int l = 0; l < w.length; l++) {
			for (int i = 0; i < w[l].length; i++) {
				for (int j = 0; j < w[l][i].length; j++) {
					if(mother==null || b()) {
						w[l][i][j] = father.w[l][i][j];
					} else {
						w[l][i][j] = mother.w[l][i][j];
					}
				}
			}
		}
	}
	/**
	 * Resets the gradients and eligibility traces. Should be called everytime before 
	 * the new learning episode starts.
	 */
	public void reset() {
		for (int l = 0; l < w.length; l++) {
			for (int i = 0; i < w[l].length; i++) {
				g[l][i] = 0; 
			}
		}
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getMaxWeight() {
		return maxWeight;
	}

	public void setMaxWeight(double maxWeight) {
		this.maxWeight = maxWeight;
	}

	public boolean isUnipolar() {
		return unipolar;
	}

	public void setUnipolar(boolean unipolar) {
		this.unipolar = unipolar;
	}

	/**
	 * Method allocating input arrays for all the NN layers
	 * @param neuronsNo
	 * @return
	 */
	private double[][] createLayerInputs(int[] neuronsNo) {
		double[][] ret = new double[neuronsNo.length][];
		for (int l = 0; l < neuronsNo.length; l++) {
			if(l==0) {
				ret[l] = input;
			} else {
				ret[l] = activation[l-1];
			}
		}
		return ret;
	}
	/**
	 * Method allocating neuron activation values' arrays
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
	 * @return 
	 */
	public double getMaxW() {
		double ret = 0.0;
		int no=0;
		for (int l = 0; l < w.length; l++) {
			for (int i = 0; i < w[l].length; i++) {
				for (int j = 0; j < w[l][i].length; j++) {
					ret += abs(w[l][i][j]);
					no++;
				}
			}
		}
		return ret/no;
	}

	public void backup() {
		if(wBackup==null) {
			wBackup = createWeightTable(neuronsNo);
		}
		for (int l = 0; l < w.length; l++) {
			for (int i = 0; i < w[l].length; i++) {
                            arraycopy(w[l][i], 0, wBackup[l][i], 0, w[l][i].length);
			}
		}
	}

	public void restore() {
		if(wBackup!=null) {
			for (int l = 0; l < w.length; l++) {
				for (int i = 0; i < w[l].length; i++) {
                                    arraycopy(wBackup[l][i], 0, w[l][i], 0, w[l][i].length);
				}
			}
		}
	}

	public void set(ErrorBackpropagationNN brain) {
		for (int l = 0; l < w.length; l++) {
			for (int i = 0; i < w[l].length; i++) {
                            arraycopy(brain.w[l][i], 0, w[l][i], 0, w[l][i].length);
			}
		}
	}

	public double[] getInput() {
		return input;
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
		w = (double[][][])in.readObject();
		in.close();
	}

	public Perception getPerception() {
		return perception;
	}

	public double getError() {
		return error;
	}

	public double[] getDesiredOutput() {
		return desiredOutput;
	}

	public void setDesiredOutput(double[] desiredOutput) {
		this.desiredOutput = desiredOutput;
	}

	public double getMomentum() {
		return momentum;
	}

	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	public double[] getOutput() {
		return output;
	}
}
