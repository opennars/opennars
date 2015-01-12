package nars.rl.elsy;

import java.io.Serializable;

import static nars.rl.elsy.Mat.sigmoidBi;
import static nars.rl.elsy.Mat.sigmoidUni;
import static nars.rl.elsy.Rand.d;


/**
 * Used to prepare input for the Brain.
 * @author Elser
 */
public abstract class Perception implements Serializable{
	private static final double NEURON_ON = 0.85;
	private static final long serialVersionUID = 1;
        boolean rawInput = false;
	protected double[] output;
	private int outputIter;
	private boolean addRandomInput = false;

	public abstract boolean isUnipolar();
	/**
	 * This method must be implemented in your class extending this class.
	 * <UL> It should return:
	 * <LI> a positive value (0..1) any time you want to reward the
	 * agent for the actions he has taken (ex. finding food, reaching goal)
	 * <LI> a negative value (-1..0) if you want to punish the agent (ex. for collisions)
	 */
	public abstract double getReward();
	/**
	 * This method must be implemented in your class extending this class.
	 * Here you set all the input values coming from the agent's sensors.
	 * This method shall invoke the setNextValue(double d) method for each
	 * input with the input value as a parameter. In other words, setNextValue should
	 * be called as many times as the number of the agent sensors.
	 */
	protected abstract void updateInputValues();
	/**
	 * Prepares input for the brain by invoking setStateValues() method.
	 * Should be invoked everytime before calling Brain.count() method.
	 * @see Brain#count() 
	 */
	public void perceive() {
		outputIter = 0;
		if (addRandomInput) {
			setNextValue(d(-2, 2)); // random value
		}
		updateInputValues();
	}
	
	/**
	 * This method is called by the Brain constructor and shall not be used anywhere else.
	 * It counts the size of the array of input values.
	 */
	public void start() {
		if(this.output==null) {
			perceive();
			this.output = new double[outputIter];
		}
	}
	
	/**
	 * Sets the value of the next input. Invoked by setStateValues() method.
	 * @param input
	 */
	protected void setNextValue(double input) {
		if(output!=null) { 
                    if (rawInput)
                        output[outputIter] = input;
                    else {
			if(isUnipolar()) {
				output[outputIter] = sigmoidUni(input);
			} else {
				output[outputIter] = sigmoidBi(input);
			}
                    }
		}
		outputIter++;
	}
	
	/**
	 * @return	perception input values array
	 */
	public double[] getOutput() {
		return output;
	}
	protected void setNextValue(boolean b) {
		if(output!=null) {
			if(isUnipolar()) {
				output[outputIter] = b ? NEURON_ON : 1 - NEURON_ON;
			} else {
				output[outputIter] = b ? NEURON_ON : -NEURON_ON;
			}
		}
		outputIter++;
	}
	
	protected void setNextValueAbsolute(double d) {
		if(output!=null) {
			output[outputIter] = d;
		}
		outputIter++;
	}
	
	
	public boolean isAddRandomInput() {
		return addRandomInput;
	}
	public void setAddRandomInput(boolean addRandomInput) {
		this.addRandomInput = addRandomInput;
	}
}
