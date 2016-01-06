/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.menu;

import jurls.core.LearnerAndActor;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.ParameterizedFunctionGenerator;
import jurls.core.brain.Brain;
import jurls.core.reinforcementlearning.*;

import javax.swing.*;

/**
 * 
 * @author thorsten2
 */
public class AgentMenu extends RLMenu {

	public ApproximatorMenu approximatorMenu = new ApproximatorMenu(true);
	public JRadioButtonMenuItem haiq = new JRadioButtonMenuItem(new MyAction(
			"HaiQ"));
	public JRadioButtonMenuItem qlambda = new JRadioButtonMenuItem(
			new MyAction("Q(lambda)"));
	public JRadioButtonMenuItem sarsalambda = new JRadioButtonMenuItem(
			new MyAction("SARSA(lmabda)"));
	public JRadioButtonMenuItem qzero = new JRadioButtonMenuItem(new MyAction(
			"Q(0)"));
	public JRadioButtonMenuItem brain = null;
	public AgentMenu soulMenu = null;

	// TODO make a menu for this
	private ActionSelector getActionSelector() {
		return new EpsilonGreedyActionSelector();
		// return new ByQActionSelector();
	}

	public AgentMenu(String prefix, int depth) {
		super(prefix + "RL Agent");

		ButtonGroup bg = new ButtonGroup();
		bg.add(haiq);
		bg.add(qlambda);
		bg.add(sarsalambda);
		bg.add(qzero);

		add(haiq);
		add(qlambda);
		add(sarsalambda);
		add(qzero);
		if (depth > 0) {
			brain = new JRadioButtonMenuItem(new MyAction("Brain"));
			bg.add(brain);
			soulMenu = new AgentMenu("Brain's ", depth - 1);
			soulMenu.addActionListener(new MyAction(""));
			addSeparator();
			add(brain);
			add(soulMenu);
			sarsalambda.setSelected(true);
		} else {
			sarsalambda.setSelected(true);
		}

		addSeparator();
		add(approximatorMenu);

		approximatorMenu.addActionListener(new MyAction(""));
	}

	public LearnerAndActor getAgent(int numActions, double[] s0,
			ApproxParameters approxParameters, RLParameters rLParameters) {

		if (haiq.isSelected()) {
			// return new HsomQBrain(s0.length, numActions);
		}

		UpdateProcedure up = null;

		if (qlambda.isSelected()) {
			up = new QUpdateProcedure();
		}
		if (sarsalambda.isSelected()) {
			up = new SARSAUpdateProcedure();
		}
		ParameterizedFunctionGenerator pfg = approximatorMenu
				.getFunctionGenerator(approxParameters);

		if (qlambda.isSelected() || sarsalambda.isSelected()) {
			return new RLAgent(pfg, up, getActionSelector(), numActions, s0,
					approxParameters, rLParameters, 4);
		}

		if (qzero.isSelected()) {
			return new QZeroAgent(rLParameters, pfg,
					new EpsilonGreedyActionSelector(), s0, numActions);
		}

		if (brain.isSelected()) {
			return new Brain(s0.length, numActions, rLParameters, pfg,
					soulMenu.getAgent(numActions, s0, approxParameters,
							rLParameters));
		}

		return null;
	}

}
