package nars.rl.curiosity;

import nars.rl.elsy.Perception;


/**
 * Perception feeding Curiosity neural network. Provides information for the
 * ErrorBackpropagationNN that tries to predict player's perception output in
 * the next step.
 */
public class CuriosityPerc extends Perception {
	private static final long serialVersionUID = 1L;

	private final Perception perception;

	private final CuriousBrain brain;

	public CuriosityPerc(Perception perception, CuriousBrain brain) {
		this.perception = perception;
		this.brain = brain;
	}

        @Override
	public double getReward() {
		return 0;
	}

        @Override
	public boolean isUnipolar() {
		return this.perception.isUnipolar();
	}

        @Override
	protected void updateInputValues() {
		double[] percOut = this.perception.getOutput();
		for (int i = 1; i < percOut.length; i++) {
			setNextValueAbsolute(percOut[i]);
		}
		setNextValue(this.perception.getReward());
		int action = brain.getAction();
		for (int i = 0; i < brain.getOutput().length; i++) {
			setNextValue(i == action);
		}
	}

}
